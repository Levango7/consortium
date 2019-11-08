package org.wisdom.consortium.net;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.common.Context;
import org.wisdom.common.Peer;
import org.wisdom.common.PeerServer;
import org.wisdom.common.PeerServerListener;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


@Slf4j
public class TestClient {

    public static void main(String... args) throws Exception{
        Logger.getLogger("io.grpc").setLevel(Level.INFO);
        int port = Integer.parseInt(System.getenv("X_PORT"));
        Cache<String, Boolean> cache = CacheBuilder.newBuilder().maximumSize(16).build();
        GRpcPeerServer server = new GRpcPeerServer();
        Properties properties = new Properties();
        properties.setProperty("address", "node://localhost:" + port);
        properties.setProperty("bootstraps.1", "node://localhost:30569");
        properties.setProperty("enable-discovery", "true");
        server.load(properties);
        server.use(new PeerServerListener() {
            @Override
            public void onMessage(Context context, PeerServer server) {
                String m = new String(context.getMessage(), StandardCharsets.UTF_8);
                if(cache.asMap().containsKey(m)) return;
                cache.asMap().put(m, true);
                context.relay();
                System.out.println(m);
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
            String line = scanner.nextLine();
            if(line.equals("peers")){
                server.getPeers().forEach(System.out::println);
            }
            server.broadcast(line.getBytes(StandardCharsets.UTF_8));
        }
    }
}
