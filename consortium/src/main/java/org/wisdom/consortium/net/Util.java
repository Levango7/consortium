package org.wisdom.consortium.net;

import com.google.common.primitives.Bytes;
import org.wisdom.consortium.proto.Message;
import org.wisdom.util.BigEndian;

import java.nio.charset.StandardCharsets;

public class Util {
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
}
