package org.wisdom.consortium.net;

import com.google.common.base.Functions;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.consortium.Start;
import org.wisdom.consortium.proto.*;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
        GRpcClient client = server.getClient();
        PeersCache cache = client.peersCache;
        MessageBuilder builder = client.messageBuilder;
        context.keep();
        switch (context.message.getCode()) {
            case PING:
                context.channel.write(builder.buildPong());
                return;
            case LOOK_UP:
                context.channel.write(
                    builder.buildPeers(server.getPeers())
                );
                return;
            case PEERS:
                if(!config.isEnableDiscovery()) return;
                try {
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
        GRpcClient client = server.getClient();
        PeersCache cache = client.peersCache;
        MessageBuilder builder = client.messageBuilder;

        // keep self alive
        Start.APPLICATION_THREAD_POOL.execute(() -> {
            while (true){
                try{
                    TimeUnit.SECONDS.sleep(DISCOVERY_RATE);
                }catch (Exception ignored){}
                client.broadcast(
                        builder.buildPing()
                );
            }
        });
        Start.APPLICATION_THREAD_POOL.execute(() -> {
            while (true){
                try{
                    TimeUnit.SECONDS.sleep(DISCOVERY_RATE);
                }catch (Exception ignored){}
                lookup();
                cache.half();
                if(!config.isEnableDiscovery()) continue;
                pending.keySet()
                        .stream()
                        .filter(x -> !cache.has(x))
                        .limit(config.getMaxPeers())
                        .forEach(
                            p -> client.dial(p, builder.buildPing())
                        );
                pending.clear();
            }
        });
    }

    private void lookup(){
        GRpcClient client = server.getClient();
        PeersCache cache = client.peersCache;
        MessageBuilder builder = client.messageBuilder;

        if(!config.isEnableDiscovery()){
            // keep channel to bootstraps and trusted alive
            Stream.of(cache.bootstraps.keySet().stream(), cache.trusted.keySet().stream())
                    .flatMap(Functions.identity())
                    .filter(x -> !cache.has(x))
                    .forEach(x -> server.getClient().dial(x, builder.buildPing()));
            return;
        }
        if(cache.size() > 0){
            client.broadcast(builder.buildLookup());
            cache.trusted.keySet().forEach(p -> client.dial(p, builder.buildPing()));
            return;
        }
        Stream.of(cache.bootstraps, cache.trusted)
                .flatMap(x -> x.keySet().stream())
                .forEach(p -> client.dial(p, builder.buildLookup()));
    }

    @Override
    public void onNewPeer(PeerImpl peer, ProtoPeerServer server) {

    }

    @Override
    public void onDisconnect(PeerImpl peer, ProtoPeerServer server) {

    }
}
