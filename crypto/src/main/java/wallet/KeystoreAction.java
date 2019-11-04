package wallet;

import crypto.RipemdUtility;
import crypto.SHA3Utility;
import crypto.ed25519.Ed25519PrivateKey;
import crypto.ed25519.Ed25519PublicKey;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import util.Base58Utility;
import util.ByteUtil;

import java.math.BigInteger;

public class KeystoreAction {
    private static final String t = "1000000000000000000000000000000014def9dea2f79cd65812631a5cf5d3ec";
    public Crypto crypto;

    public static String pubKeyToAddress(byte[] pubkey) {
        byte[] pub256 = SHA3Utility.keccak256(pubkey);
        byte[] r1 = RipemdUtility.ripemd160(pub256);
        byte[] r2 = ByteUtil.prepend(r1, (byte) 0x00);
        byte[] r3 = SHA3Utility.keccak256(SHA3Utility.keccak256(r1));
        byte[] b4 = ByteUtil.bytearraycopy(r3, 0, 4);
        byte[] b5 = ByteUtil.byteMerger(r2, b4);
        String s6 = Base58Utility.encode(b5);
        return s6;
    }

    public static byte[] addressToPubKeyHash(String address) {
        byte[] r5 = Base58Utility.decode(address);
        byte[] r2 = ByteUtil.bytearraycopy(r5, 0, 21);
        byte[] r1 = ByteUtil.bytearraycopy(r2, 1, 20);
        return r1;
    }

    public static String pubKeyHashToAddress(byte[] r1) {
        byte[] r2 = ByteUtil.prepend(r1, (byte) 0x00);
        byte[] r3 = SHA3Utility.keccak256(SHA3Utility.keccak256(r1));
        byte[] b4 = ByteUtil.bytearraycopy(r3, 0, 4);
        byte[] b5 = ByteUtil.byteMerger(r2, b4);
        String s6 = Base58Utility.encode(b5);
        return s6;
    }


    public static String priKeyToPubkey(String prikey) throws Exception {
        if (prikey.length() != 64 || new BigInteger(Hex.decodeHex(prikey.toCharArray())).compareTo(new BigInteger(Hex.decodeHex(t.toCharArray()))) > 0) {
            throw new Exception("Private key format error");
        }
        Ed25519PrivateKey eprik = new Ed25519PrivateKey(Hex.decodeHex(prikey.toCharArray()));
        Ed25519PublicKey epuk = eprik.generatePublicKey();
        return Hex.encodeHexString(epuk.getEncoded());
    }

    public static String pubKeyToPubKeyHash(String pubkeyStr) {
        byte[] pubkey;
        try {
            pubkey = Hex.decodeHex(pubkeyStr.toCharArray());
            byte[] pub256 = SHA3Utility.keccak256(pubkey);
            byte[] r1 = RipemdUtility.ripemd160(pub256);
            return Hex.encodeHexString(r1);
        } catch (DecoderException e) {
            return "";
        }
    }

}
