package org.wisdom.consortium.net;

import org.wisdom.consortium.proto.Message;

import java.io.Closeable;

public interface Channel extends Closeable {
    boolean write(Message message);

    void close();

    boolean isClosed();

    interface ChannelListener {
        void onConnect(PeerImpl remote, Channel channel);

        void onMessage(Message message, Channel channel);
    }

    void addListener(ChannelListener... listeners);
}
