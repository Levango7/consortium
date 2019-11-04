package wallet;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class KeystoreTest {

    @Test
    public void test() throws Exception {
        String priKey = "a9ce809d201b28e3fd00b269ab042ed27c6ccd6d330a03c13102556b9c958178";
        System.out.println("priKey: " + priKey);
        System.out.println("pubKey: " + KeystoreAction.priKeyToPubkey(priKey));
        String pubKey = KeystoreAction.priKeyToPubkey(priKey);
        System.out.println("pubKeyHash: " + KeystoreAction.pubKeyToPubKeyHash(pubKey));
        String address = KeystoreAction.pubKeyToAddress(Hex.decodeHex(pubKey.toCharArray()));
        System.out.println("address: " + address);
        System.out.println("pubKeyHash: " + Hex.encodeHexString(KeystoreAction.addressToPubKeyHash(address)));
        String pubKeyHash = "1b83fceae112e4147e84886594bf2439a97ebb44";
        byte[] pubHash = Hex.decodeHex(pubKeyHash.toCharArray());
        System.out.println("address: " + KeystoreAction.pubKeyHashToAddress(pubHash));
    }
}