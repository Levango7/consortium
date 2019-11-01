package crypto.ed25519;

import crypto.CryptoException;

public interface PrivateKey extends java.security.PrivateKey {
    byte[] sign(byte[] msg) throws CryptoException;
    PublicKey generatePublicKey();
}
