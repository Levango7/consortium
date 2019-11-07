package org.wisdom.consortium.net;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Peer;
import org.wisdom.common.PeerServer;
import org.wisdom.common.PeerServerListener;
import org.wisdom.common.Serializable;
import org.wisdom.consortium.proto.EntryGrpc;
import org.wisdom.consortium.proto.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GRpcPeerServer extends EntryGrpc.EntryImplBase implements PeerServer {
    private PeerServerConfig config;
    private List<PeerServerListener> listeners = new ArrayList<>();
    private Server server;
    private GRpcClient client;
    private org.wisdom.consortium.net.Peer self;
    private ConcurrentHashMap<String, StreamObserver<Message>> channels;

    public GRpcPeerServer() {
        client = new GRpcClient();
    }

    @Override
    public void dial(Peer peer, Serializable message) {

    }

    @Override
    public void broadcast(Serializable message) {

    }

    @Override
    public List<Peer> getPeers() {
        return null;
    }

    @Override
    public void use(PeerServerListener... peerServerListeners) {
        this.listeners.addAll(Arrays.asList(peerServerListeners));
    }

    @Override
    public void start() {
        log.info("peer server is listening on " +
                config.getProtocol() + "://" +
                config.getAddress() + ":" + config.getPort());

        listeners.forEach(l -> l.onStart(this));
        try {
            this.server = ServerBuilder.forPort(config.getPort()).addService(this).build().start();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void load(Properties properties) {
        JavaPropsMapper mapper = new JavaPropsMapper();
        try{
            config = mapper.readPropertiesAs(properties, PeerServerConfig.class);
        }catch (Exception e){
            String schema = "";
            try{
                schema = mapper.writeValueAsProperties(new PeerServerConfig()).toString();
            }catch (Exception ignored){}
            throw new RuntimeException(
                    "load properties failed :" + properties.toString() + " expecting " + schema
            );
        }
        if (config.getPort() == 0) config.setPort(PeerServerConfig.DEFAULT_PORT);
        if (config.getProtocol() == null || config.getProtocol().equals("")) {
            config.setProtocol(PeerServerConfig.DEFAULT_PROTOCOL);
        }
    }

    @Override
    public StreamObserver<Message> entry(
            //
            StreamObserver<Message> responseObserver
    ) {
        PeerChannel channel = new PeerChannel();
        channel.addListener(client);
        channel.setOut(responseObserver);
        return channel;
    }
}
