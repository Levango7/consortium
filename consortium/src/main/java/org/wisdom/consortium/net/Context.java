package org.wisdom.consortium.net;

import lombok.Builder;
import org.wisdom.common.Peer;
import org.wisdom.common.Serializable;

@Builder
public class Context implements org.wisdom.common.Context {
    private boolean exit;
    private boolean disconnect;
    private boolean block;
    private boolean keep;
    private boolean relay;
    private Peer remote;
    private byte[] message;
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
        return message;
    }

    @Override
    public Peer getRemote() {
        return remote;
    }
}
