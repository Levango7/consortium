package wallet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import crypto.*;
import crypto.ed25519.Ed25519PrivateKey;
import crypto.ed25519.Ed25519PublicKey;
import util.Base58Utility;
import util.ByteUtil;
import util.ByteUtils;

import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.util.Arrays;


public class WalletUtility {

    public String address;
    public Crypto crypto;
    private static final int saltLength = 32;
    private static final int ivLength = 16;
    private static final String defaultVersion = "1";
    private static final String t = "1000000000000000000000000000000014def9dea2f79cd65812631a5cf5d3ec";
    public static byte[] outscrip;
    private static final Long rate= 100000000L;


    /**
     *       3.将r1进行两次SHA3-256计算，得到结果r3，
     *            获得r3的前面4个字节，称之为b4
     *       4.将b4附加在r2的后面，得到结果r5
     *       5.将r5进行base58编码，得到结果r6
     *       6.r6就是地址
     * @param r1Str
     * @return
     */
    public static String pubkeyHashToAddress(String r1Str){
        try {
            byte[] r1 = Hex.decodeHex(r1Str.toCharArray());
            byte[] r2 = ByteUtil.prepend(r1,(byte)0x00);
            byte[] r3 = SHA3Utility.keccak256(SHA3Utility.keccak256(r1));
            byte[] b4 = ByteUtil.bytearraycopy(r3,0,4);
            byte[] b5 = ByteUtil.byteMerger(r2,b4);
            String s6 = Base58Utility.encode(b5);
            return  s6;
        }catch (Exception e){
            return "";
        }
    }

    /**
     *     地址转公钥哈希
     *    1.将地址进行base58解码，得到结果r5
     *    2.将r5移除后后面4个字节得到r2
     *    3.将r2移除第1个字节:0x01得到r1(公钥哈希值)
     * @param address
     * @return
     */
    public static String addressToPubkeyHash(String address){
        try {
            byte[] r5 = Base58Utility.decode(address);
            byte[] r2 = ByteUtil.bytearraycopy(r5,0,21);
            byte[] r1 = ByteUtil.bytearraycopy(r2,1,20);
            String publickeyHash =  new String(Hex.encodeHex(r1));
            return  publickeyHash;
        }catch (Exception e){
            return "";
        }
    }

    /**
     * 通过keystore,密码获取地址
     * @param ksJson
     * @param password
     * @return
     */
    public static String keystoreToAddress(String ksJson,String password){
        try {
            Keystore ks = JSON.parseObject(ksJson, Keystore.class);
            String address = ks.address;
            return  address;
        }catch (Exception e){
            return "";
        }
    }

    /**
     * 通过keystore,密码获取公钥
     * @param ksJson
     * @param password
     * @return
     */
    public static String keystoreToPubkey(String ksJson,String password){
        try {
            Keystore ks = JSON.parseObject(ksJson, Keystore.class);
            String privateKey =  KeystoreAction.obPrikey(ks,password);
            String pubkey = KeystoreAction.prikeyToPubkey(privateKey);
            return  pubkey;
        }catch (Exception e){
            return "";
        }
    }

    /**
     * 通过keystore,密码获取公钥hash
     * @param ksJson
     * @param password
     * @return
     */
    public static String keystoreToPubkeyHash(String ksJson,String password){
        try {
            Keystore ks = JSON.parseObject(ksJson, Keystore.class);
            String privateKey =  KeystoreAction.obPrikey(ks,password);
            String pubkey = KeystoreAction.prikeyToPubkey(privateKey);
            byte[] pub256 = SHA3Utility.keccak256(Hex.decodeHex(pubkey.toCharArray()));
            byte[] r1 = RipemdUtility.ripemd160(pub256);
            String pubkeyHash = new String(Hex.encodeHex(r1));
            return  pubkeyHash;
        }catch (Exception e){
            return "";
        }
    }

    /**
     * 通过keystore,密码获取私钥
     * @param ksJson
     * @param password
     * @return
     */
    public static String obtainPrikey(String ksJson, String password){
        try {
            Keystore ks = JSON.parseObject(ksJson, Keystore.class);
            String privateKey =  new String(Hex.encodeHex(KeystoreAction.decrypt(ks,password)));
            return  privateKey;
        }catch (Exception e){
            return "";
        }
    }

    /**
     * 通过私钥获取公钥
     * @param prikey
     * @return
     */
    public static String prikeyToPubkey(String prikey){
        try {
            if(prikey.length() != 64 || new BigInteger(Hex.decodeHex(prikey.toCharArray())).compareTo(new BigInteger(ByteUtils.hexStringToBytes(t))) > 0){
                return "";
            }
            Ed25519PrivateKey eprik = new Ed25519PrivateKey(Hex.decodeHex(prikey.toCharArray()));
            Ed25519PublicKey epuk = eprik.generatePublicKey();
            String pubkey = new String(Hex.encodeHex(epuk.getEncoded()));
            return  pubkey;
        }catch (Exception e){
            return "";
        }
    }

    /**
     * pubkeyStrToPubkeyHashStr
     * @param pubkeyStr
     * @return
     */
    public static String pubkeyStrToPubkeyHashStr(String pubkeyStr){
        try {
            byte[] pubkey = Hex.decodeHex(pubkeyStr.toCharArray());
            byte[] pub256 = SHA3Utility.keccak256(pubkey);
            byte[] r1 = RipemdUtility.ripemd160(pub256);
            String pubkeyHashStr = new String(Hex.encodeHex(r1));
            return  pubkeyHashStr;
        }catch (Exception e){
            return "";
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * 地址有效性校验
     * @param address
     * @return
     */
    public static int verifyAddress(String address){
        try {
            byte[] r5 = Base58Utility.decode(address);
            if(!address.startsWith("1")){
                return  -1;
            }
            byte[] r3 = SHA3Utility.keccak256(SHA3Utility.keccak256(KeystoreAction.atph(address)));
            byte[] b4 = ByteUtil.bytearraycopy(r3,0,4);
            byte[] _b4 = ByteUtil.bytearraycopy(r5,r5.length-4,4);
            if(Arrays.equals(b4,_b4)){
                return  0;
            }else {
                return  -2;
            }
        }catch (Exception e){
            return -2;
        }
    }

}