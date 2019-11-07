package org.wisdom.common;

public interface Peer {
    String getHost();

    int getPort();

    HexBytes getID();
}
