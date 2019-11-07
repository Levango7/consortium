package org.wisdom.consortium.net;

import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.google.protobuf.AbstractMessage;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.*;
import org.wisdom.consortium.proto.*;
import org.wisdom.exception.PeerServerLoadException;

import java.io.IOException;
import java.util.*;

@Slf4j
public class GRpcPeerServer extends EntryGrpc.EntryImplBase implements Channel.ChannelListener, ProtoPeerServer {
    private PeerServerConfig config;
    private List<Plugin> plugins = new ArrayList<>();
    private Server server;
    private GRpcClient client;
    private PeerImpl self;

    public GRpcPeerServer() {

    }

    @Override
    public Peer getSelf() {
        return self;
    }

    @Override
    public void dial(Peer peer, Serializable message) {
        client.dial(peer, Code.ANOTHER, 1, message.getBytes());
    }

    @Override
    public void broadcast(Serializable message) {
        for(Channel ch: client.channels.values()){
            ch.write(client.buildMessage(Code.ANOTHER, config.getMaxTTL(), message.getBytes()));
        }
    }

    @Override
    public List<Peer> getPeers() {
        return Arrays.asList(client.channels.keySet().toArray(new Peer[]{}));
    }

    @Override
    public void use(PeerServerListener... peerServerListeners) {
        for(PeerServerListener listener: peerServerListeners){
            plugins.add(new PluginWrapper(listener));
        }
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
        plugins.forEach(l -> l.onStart(this));
        try {
            this.server = ServerBuilder.forPort(self.getPort()).addService(this).build().start();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (config.getBootstraps() == null) return;
        config.getBootstraps().forEach(x -> {
            client.dial(x.getHost(), x.getPort(), Code.PING,1
                    , Ping.newBuilder().build().toByteArray());
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
        client = new GRpcClient(self).withListener(this);

        // loading plugins
        plugins.add(new MessageFilter(config));
        plugins.add(new PeersManager());
    }

    @Override
    public StreamObserver<Message> entry(
            //
            StreamObserver<Message> responseObserver
    ) {
        return client.createObserver(responseObserver);
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

        for (Plugin plugin : plugins) {
            plugin.onMessage(context, this);
        }
    }

    @Override
    public void onError(Throwable throwable, Channel channel) {

    }

    @Override
    public void onClose(Channel channel) {

    }

    @Override
    public void dial(PeerImpl peer, Code code, long ttl, AbstractMessage message) {
        client.dial(peer,code, ttl, message.toByteArray());
    }

    @Override
    public void broadcast(Code code, long ttl, AbstractMessage message) {
        client.broadcast(code, ttl, message.toByteArray());
    }

    @Override
    public Set<Peer> getPeerSet() {
        return client.channels.keySet();
    }
}
