package org.wisdom.consortium.net;

import lombok.extern.slf4j.Slf4j;
import org.wisdom.consortium.proto.Code;
import org.wisdom.consortium.proto.Message;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
public class TestClient {

    public static void main(String... args) throws Exception{
        PeerImpl self = PeerImpl.create("node://localhost:1789");
        GRpcClient client = new GRpcClient(self, PeerServerConfig.builder().maxPeers(16).build())
                .withListener(new Channel.ChannelListener() {
                    @Override
                    public void onConnect(PeerImpl remote, Channel channel) {
                        log.info("connected to " + remote + " success");
                    }

                    @Override
                    public void onMessage(Message message, Channel channel) {
                        log.info("received message = \n" + message);
                    }

                    @Override
                    public void onError(Throwable throwable, Channel channel) {
                        log.error(throwable.getMessage());
                    }

                    @Override
                    public void onClose(Channel channel) {
                        log.info("channel to " + channel.getRemote().get() + " closed");
                    }
                });
        Channel ch = client.dial("localhost", 9998, Code.ANOTHER, 1, "".getBytes(StandardCharsets.UTF_8));;
        Scanner scanner = new Scanner(System.in);
        while(true){
            String line = scanner.nextLine();
            ch.write(client.buildMessage(Code.PING, 1, line.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
