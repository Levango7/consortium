package org.wisdom.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * hex bytes helper for json marshal/unmarshal
 * <p>
 * HexBytes bytes = mapper.readValue("ffff", HexBytes.class);
 * String json = mapper.writeValueAsString(new HexBytes(new byte[32]));
 */
@JsonDeserialize(using = HexBytesUtils.HexBytesDeserializer.class)
@JsonSerialize(using = HexBytesUtils.HexBytesSerializer.class)
public class HexBytes {
    private byte[] bytes;
    private String hexCache;

    public static String encode(byte[] bytes){
        return Hex.encodeHexString(bytes);
    }

    public static HexBytes parse(String hex) throws DecoderException{
        return new HexBytes(hex);
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int size(){
        return bytes == null ? 0: bytes.length;
    }

    public String toString(){
        if (hexCache != null) return hexCache;
        hexCache = Hex.encodeHexString(bytes);
        return hexCache;
    }

    public HexBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public HexBytes(String hex) throws DecoderException {
        bytes = Hex.decodeHex(hex.toCharArray());
        hexCache = hex;
    }
}
