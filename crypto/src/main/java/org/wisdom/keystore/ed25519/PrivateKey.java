package org.wisdom.keystore.ed25519;

import org.wisdom.keystore.crypto.CryptoException;

public interface PrivateKey extends java.security.PrivateKey {
    byte[] sign(byte[] msg) throws CryptoException;
    PublicKey generatePublicKey();
}
