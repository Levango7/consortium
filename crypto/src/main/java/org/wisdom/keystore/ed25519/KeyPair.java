package org.wisdom.keystore.ed25519;


public interface KeyPair {
    PrivateKey getPrivateKey();
    PublicKey getPublicKey();
}
