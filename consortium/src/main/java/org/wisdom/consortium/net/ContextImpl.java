package org.wisdom.consortium.net;

import lombok.Builder;
import org.wisdom.common.Peer;
import org.wisdom.common.Serializable;
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
    private Serializable response;
    private Nothing nothing;
    private Ping ping;
    private Pong pong;
    private Peers peers;
    private Lookup lookup;

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

    void response(Ping ping) {
        this.ping = ping;
    }

    void response(Pong pong) {
        this.pong = pong;
    }

    void response(Peers peers) {
        this.peers = peers;
    }

    void response(Lookup lookup) {
        this.lookup = lookup;
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
