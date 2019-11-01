package crypto.ed25519;


public interface KeyPair {
    PrivateKey getPrivateKey();
    PublicKey getPublicKey();
}
