package org.wisdom.common;

public interface Chained {
    HexBytes getHashPrev();
    HexBytes getHash();
    long getHeight();
}
