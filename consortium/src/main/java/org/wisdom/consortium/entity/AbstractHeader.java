package org.wisdom.consortium.entity;

public abstract class AbstractHeader{
    public abstract byte[] getHash();

    public abstract int getVersion();

    public abstract byte[] getHashPrev();

    public abstract byte[] getMerkleRoot();

    public abstract long getHeight();

    public abstract long getCreatedAt();

    public abstract byte[] getPayload();
}
