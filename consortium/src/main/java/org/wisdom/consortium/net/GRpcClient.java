package org.wisdom.consortium.net;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.consortium.proto.*;
import org.wisdom.common.Peer;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.wisdom.consortium.net.Util.getRawForSign;

@Slf4j
public class GRpcClient implements Channel.ChannelListener {
    private PeerImpl self;
    private AtomicLong nonce = new AtomicLong();
    private Channel.ChannelListener listener;
    PeersCache peersCache;

    public GRpcClient(PeerImpl self, PeerServerConfig config) {
        this.self = self;
        this.peersCache = new PeersCache(self, config);
    }

    GRpcClient withListener(Channel.ChannelListener listener) {
        this.listener = listener;
        return this;
    }

    void broadcast(Code code, long ttl, byte[] body) {
        peersCache.getChannels().forEach(ch -> ch.write(buildMessage(code, ttl, body)));
    }

    public void dial(Peer peer, Code code, long ttl, byte[] body, Channel.ChannelListener... listeners) {
        dial(peer, buildMessage(code, ttl, body), listeners);
    }

    public void dial(String host, int port, Code code, long ttl, byte[] body, Channel.ChannelListener... listeners) {
        dial(host, port, buildMessage(code, ttl, body), listeners);
    }

    private void dial(String host, int port, Message message, Channel.ChannelListener... listeners) {
        createChannel(host, port, listeners).write(message);
    }

    private void dial(Peer peer, Message message, Channel.ChannelListener... listeners) {
        Optional<Channel> o = peersCache.getChannel(peer.getID());
        if (o.isPresent() && !o.get().isClosed()) {
            o.get().write(message);
        }
        createChannel(peer.getHost(), peer.getPort(), listeners).write(message);
    }


    Channel createChannel(String host, int port, Channel.ChannelListener... listeners) {
        ManagedChannel ch = ManagedChannelBuilder
                .forAddress(host, port).usePlaintext().build();
        EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
        ProtoChannel channel = new ProtoChannel();
        channel.addListener(this);
        if (listener != null) channel.addListener(listener);
        channel.addListener(listeners);
        channel.setOut(stub.entry(channel));
        return channel;
    }

    StreamObserver<Message> createObserver(StreamObserver<Message> out, Channel.ChannelListener... listeners) {
        ProtoChannel channel = new ProtoChannel();
        channel.addListener(this);
        if (listener != null) channel.addListener(listener);
        channel.addListener(listeners);
        channel.setOut(out);
        return channel;
    }

    @Override
    public void onConnect(PeerImpl remote, Channel channel) {
        peersCache.keep(remote, channel);
    }

    @Override
    public void onMessage(Message message, Channel channel) {
    }

    @Override
    public void onError(Throwable throwable, Channel channel) {
        log.error(throwable.getMessage());
    }

    @Override
    public void onClose(Channel channel) {
        Optional<PeerImpl> remote = channel.getRemote();
        if (!remote.isPresent()) return;
        log.error("close channel to " + remote.get());
        peersCache.remove(remote.get());
    }

    public Message buildMessage(Code code, long ttl, byte[] msg) {
        Message.Builder builder = Message.newBuilder()
                .setCode(code)
                .setCreatedAt(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000))
                .setRemotePeer(self.encodeURI())
                .setTtl(ttl)
                .setNonce(nonce.incrementAndGet())
                .setBody(ByteString.copyFrom(msg));
        byte[] sig = self.getPrivateKey().sign(getRawForSign(builder.build()));
        return builder.setSignature(ByteString.copyFrom(sig)).build();
    }

    void relay(Message message, Peer receivedFrom) {
        Message.Builder builder = Message.newBuilder().mergeFrom(message)
                .setCreatedAt(
                        Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build()
                )
                .setNonce(nonce.incrementAndGet())
                .setTtl(message.getTtl() - 1);
        byte[] sig = self.getPrivateKey().sign(getRawForSign(builder.build()));
        message = builder.setSignature(ByteString.copyFrom(sig)).build();
        Message finalMessage = message;
        peersCache.getChannels().forEach(c ->{
            if(c.getRemote().map(r -> r.equals(receivedFrom)).orElse(true)) return;
            c.write(finalMessage);
        });
    }
}
