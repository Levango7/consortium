package org.wisdom.consortium.net;

import lombok.Builder;
import org.wisdom.common.Peer;
import org.wisdom.consortium.proto.*;

@Builder
public class ContextImpl implements org.wisdom.common.Context {
    boolean keep;
    boolean relay;
    boolean exit;
    boolean disconnect;
    boolean block;
    PeerImpl remote;
    Message message;
    byte[] response;
    Channel channel;


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
    public void response(byte[] message) {
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
    public Peer getRemote() {
        return remote;
    }
}
