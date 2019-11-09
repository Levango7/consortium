package org.wisdom.consortium.net;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Peer;
import org.wisdom.consortium.Start;
import org.wisdom.consortium.proto.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
// plugin for peers join/remove management
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
                        client.messageBuilder.buildMessage(
                                Code.PONG, 1, context.message.getBody().toByteArray()
                        )
                );
                return;
            case LOOK_UP:
                Peers peers = Peers.newBuilder().addAllPeers(
                        server.getPeers().stream().map(Peer::encodeURI)
                                .collect(Collectors.toSet())
                ).build();
                context.channel.write(
                    client.messageBuilder.buildMessage(Code.PEERS, 1, peers.toByteArray())
                );
                return;
            case PEERS:
                if(!config.isEnableDiscovery()) return;
                try {
                    if (cache.size() >= config.getMaxPeers()) return;
                    Peers.parseFrom(context.message.getBody()).getPeersList().stream()
                            .map(PeerImpl::parse)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(x -> !cache.has(x) && !x.equals(server.getSelf()))
                            .forEach(x -> pending.put(x, true));
                } catch (InvalidProtocolBufferException e) {
                    log.error("parse peers message failed");
                }
        }
    }

    @Override
    public void onStart(ProtoPeerServer server) {
        this.server = server;
        PeersCache cache = server.getClient().peersCache;
        if(!config.isEnableDiscovery()) return;
        GRpcClient client = server.getClient();
        Start.APPLICATION_THREAD_POOL.execute(() -> {
            while (true){
                if(cache.isFull()) continue;
                List<Channel> channels = server.getClient().peersCache.getChannels().collect(Collectors.toList());
                if(channels.size() == 0){
                    channels = cache.bootstraps.keySet()
                            .stream().map(p -> server.getClient().createChannel(p.getHost(), p.getPort()))
                            .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                }
                channels.forEach(
                        c -> c.write(
                                server.getClient().messageBuilder.buildMessage(
                                        Code.LOOK_UP, 1, Lookup.newBuilder().build().toByteArray()
                                )
                        )
                );
                pending.keySet().stream()
                        .filter(x -> !cache.has(x))
                        .limit(config.getMaxPeers())
                        .forEach(
                                x -> client.dial(
                                        x,  client.messageBuilder.buildMessage(
                                                Code.PING, 1, Ping.newBuilder().build().toByteArray()
                                ))
                        );

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
