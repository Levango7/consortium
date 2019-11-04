package org.wisdom.keystore.ed25519;

public interface PublicKey extends java.security.PublicKey {
    boolean verify(byte[] msg, byte[] signature);
}
