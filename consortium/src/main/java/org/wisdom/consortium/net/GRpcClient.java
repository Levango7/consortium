package org.wisdom.consortium.net;

import com.google.common.primitives.Bytes;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Arrays;
import org.wisdom.consortium.proto.*;
import org.wisdom.util.BigEndian;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class GRpcClient implements Channel.ChannelListener {
    private ConcurrentHashMap<Peer, Channel> channels;
    private Peer self;
    private AtomicLong nonce;

    void dial(String host, int port, byte[] message) {
        try {
            Channel ch = openChannel(host, port);
            ch.write(buildMessage(1, message));
        } catch (Exception e) {
            log.error("cannot connect to peer " + host + ":" + port);
        }
    }

    void dial(Peer peer, byte[] message) {
        if (channels.containsKey(peer) && !channels.get(peer).isClosed()) {
            boolean success = channels.get(peer).write(buildMessage(1, message));
        }
        try {
            Channel ch = openChannel(peer.getHost(), peer.getPort());
            channels.put(peer, ch);
            ch.write(buildMessage(1, message));
        } catch (Exception e) {
            log.error("cannot connect to peer " + peer);
        }
    }


    public Channel openChannel(String host, int port) {
        ManagedChannel ch = ManagedChannelBuilder
                .forAddress(host, port).usePlaintext().build();
        EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
        PeerChannel channel = new PeerChannel();
        channel.setOut(stub.entry(channel));
        return channel;
    }

    @Override
    public void onConnect(Peer remote, Channel channel) {
        if (!channels.containsKey(remote) || channels.get(remote).isClosed()) {
            channels.put(remote, channel);
            return;
        }
        channel.close();
    }

    @Override
    public void onMessage(Message message, Channel channel) {

    }

    public static byte[] getRawForSign(Message msg) {
        return Bytes.concat(
                BigEndian.encodeInt32(msg.getCode().getNumber()),
                BigEndian.encodeInt64(msg.getCreatedAt().getSeconds()),
                msg.getRemotePeer().getBytes(StandardCharsets.UTF_8),
                BigEndian.encodeInt64(msg.getTtl()),
                BigEndian.encodeInt64(msg.getNonce()),
                msg.getBody().toByteArray()
        );
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

    public Message buildMessage(long ttl, byte[] msg){
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
