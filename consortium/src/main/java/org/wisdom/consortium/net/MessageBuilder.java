package org.wisdom.consortium.net;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import org.wisdom.consortium.proto.Code;
import org.wisdom.consortium.proto.Message;

import java.util.concurrent.atomic.AtomicLong;

import static org.wisdom.consortium.net.Util.getRawForSign;

public class MessageBuilder {
    private PeerImpl self;
    private AtomicLong nonce = new AtomicLong();

    public MessageBuilder(PeerImpl self) {
        this.self = self;
    }

    public Message buildRelay(Message message) {
        Message.Builder builder = Message.newBuilder().mergeFrom(message)
                .setCreatedAt(
                        Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build()
                )
                .setRemotePeer(self.encodeURI())
                .setNonce(nonce.incrementAndGet())
                .setTtl(message.getTtl() - 1);
        byte[] sig = self.getPrivateKey().sign(getRawForSign(builder.build()));
        return builder.setSignature(ByteString.copyFrom(sig)).build();
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
}
