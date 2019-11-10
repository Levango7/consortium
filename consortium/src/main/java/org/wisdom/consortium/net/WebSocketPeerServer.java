package org.wisdom.consortium.net;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.wisdom.consortium.proto.Message;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ServerEndpoint("/move")
public class WebSocketPeerServer extends AbstractPeerServer{
    @OnMessage
    public void onMessage(Session session, String message) {
        ProtoChannel ch = CHANNELS.get(session);
        if(ch == null) return;
        try {
            ch.message(Message.parseFrom(ByteString.copyFrom(message, StandardCharsets.UTF_8)));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    static final Map<Session, ProtoChannel> CHANNELS = new ConcurrentHashMap<>();

    @OnOpen
    public void open(Session session) {
        ProtoChannel ch = new ProtoChannel();
        ch.addListener(client, this);
        ch.setOut(new WebSocketChannelOut(session));
        CHANNELS.put(session, ch);
    }

    @OnError
    public void error(Session session, Throwable t) {
        ProtoChannel ch = CHANNELS.get(session);
        if(ch == null) return;
        ch.error(t);
    }

    @OnClose
    public void closedConnection(Session session) {
        ProtoChannel ch = CHANNELS.get(session);
        if(ch == null) return;
        ch.close();
    }

    @Override
    void startListening() {

    }
}