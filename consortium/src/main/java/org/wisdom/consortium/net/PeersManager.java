package org.wisdom.consortium.net;


import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Peer;
import org.wisdom.common.PeerServer;
import org.wisdom.consortium.proto.Peers;
import org.wisdom.consortium.proto.Pong;

import java.util.stream.Collectors;


@Slf4j
public class PeersManager extends Plugin {
    private PeerServer server;

    @Override
    public void onMessage(ContextImpl context, ProtoPeerServer server) {
        switch (context.message.getCode()){
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

    //    @Override
//    public void onMessage(ContextImpl context, PeerServer server) {
//        switch (context.message) {
//            case PING:
//                onPing(context, server);
//                break;
//            case PONG:
//                context.keep();
//                break;
//            case LOOK_UP:
//                onLookup(context, server);
//                break;
//            case PEERS:
//                onPeers(context, server);
//        }
//    }
//
//    @Override
//    public void onStart(PeerServer server) {
//        this.server = server;
//    }
//
//    private void onPing(Context context, PeerServer server) {
//        context.response(PONG);
//        context.pend();
//    }
//
//    private void onPong(Context context, PeerServer server) {
//        context.keep();
//    }
//
//    private void onLookup(Context context, PeerServer server) {
//        List<String> peers = new ArrayList<>();
//        for (Peer p : server.getPeers()) {
//            peers.add(p.toString());
//        }
//        peers.add(server.getSelf().toString());
//        context.response(WisdomOuterClass.Peers.newBuilder().addAllPeers(peers).build());
//    }
//
//    private void onPeers(Context context, PeerServer server) {
//        try {
//            for (String p : context.getPayload().getPeers().getPeersList()) {
//                Peer pr = Peer.parse(p);
//                server.pend(pr);
//            }
//        } catch (Exception e) {
//            logger.error("parse peer fail");
//        }
//    }
//
//    public List<Peer> getPeers() {
//        return Optional.ofNullable(server)
//                .map(PeerServer::getPeers).orElse(new ArrayList<>());
//    }
//
//    public String getSelfAddress() {
//        return Optional.ofNullable(server)
//                .map(PeerServer::getSelf)
//                .map(Peer::toString).orElse("");
//    }

}
