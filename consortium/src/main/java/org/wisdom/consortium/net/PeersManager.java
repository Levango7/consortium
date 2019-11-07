package org.wisdom.consortium.net;

import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Peer;
import org.wisdom.common.PeerServer;
import org.wisdom.consortium.proto.Peers;
import org.wisdom.consortium.proto.Pong;

import java.util.stream.Collectors;

@Slf4j
public class PeersManager implements Plugin {
    private PeerServer server;

    @Override
    public void onMessage(ContextImpl context, ProtoPeerServer server) {
        switch (context.message.getCode()) {
            case PING:
                context.response(Pong.newBuilder().build());
                return;
            case PONG:
                context.keep();
                return;
            case LOOK_UP:
                context.response(
                        Peers.newBuilder().addAllPeers(
                                server.getPeers().stream().map(Peer::encodeURI)
                                        .collect(Collectors.toSet())
                        ).build()
                );
                return;
            case PEERS:
                return;
        }
    }

    @Override
    public void onStart(ProtoPeerServer server) {

    }

    @Override
    public void onNewPeer(PeerImpl peer, ProtoPeerServer server) {

    }

    @Override
    public void onDisconnect(PeerImpl peer, ProtoPeerServer server) {

    }
}
