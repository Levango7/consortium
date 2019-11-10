package org.wisdom.consortium.net;

import lombok.AllArgsConstructor;
import org.wisdom.consortium.proto.Message;

import javax.websocket.Session;
import java.io.IOException;

@AllArgsConstructor
public class WebSocketChannelOut implements ChannelOut{
    private Session session;

    @Override
    public void write(Message message) {
        try {
            session.getBasicRemote()
                    .sendBinary(message.toByteString().asReadOnlyByteBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
