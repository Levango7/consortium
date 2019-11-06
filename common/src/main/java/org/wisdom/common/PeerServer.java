package org.wisdom.common;

import java.util.List;
import java.util.Properties;

public interface PeerServer {
    void dial(Peer peer, Serializable message);

    void broadcast(Serializable message);

    List<Peer> getPeers();

    void use(PeerServerListener... peerServerListeners);

    void start();

    void load(Properties properties);
}
