package org.wisdom.common;

// context for communicating with peer server and plugin
public interface Context {
    void disconnect();

    void block();

    void keep();

    void response(Serializable message);

    void relay();

    byte[] getMessage();

    Peer getRemote();
}
