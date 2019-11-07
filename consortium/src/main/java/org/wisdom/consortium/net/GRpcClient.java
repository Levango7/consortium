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
    ConcurrentHashMap<Peer, Channel> channels = new ConcurrentHashMap<>();
    private PeerImpl self;
    private AtomicLong nonce = new AtomicLong();
    private Channel.ChannelListener listener;

    public GRpcClient(PeerImpl self) {
        this.self = self;
    }

    GRpcClient withListener(Channel.ChannelListener listener){
        this.listener = listener;
        return this;
    }

    void broadcast(Code code, long ttl, byte[] body){
        channels.values().forEach(ch -> ch.write(buildMessage(code, ttl, body)));
    }

    public void dial(Peer peer, Code code, long ttl, byte[] body) {
        dial(peer, buildMessage(code, ttl, body));
    }

    public void dial(String host, int port, Code code, long ttl, byte[] body) {
        dial(host, port, buildMessage(code, ttl, body));
    }

    private void dial(String host, int port, Message message) {
        createChannel(host, port).write(message);
    }

    private void dial(Peer peer, Message message) {
        if (channels.containsKey(peer) && !channels.get(peer).isClosed()) {
            channels.get(peer).write(message);
        }
        createChannel(peer.getHost(), peer.getPort()).write(message);
    }


    Channel createChannel(String host, int port) {
        ManagedChannel ch = ManagedChannelBuilder
                .forAddress(host, port).usePlaintext().build();
        EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
        ProtoChannel channel = new ProtoChannel();
        channel.addListener(this);
        if (listener != null) channel.addListener(listener);
        channel.setOut(stub.entry(channel));
        return channel;
    }

    StreamObserver<Message> createObserver(StreamObserver<Message> out) {
        ProtoChannel channel = new ProtoChannel();
        channel.addListener(this);

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
        log.error(throwable.getMessage());
    }

    @Override
    public void onClose(Channel channel) {
        Optional<PeerImpl> remote = channel.getRemote();
        if(!remote.isPresent()) return;
        log.error("close channel to " + remote.get());
        channels.remove(remote.get());
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

    void relay(Message message, Peer receivedFrom){
        Message.Builder builder = Message.newBuilder().mergeFrom(message)
                .setCreatedAt(
                        Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000 ).build()
                )
                .setNonce(nonce.incrementAndGet())
                .setTtl(message.getTtl() - 1);
        byte[] sig = self.getPrivateKey().sign(getRawForSign(builder.build()));
        message = builder.setSignature(ByteString.copyFrom(sig)).build();
        for(Peer p: channels.keySet()){
            if(!p.equals(receivedFrom)) channels.get(p).write(message);
        }
    }
}
