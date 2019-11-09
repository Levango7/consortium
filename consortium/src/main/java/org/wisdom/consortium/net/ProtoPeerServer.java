package org.wisdom.consortium.net;

import org.wisdom.common.PeerServer;

public interface ProtoPeerServer extends PeerServer {
    GRpcClient getClient();
}
