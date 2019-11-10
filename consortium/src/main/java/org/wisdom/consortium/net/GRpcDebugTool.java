package org.wisdom.consortium.net;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Context;
import org.wisdom.common.Peer;
import org.wisdom.common.PeerServer;
import org.wisdom.common.PeerServerListener;
import org.wisdom.consortium.PeerServerProperties;
import org.wisdom.consortium.proto.Code;
import org.wisdom.consortium.proto.Ping;

import java.nio.charset.StandardCharsets;
import java.util.*;


@Slf4j
public class GRpcDebugTool {

    public static void main(String... args) throws Exception{
        io.netty.util.internal.logging.InternalLoggerFactory.setDefaultFactory(new InternalLoggerFactory() {
            @Override
            protected InternalLogger newInstance(String name) {
                return new Nop();
            }
        });
        // port listening on
        int port = Integer.parseInt(System.getenv("X_PORT"));
        Cache<String, Boolean> cache = CacheBuilder.newBuilder().maximumSize(16).build();
        GRpcPeerServer server = new GRpcPeerServer();
        Properties properties = new PeerServerProperties();
        properties.setProperty("address", "node://localhost:" + port);
        if(System.getenv("X_BOOTSTRAPS") != null){
            String[] bootstraps = System.getenv("X_BOOTSTRAPS").split(",");
            for(int i = 1; i < bootstraps.length + 1; i++){
                properties.setProperty("bootstraps." + i, bootstraps[i-1]);
            }
        }
        if(System.getenv("X_TRUSTED") != null){
            String[] trusted = System.getenv("X_TRUSTED").split(",");
            for(int i = 1; i < trusted.length + 1; i++){
                properties.setProperty("trusted." + i, trusted[i-1]);
            }
        }
        properties.setProperty("max-peers", "32");
        properties.setProperty(
                "enable-discovery",
                Optional.ofNullable(System.getenv("X_ENABLE_DISCOVERY")).orElse("false")
        );
        server.load(properties);
        server.use(new PeerServerListener() {
            @Override
            public void onMessage(Context context, PeerServer server) {
                String m = new String(context.getMessage(), StandardCharsets.UTF_8).trim();
                if(m.equals("disconnect")){
                    log.info("disconnect to remote " + context.getRemote());
                    context.disconnect();
                }
                if(m.equals("block")){
                    context.block();
                }
                if(m.equals("keep")){
                    context.keep();
                }
                if(m.startsWith("relay")){
                    context.relay();
                }
                log.info("remote = " + context.getRemote() + " message = " + m);
            }

            @Override
            public void onStart(PeerServer server) {

            }

            @Override
            public void onNewPeer(Peer peer, PeerServer server) {
                log.info("connected to " + peer + " success");
            }

            @Override
            public void onDisconnect(Peer peer, PeerServer server) {
                log.info("channel to " + peer + " closed");
            }
        });
        server.start();
        Scanner scanner = new Scanner(System.in);
        while(true){
            String line = scanner.nextLine().trim();
            if(line.equals("peers")){
                server.getPeers().forEach(x -> {
                    System.out.println(x.encodeURI() + " " + ((PeerImpl) x).score);
                });
                server.getClient().peersCache.blocked.keySet().forEach(x -> {
                    System.out.println(x.encodeURI() + " " + x.score);
                });
                continue;
            }
            if(line.equals("clear")){
                for(int i = 0; i < 2000; i++) System.out.println();
                continue;
            }
            if(line.equals("self")){
                System.out.println(server.getSelf());
                continue;
            }
            if(line.equals("trusted")){
                server.getClient().peersCache.trusted.keySet().forEach(System.out::println);
                continue;
            }
            if(line.startsWith("connect")){
                String[] hostPort = line.substring("connect".length()).trim()
                        .split("\\s|:");
                server.getClient().dial(hostPort[0], Integer.parseInt(hostPort[1]),
                        server.getClient()
                        .messageBuilder
                        .buildPing());
                continue;
            }
            if(line.equals("bootstraps")){
                server.getBootStraps().forEach(System.out::println);
                continue;
            }
            if(line.startsWith("broadcast")){
                server.broadcast(line.substring("broadcast".length())
                        .trim().getBytes(StandardCharsets.UTF_8));
                continue;
            }
            List<String> arguments = Arrays.asList(line.split("\\s"));
            if(arguments.size() == 0) continue;
            for(Peer p: server.getPeers()){
                if(p.getID().toString().startsWith(arguments.get(0))){
                    server.dial(p,
                            String.join(" ", arguments.subList(1, arguments.size()))
                                    .getBytes(StandardCharsets.UTF_8)
                    );
                }
            }
        }
    }

    private static final class  Nop implements InternalLogger{
        @Override
        public String name() {
            return null;
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public void trace(String msg) {

        }

        @Override
        public void trace(String format, Object arg) {

        }

        @Override
        public void trace(String format, Object argA, Object argB) {

        }

        @Override
        public void trace(String format, Object... arguments) {

        }

        @Override
        public void trace(String msg, Throwable t) {

        }

        @Override
        public void trace(Throwable t) {

        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void debug(String msg) {

        }

        @Override
        public void debug(String format, Object arg) {

        }

        @Override
        public void debug(String format, Object argA, Object argB) {

        }

        @Override
        public void debug(String format, Object... arguments) {

        }

        @Override
        public void debug(String msg, Throwable t) {

        }

        @Override
        public void debug(Throwable t) {

        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public void info(String msg) {

        }

        @Override
        public void info(String format, Object arg) {

        }

        @Override
        public void info(String format, Object argA, Object argB) {

        }

        @Override
        public void info(String format, Object... arguments) {

        }

        @Override
        public void info(String msg, Throwable t) {

        }

        @Override
        public void info(Throwable t) {

        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void warn(String msg) {

        }

        @Override
        public void warn(String format, Object arg) {

        }

        @Override
        public void warn(String format, Object... arguments) {

        }

        @Override
        public void warn(String format, Object argA, Object argB) {

        }

        @Override
        public void warn(String msg, Throwable t) {

        }

        @Override
        public void warn(Throwable t) {

        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public void error(String msg) {

        }

        @Override
        public void error(String format, Object arg) {

        }

        @Override
        public void error(String format, Object argA, Object argB) {

        }

        @Override
        public void error(String format, Object... arguments) {

        }

        @Override
        public void error(String msg, Throwable t) {

        }

        @Override
        public void error(Throwable t) {

        }

        @Override
        public boolean isEnabled(InternalLogLevel level) {
            return false;
        }

        @Override
        public void log(InternalLogLevel level, String msg) {

        }

        @Override
        public void log(InternalLogLevel level, String format, Object arg) {

        }

        @Override
        public void log(InternalLogLevel level, String format, Object argA, Object argB) {

        }

        @Override
        public void log(InternalLogLevel level, String format, Object... arguments) {

        }

        @Override
        public void log(InternalLogLevel level, String msg, Throwable t) {

        }

        @Override
        public void log(InternalLogLevel level, Throwable t) {

        }
    }
}
