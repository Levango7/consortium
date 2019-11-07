package org.wisdom.consortium.net;

import org.wisdom.common.Context;
import org.wisdom.common.Peer;
import org.wisdom.common.PeerServer;
import org.wisdom.common.PeerServerListener;

public abstract class Plugin implements PeerServerListener {
    @Override
    public void onMessage(Context context, PeerServer server) {

    }

    @Override
    public void onStart(PeerServer server) {

    }

    @Override
    public void onNewPeer(Peer peer, PeerServer server) {

    }

    @Override
    public void onDisconnect(Peer peer, PeerServer server) {

    }

    abstract void onMessage(ContextImpl context, ProtoPeerServer server);

    abstract void onStart(ProtoPeerServer server);
}
