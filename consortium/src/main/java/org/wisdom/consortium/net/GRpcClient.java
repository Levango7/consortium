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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.wisdom.consortium.net.Util.getRawForSign;

@Slf4j
public class GRpcClient implements Channel.ChannelListener {
    private ConcurrentHashMap<Peer, Channel> channels = new ConcurrentHashMap<>();
    private PeerImpl self;
    private AtomicLong nonce = new AtomicLong();

    public GRpcClient(PeerImpl self) {
        this.self = self;
    }

    public void dial(Peer peer, Nothing nothing, Channel.ChannelListener... listeners) {
        dial(peer, buildMessage(1, nothing), listeners);
    }

    public void dial(Peer peer, Ping ping, Channel.ChannelListener... listeners) {
        dial(peer, buildMessage(1, ping), listeners);
    }

    public void dial(Peer peer, Pong pong, Channel.ChannelListener... listeners) {
        dial(peer, buildMessage(1, pong), listeners);
    }

    public void dial(Peer peer, Lookup lookup, Channel.ChannelListener... listeners) {
        dial(peer, buildMessage(1, lookup), listeners);
    }

    public void dial(Peer peer, Peers peers, Channel.ChannelListener... listeners) {
        dial(peer, buildMessage(1, peers), listeners);
    }

    void dial(String host, int port, Nothing message, Channel.ChannelListener... listeners) {
        dial(host, port, buildMessage(1, message), listeners);
    }

    void dial(String host, int port, Ping message, Channel.ChannelListener... listeners) {
        dial(host, port, buildMessage(1, message), listeners);
    }

    void dial(String host, int port, Pong message, Channel.ChannelListener... listeners) {
        dial(host, port, buildMessage(1, message), listeners);
    }

    void dial(String host, int port, Lookup message, Channel.ChannelListener... listeners) {
        dial(host, port, buildMessage(1, message), listeners);
    }

    void dial(String host, int port, Peers message, Channel.ChannelListener... listeners) {
        dial(host, port, buildMessage(1, message), listeners);
    }

    void dial(String host, int port, byte[] message, Channel.ChannelListener... listeners) {
        dial(host, port, buildMessage(1, message), listeners);
    }

    void dial(String host, int port, Message message, Channel.ChannelListener... listeners) {
            createChannel(host, port, listeners).write(message);
    }

    void dial(Peer peer, Message message, Channel.ChannelListener... listeners) {
        if (channels.containsKey(peer) && !channels.get(peer).isClosed()) {
            channels.get(peer).write(message);
        }
        createChannel(peer.getHost(), peer.getPort(), listeners).write(message);
    }


    Channel createChannel(String host, int port, Channel.ChannelListener... listeners) {
        ManagedChannel ch = ManagedChannelBuilder
                .forAddress(host, port).usePlaintext().build();
        EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
        PeerChannel channel = new PeerChannel();
        channel.addListener(this);
        channel.addListener(listeners);
        channel.setOut(stub.entry(channel));
        return channel;
    }

    StreamObserver<Message> createObserver(StreamObserver<Message> out, Channel.ChannelListener... listeners) {
        PeerChannel channel = new PeerChannel();
        channel.addListener(this);
        channel.addListener(listeners);
        channel.setOut(out);
        return channel;
    }

    @Override
    public void onConnect(PeerImpl remote, Channel channel) {
        if (!channels.containsKey(remote) || channels.get(remote).isClosed()) {
            channels.put(remote, channel);
            return;
        }
        channel.close();
    }

    @Override
    public void onMessage(Message message, Channel channel) {
    }

    @Override
    public void onError(Throwable throwable, Channel channel) {
        Optional<PeerImpl> remote = channel.getRemote();
        if(!remote.isPresent()) return;
        log.error("cannot connect to peer " + remote.get());
        channels.remove(remote.get());
    }

    public Message buildMessage(long ttl, Nothing msg) {
        return buildMessage(Code.NOTHING, nonce.incrementAndGet(), ttl, msg.toByteArray());
    }

    public Message buildMessage(long ttl, Ping msg) {
        return buildMessage(Code.PING, nonce.incrementAndGet(), ttl, msg.toByteArray());
    }

    public Message buildMessage(long ttl, Pong msg) {
        return buildMessage(Code.PONG, nonce.incrementAndGet(), ttl, msg.toByteArray());
    }

    public Message buildMessage(long ttl, Lookup msg) {
        return buildMessage(Code.LOOK_UP, nonce.incrementAndGet(), ttl, msg.toByteArray());
    }

    public Message buildMessage(long ttl, Peers msg) {
        return buildMessage(Code.PEERS, nonce.incrementAndGet(), ttl, msg.toByteArray());
    }

    public Message buildMessage(long ttl, byte[] msg) {
        return buildMessage(Code.ANOTHER, nonce.incrementAndGet(), ttl, msg);
    }

    public Message buildMessage(Code code, long nonce, long ttl, byte[] msg) {
        Message.Builder builder = Message.newBuilder()
                .setCode(code)
                .setCreatedAt(Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000))
                .setRemotePeer(self.encodeURI())
                .setTtl(ttl)
                .setNonce(nonce)
                .setBody(ByteString.copyFrom(msg));
        byte[] sig = self.getPrivateKey().sign(getRawForSign(builder.build()));
        return builder.setSignature(ByteString.copyFrom(sig)).build();
    }

}
