package org.wisdom.common;

public interface PeerServerListener {
    void onMessage(Context context, PeerServer server);

    void onStart(PeerServer server);

    void onNewPeer(Peer peer, PeerServer server);

    void onDisconnect(Peer peer, PeerServer server);
}
