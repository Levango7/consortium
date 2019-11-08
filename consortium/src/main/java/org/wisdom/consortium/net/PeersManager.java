package org.wisdom.consortium.net;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Peer;
import org.wisdom.consortium.Start;
import org.wisdom.consortium.proto.Code;
import org.wisdom.consortium.proto.Peers;
import org.wisdom.consortium.proto.Ping;
import org.wisdom.consortium.proto.Pong;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class PeersManager implements Plugin {
    private ProtoPeerServer server;
    private PeerServerConfig config;
    private ConcurrentHashMap<PeerImpl, Boolean> pending = new ConcurrentHashMap<>();
    private static final int DISCOVERY_RATE = 15;

    public PeersManager(PeerServerConfig config) {
        this.config = config;
    }

    @Override
    public void onMessage(ContextImpl context, ProtoPeerServer server) {
        PeersCache cache = server.getClient().peersCache;
        GRpcClient client = server.getClient();
        switch (context.message.getCode()) {
            case PING:
                context.channel.write(
                        client.buildMessage(
                                Code.PONG, 1, Pong.newBuilder().build().toByteArray()
                        )
                );
                return;
            case LOOK_UP:
                Peers peers = Peers.newBuilder().addAllPeers(
                        server.getPeers().stream().map(Peer::encodeURI)
                                .collect(Collectors.toSet())
                ).build();
                context.channel.write(
                    client.buildMessage(Code.PEERS, 1, peers.toByteArray())
                );
                return;
            case PEERS:
                if(!config.isEnableDiscovery()) return;
                try {
                    if (config.getMaxPeers() >= cache.size()) return;
                    Peers.parseFrom(context.message.getBody()).getPeersList().stream()
                            .map(PeerImpl::parse)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(x -> !cache.has(x))
                            .forEach(x -> pending.put(x, true));
                } catch (InvalidProtocolBufferException e) {
                    log.error("parse peers message failed");
                }
                return;
        }
    }

    @Override
    public void onStart(ProtoPeerServer server) {
        this.server = server;
        PeersCache cache = server.getClient().peersCache;
        if(!config.isEnableDiscovery()) return;
        Start.APPLICATION_THREAD_POOL.execute(() -> {
            while (true){
                if(cache.isFull()) continue;
                server.broadcast(Code.PEERS, 1, Peers.newBuilder().build());
                pending.keySet().stream()
                        .filter(x -> !cache.has(x))
                        .limit(config.getMaxPeers())
                        .forEach(x -> server.dial(x, Code.PING, 1, Ping.newBuilder().build()));

                pending.clear();
                cache.half();
                try{
                    TimeUnit.SECONDS.sleep(DISCOVERY_RATE);
                }catch (Exception ignored){}
            }
        });
    }

    @Override
    public void onNewPeer(PeerImpl peer, ProtoPeerServer server) {

    }

    @Override
    public void onDisconnect(PeerImpl peer, ProtoPeerServer server) {

    }
}
