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
        client.broadcast(Code.ANOTHER, config.getMaxTTL(), message.getBytes());
    }

    @Override
    public List<Peer> getBootStraps() {
        return new ArrayList<>(client.peersCache.bootstraps.keySet());
    }

    @Override
    public List<Peer> getPeers() {
        return client.peersCache.getPeers();
    }

    @Override
    public void use(PeerServerListener... peerServerListeners) {
        for(PeerServerListener listener: peerServerListeners){
            plugins.add(new PluginWrapper(listener));
        }
    }

    @Override
    public void start() {
        plugins.forEach(l -> l.onStart(this));
        try {
            this.server = ServerBuilder.forPort(self.getPort()).addService(this).build().start();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        if(!self.getHost().equals("localhost") && !self.getHost().equals("127.0.0.1")){
            return;
        }
        String externalIP = null;
        try{
            externalIP = Util.externalIp();
        }catch (Exception ignored){
            log.error("cannot get external ip, fall back to bind ip");
        }
        if (externalIP != null && Util.ping(externalIP, self.getPort())){
            log.info("ping " + externalIP + " success, set as your host");
            self.setHost(externalIP);
            return;
        }
        String bindIP = null;
        try{
            bindIP = Util.bindIp();
        }catch (Exception e){
            log.error("get bind ip failed");
        }
        if (bindIP != null){
            self.setHost(bindIP);
        }

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

        if (config.getBootstraps() == null) return;
        config.getBootstraps().forEach(x -> {
            client.dial(x.getHost(), x.getPort(), Code.PING, 1
                    , Ping.newBuilder().build().toByteArray()
                    , new Channel.ChannelListener() {
                        @Override
                        public void onConnect(PeerImpl remote, Channel channel) {
                            log.info("successfully connected to bootstrap node " + remote);
                            client.peersCache.bootstraps.put(remote, true);
                        }

                        @Override
                        public void onMessage(Message message, Channel channel) {

                        }

                        @Override
                        public void onError(Throwable throwable, Channel channel) {

                        }

                        @Override
                        public void onClose(Channel channel) {

                        }
                    }
            );
        });
    }

    @Override
    public void load(Properties properties) throws PeerServerLoadException {
        JavaPropsMapper mapper = new JavaPropsMapper();
        try {
            config = mapper.readPropertiesAs(properties, PeerServerConfig.class);
            if(config.getMaxTTL() <= 0) config.setMaxTTL(PeerServerConfig.DEFAULT_MAX_TTL);
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
        client = new GRpcClient(self, config).withListener(this);

        // loading plugins
        plugins.add(new MessageFilter(config));
        plugins.add(new PeersManager(config));
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
        for (Plugin plugin : plugins) {
            plugin.onNewPeer(remote, this);
        }
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        Optional<PeerImpl> peer = channel.getRemote();
        if (!peer.isPresent()) {
            channel.close();
            throw new RuntimeException("failed to parse peer");
        }
        ContextImpl context = ContextImpl.builder()
                .channel(channel)
                .message(message)
                .remote(peer.get()).build();

        for (Plugin plugin : plugins) {
            plugin.onMessage(context, this);
            if(context.block){
                client.peersCache.block(peer.get());
                return;
            }
            if(context.disconnect){
                channel.close();
                client.peersCache.remove(peer.get());
                return;
            }
            if(context.exit){
                return;
            }
            if(context.keep){
                client.peersCache.keep(peer.get(), channel);
            }
            if(context.relay){
                client.relay(context.message, peer.get());
            }
            if(context.response != null){
                channel.write(client.buildMessage(Code.ANOTHER, 1, context.response.getBytes()));
                context.response = null;
            }
        }
    }

    @Override
    public void onError(Throwable throwable, Channel channel) {

    }

    @Override
    public void onClose(Channel channel) {
        if(channel.getRemote().isPresent()){
            for (Plugin plugin : plugins) {
                plugin.onDisconnect(channel.getRemote().get(), this);
            }
        }
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
    public GRpcClient getClient() {
        return client;
    }
}
