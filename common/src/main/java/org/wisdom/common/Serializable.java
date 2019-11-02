package org.wisdom.common;

public interface Serializable {
    void copyFrom(byte[] data);
    byte[] getBytes();
}
