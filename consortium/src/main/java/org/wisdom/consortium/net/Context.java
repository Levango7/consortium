package org.wisdom.consortium.net;

import lombok.Builder;
import org.wisdom.common.Serializable;
import org.wisdom.consortium.proto.Message;

@Builder
public class Context implements org.wisdom.common.Context {
    boolean keep;
    boolean relay;
    boolean exit;
    boolean disconnect;
    boolean block;
    Peer remote;
    Message message;
    private Serializable response;

    @Override
    public void exit() {
        exit = true;
    }

    @Override
    public void disconnect() {
        disconnect = true;
    }

    @Override
    public void block() {
        block = true;
    }

    @Override
    public void keep() {
        keep = true;
    }

    @Override
    public void response(Serializable message) {
        response = message;
    }

    @Override
    public void relay() {
        relay = true;
    }

    @Override
    public byte[] getMessage() {
        return message.getBody().toByteArray();
    }

    @Override
    public org.wisdom.common.Peer getRemote() {
        return remote;
    }
}
