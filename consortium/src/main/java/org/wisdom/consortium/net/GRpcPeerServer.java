package org.wisdom.consortium.net;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.HexBytes;
import org.wisdom.common.Peer;
import org.wisdom.common.PeerServerListener;
import org.wisdom.common.Serializable;
import org.wisdom.consortium.proto.*;
import org.wisdom.exception.PeerServerLoadException;

import java.io.IOException;
import java.util.*;

@Slf4j
public class GRpcPeerServer extends EntryGrpc.EntryImplBase implements ProtoPeerServer, Channel.ChannelListener {
    private PeerServerConfig config;
    private List<PeerServerListener> listeners = new ArrayList<>();
    private Server server;
    private GRpcClient client;
    private PeerImpl self;

    public GRpcPeerServer() {

    }

    @Override
    public Peer getSelf() {
        return self;
    }

    public void setSelf(PeerImpl self) {
        this.self = self;
    }

    @Override
    public void dial(Peer peer, Serializable message) {
        client.dial(peer, client.buildMessage(1, message.getBytes()), this);
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
                self.encodeURI());
        log.info("your p2p secret address is " +
                String.format("%s://%s@%s:%d",
                        self.getProtocol(),
                        new HexBytes(
                                self.getPrivateKey().getEncoded(),
                                self.getPrivateKey().generatePublicKey().getEncoded())
                        ,
                        self.getHost(),
                        self.getPort()));
        listeners.forEach(l -> l.onStart(this));
        try {
            this.server = ServerBuilder.forPort(self.getPort()).addService(this).build().start();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (config.getBootstraps() == null) return;
        config.getBootstraps().forEach(x -> {
            client.dial(x.getHost(), x.getPort(), Ping.newBuilder().build(), this);
        });
    }

    @Override
    public void load(Properties properties) throws PeerServerLoadException {
        JavaPropsMapper mapper = new JavaPropsMapper();
        try {
            config = mapper.readPropertiesAs(properties, PeerServerConfig.class);
        } catch (Exception e) {
            String schema = "";
            try {
                schema = mapper.writeValueAsProperties(new PeerServerConfig()).toString();
            } catch (Exception ignored) {
            }
            throw new RuntimeException(
                    "load properties failed :" + properties.toString() + " expecting " + schema
            );
        }
        try {
            self = PeerImpl.create(config.getAddress());
        } catch (Exception e) {
            throw new PeerServerLoadException("failed to load peer server invalid address " + config.getAddress());
        }
        client = new GRpcClient(self);

        // loading plugins
        listeners.add(new MessageFilter(config));
        listeners.add(new PeersManager());
    }

    @Override
    public StreamObserver<Message> entry(
            //
            StreamObserver<Message> responseObserver
    ) {
        return client.createObserver(responseObserver, this);
    }

    @Override
    public void onConnect(PeerImpl remote, Channel channel) {

    }

    @Override
    public void onMessage(Message message, Channel channel) {
        Optional<PeerImpl> peer = PeerImpl.parse(message.getRemotePeer());
        if (!peer.isPresent()) {
            channel.close();
            throw new RuntimeException("failed to parse peer");
        }
        ContextImpl context = ContextImpl.builder()
                .message(message)
                .remote(peer.get()).build();

        for (PeerServerListener listener : listeners) {
            listener.onMessage(context, this);
        }
    }

    @Override
    public void onError(Throwable throwable, Channel channel) {

    }

    @Override
    public void dial(PeerImpl peer, Nothing nothing) {
        client.dial(peer, nothing, this);
    }

    @Override
    public void dial(PeerImpl peer, Ping ping) {
        client.dial(peer, ping, this);
    }

    @Override
    public void dial(PeerImpl peer, Pong ping) {
        client.dial(peer, ping, this);
    }

    @Override
    public void dial(PeerImpl peer, Lookup lookup) {
        client.dial(peer, lookup, this);
    }

    @Override
    public void dial(PeerImpl peer, Peers peers) {
        client.dial(peer, peers, this);
    }
}
