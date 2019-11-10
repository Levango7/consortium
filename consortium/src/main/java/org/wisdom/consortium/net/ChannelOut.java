package org.wisdom.consortium.net;

import org.wisdom.consortium.proto.Message;

interface ChannelOut {
    void write(Message message);
    void close();
}
