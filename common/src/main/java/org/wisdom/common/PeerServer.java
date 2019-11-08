package org.wisdom.common;

import org.wisdom.exception.PeerServerLoadException;

import java.util.List;
import java.util.Properties;

public interface PeerServer {
    // dial a peer with a message
    void dial(Peer peer, Serializable message);

    // broadcast a message to all the peers
    void broadcast(byte[] message);

    // get all peers had been connected
    List<Peer> getPeers();

    // get all bootstraps
    List<Peer> getBootStraps();

    void use(PeerServerListener... peerServerListeners);

    void start();

    void load(Properties properties) throws PeerServerLoadException;

    Peer getSelf();
}
