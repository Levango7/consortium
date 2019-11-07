package org.wisdom.consortium.net;

import org.wisdom.consortium.proto.Message;

import java.io.Closeable;
import java.util.Optional;

// channel for message transports
public interface Channel extends Closeable {
    // write message to channel
    void write(Message message);

    // close the channel
    void close();

    // check whether the channel is closed
    boolean isClosed();

    interface ChannelListener {
        // triggered when channel is open, only once in the life cycle of the channel
        void onConnect(PeerImpl remote, Channel channel);

        // when new message received
        void onMessage(Message message, Channel channel);

        // when error
        void onError(Throwable throwable, Channel channel);
    }

    Optional<PeerImpl> getRemote();

    // bind listener to the channel
    void addListener(ChannelListener... listeners);
}
