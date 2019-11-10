package org.wisdom.consortium.net;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Optional;

public class WebSocketChannelBuilder implements ChannelBuilder{
    @Override
    public Optional<Channel> createChannel(String host, int port, Channel.ChannelListener... listeners) {
        return Optional.empty();
    }

    private static class Client extends WebSocketClient{
        public Client(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake ignored) {

        }

        @Override
        public void onMessage(String message) {

        }

        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }
}
