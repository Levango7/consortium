package org.wisdom.common;

public interface Serializable {
    void copyFromByte(byte[] data);
    byte[] getBytes();
}
