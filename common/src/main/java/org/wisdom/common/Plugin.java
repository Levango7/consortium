package org.wisdom.common;

public interface Plugin {
    void onMessage(byte[] message, PeerServer server);

    void onStart(PeerServer server);

    void onNewPeer(Peer peer, PeerServer server);

    void onDisconnect(Peer peer, PeerServer server);
}
