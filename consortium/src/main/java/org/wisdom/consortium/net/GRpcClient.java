package org.wisdom.consortium.net;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.consortium.proto.*;
import org.wisdom.common.Peer;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class GRpcClient implements Channel.ChannelListener {
    private Channel.ChannelListener listener;
    private PeerServerConfig config;
    MessageBuilder messageBuilder;
    PeersCache peersCache;

    @AllArgsConstructor
    private abstract static class AbstractChannelListener implements Channel.ChannelListener{
        protected GRpcClient client;
        protected Channel.ChannelListener listener;

        @Override
        public void onMessage(Message message, Channel channel) {
            client.onMessage(message, channel);
            if(listener == null) return;
            listener.onMessage(message, channel);
        }

        @Override
        public void onError(Throwable throwable, Channel channel) {
            client.onError(throwable, channel);
            if(listener == null) return;
            listener.onError(throwable, channel);
        }

        @Override
        public void onClose(Channel channel) {
            client.onClose(channel);
            if(listener == null) return;
            listener.onClose(channel);
        }
    }

    private static class BootstrapChannelListener extends AbstractChannelListener {
        private BootstrapChannelListener(GRpcClient client, Channel.ChannelListener listener) {
            super(client, listener);
        }

        @Override
        public void onConnect(PeerImpl remote, Channel channel) {
            client.peersCache.bootstraps.put(remote, true);
            if(client.peersCache.has(remote)){
                channel.close();
                return;
            }
            client.peersCache.keep(remote, channel);
            if(listener == null)return;
            listener.onConnect(remote, channel);
        }
    }

    private static class TrustedChannelListener extends AbstractChannelListener {
        public TrustedChannelListener(GRpcClient client, Channel.ChannelListener listener) {
            super(client, listener);
        }
        @Override
        public void onConnect(PeerImpl remote, Channel channel) {
            client.peersCache.trusted.put(remote, true);
            if(client.peersCache.has(remote)){
                channel.close();
                return;
            }
            client.peersCache.keep(remote, channel);
            if(listener == null)return;
            listener.onConnect(remote, channel);
        }
    }

    public GRpcClient(PeerImpl self, PeerServerConfig config) {
        this.peersCache = new PeersCache(self, config);
        this.config = config;
        this.messageBuilder = new MessageBuilder(self);
    }

    GRpcClient withListener(Channel.ChannelListener listener) {
        this.listener = listener;
        return this;
    }

    void broadcast(Message message) {
        peersCache.getChannels().forEach(ch -> ch.write(message));
    }

    void dial(Peer peer, Message message) {
        Optional<Channel> o = peersCache.getChannel(peer.getID());
        if (o.isPresent() && !o.get().isClosed()) {
            o.get().write(message);
            return;
        }
        Optional<Channel> ch = createChannel(peer.getHost(), peer.getPort(), this, listener);
        ch.ifPresent(x -> x.write(message));
    }

    void dial(String host, int port, Message message) {
        createChannel(host, port, this, listener).ifPresent(ch -> ch.write(message));
    }

    void bootstrap(Collection<URI> uris) {
        for (URI uri : uris) {
            createChannel(uri.getHost(), uri.getPort(), new BootstrapChannelListener(this, listener));
        }
    }

    void trust(Collection<URI> trusted){
        for (URI uri : trusted) {
            createChannel(uri.getHost(), uri.getPort(), new TrustedChannelListener(this, listener));
        }
    }

    private Optional<Channel> createChannel(String host, int port, Channel.ChannelListener... listeners) {
        try {
            ManagedChannel ch = ManagedChannelBuilder
                    .forAddress(host, port).usePlaintext().build();
            EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
            ProtoChannel channel = new ProtoChannel();
            channel.addListener(
                    Arrays.stream(listeners)
                            .filter(Objects::nonNull)
                            .toArray(Channel.ChannelListener[]::new
                            )
            );
            channel.setOut(stub.entry(channel));
            channel.write(messageBuilder.buildPing());
            return Optional.of(channel);
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    ProtoChannel createChannel(StreamObserver<Message> out) {
        ProtoChannel channel = new ProtoChannel();
        channel.addListener(this);
        if (listener != null) channel.addListener(listener);
        channel.setOut(out);
        return channel;
    }

    @Override
    public void onConnect(PeerImpl remote, Channel channel) {
        if (!config.isEnableDiscovery() &&
                !peersCache.bootstraps.containsKey(remote) &&
                !peersCache.trusted.containsKey(remote)
        ) {
            channel.close();
            return;
        }
        if (peersCache.getChannel(remote).map(c -> !c.isClosed()).orElse(false)) {
            // the channel had already exists
            channel.close();
            return;
        }
        peersCache.keep(remote, channel);
    }

    @Override
    public void onMessage(Message message, Channel channel) {
    }

    @Override
    public void onError(Throwable throwable, Channel channel) {
        channel.getRemote()
                    .filter(x -> !peersCache.hasBlocked(x))
                    .ifPresent(x -> peersCache.half(x));
        log.error("error found " + throwable.getMessage());
    }

    @Override
    public void onClose(Channel channel) {
        Optional<PeerImpl> remote = channel.getRemote();
        if (!remote.isPresent()) return;
        log.error("close channel to " + remote.get());
        peersCache.remove(remote.get());
    }

    void relay(Message message, PeerImpl receivedFrom) {
        peersCache.getChannels()
                .filter(x -> !x.getRemote().map(p -> p.equals(receivedFrom)).orElse(false))
                .forEach(c -> c.write(messageBuilder.buildRelay(message)));
    }
}
