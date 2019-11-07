package org.wisdom.consortium.net;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Peer;
import org.wisdom.consortium.proto.Code;
import org.wisdom.consortium.proto.Peers;
import org.wisdom.consortium.proto.Ping;
import org.wisdom.consortium.proto.Pong;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class PeersManager implements Plugin {
    private ProtoPeerServer server;
    private PeerServerConfig config;

    public PeersManager(PeerServerConfig config) {
        this.config = config;
    }

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
                try {
                    if (config.getMaxPeers() >= server.getPeerSet().size()) return;
                    Peers.parseFrom(context.message.getBody()).getPeersList().stream()
                            .map(PeerImpl::parse).filter(Optional::isPresent)
                            .map(Optional::get).limit(server.getPeerSet().size() - config.getMaxPeers())
                            .forEach(x -> server.dial(x, Code.PING, 1, Ping.newBuilder().build()));
                } catch (InvalidProtocolBufferException e) {
                    log.error("parse peers message failed");
                }
        }
    }

    @Override
    public void onStart(ProtoPeerServer server) {
        this.server = server;
    }

    @Override
    public void onNewPeer(PeerImpl peer, ProtoPeerServer server) {

    }

    @Override
    public void onDisconnect(PeerImpl peer, ProtoPeerServer server) {

    }
}
