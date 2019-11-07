package org.wisdom.consortium.net;

import org.wisdom.common.PeerServer;
import org.wisdom.consortium.proto.*;

public interface ProtoPeerServer extends PeerServer {
    void dial(PeerImpl peer, Nothing nothing);
    void dial(PeerImpl peer, Ping ping);
    void dial(PeerImpl peer, Pong ping);
    void dial(PeerImpl peer, Lookup lookup);
    void dial(PeerImpl peer, Peers peers);
}
