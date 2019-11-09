package org.wisdom.consortium.net;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.consortium.proto.*;
import org.wisdom.common.Peer;

import java.util.Optional;

@Slf4j
public class GRpcClient implements Channel.ChannelListener {
    private Channel.ChannelListener listener;
    private PeerServerConfig config;
    MessageBuilder messageBuilder;
    PeersCache peersCache;

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

    void dial(String host, int port, Message message) {
        Optional<Channel> ch = createChannel(host, port);
        ch.ifPresent(x -> x.write(message));
    }

    void dial(Peer peer, Message message) {
        Optional<Channel> o = peersCache.getChannel(peer.getID());
        if (o.isPresent() && !o.get().isClosed()) {
            o.get().write(message);
            return;
        }
        Optional<Channel> ch = createChannel(peer.getHost(), peer.getPort());
        ch.ifPresent(x -> x.write(message));
    }


    Optional<Channel> createChannel(String host, int port) {
        try {
            ManagedChannel ch = ManagedChannelBuilder
                    .forAddress(host, port).usePlaintext().build();
            EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
            ProtoChannel channel = new ProtoChannel();
            channel.addListener(this);
            if (listener != null) channel.addListener(listener);
            channel.setOut(stub.entry(channel));
            channel.write(messageBuilder.buildMessage(Code.PING, 1, Ping.newBuilder().build().toByteArray()));
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
        boolean isBootstrap = config.getBootstraps() != null &&
                config.getBootstraps().stream().anyMatch(
                        x -> x.getHost().equals(remote.getHost()) && x.getPort() == remote.getPort()
                );
        if (!config.isEnableDiscovery() && !isBootstrap) {
            channel.close();
            return;
        }
        if(isBootstrap){
            peersCache.bootstraps.put(remote, true);
        }
        peersCache.keep(remote, channel);
    }

    @Override
    public void onMessage(Message message, Channel channel) {
    }

    @Override
    public void onError(Throwable throwable, Channel channel) {
        if (config.isEnableDiscovery()) {
            channel.getRemote()
                    .filter(x -> !peersCache.hasBlocked(x))
                    .ifPresent(x -> peersCache.half(x));
        }
        log.error("error found" + throwable.getMessage());
    }

    @Override
    public void onClose(Channel channel) {
        Optional<PeerImpl> remote = channel.getRemote();
        if (!remote.isPresent()) return;
        log.error("close channel to " + remote.get());
        peersCache.remove(remote.get());
    }

    void relay(Message message, PeerImpl receivedFrom){
        peersCache.getChannels()
                .filter(x -> !x.getRemote().map(p -> p.equals(receivedFrom)).orElse(false))
                .forEach(c -> c.write(messageBuilder.buildRelay(message)));
    }
}
