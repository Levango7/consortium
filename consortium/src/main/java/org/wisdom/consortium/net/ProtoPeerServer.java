package org.wisdom.consortium.net;

import com.google.protobuf.AbstractMessage;
import org.wisdom.common.PeerServer;
import org.wisdom.consortium.proto.*;

public interface ProtoPeerServer extends PeerServer {
    void dial(PeerImpl peer, Code code, long ttl, AbstractMessage message);
    void broadcast(Code code, long ttl, AbstractMessage message);
    GRpcClient getClient();
}
