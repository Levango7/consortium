package org.wisdom.consortium.net;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.consortium.proto.Message;

import java.util.Optional;

@Slf4j
public class Channel implements StreamObserver<Message> {
    private boolean closed;
    private Peer remote;
    private StreamObserver<Message> out;
    private boolean pinged;
    private Plugin handler;

    public Channel(Plugin handler) {
        this.handler = handler;
    }

    public void setOut(StreamObserver<Message> out) {
        this.out = out;
    }

    @Override
    public void onNext(Message message) {
        handlePing(message);
        Optional<Peer> o = Peer.parse(message.getRemotePeer());
        if (!o.isPresent()) {
            log.error("cannot parse remote peer");
            return;
        }
        Context ctx = Context.builder().remote(o.get())
                .message(message).build();
        handler.onMessage(ctx);
    }

    private void handlePing(Message message) {
        if (pinged) return;
        Optional<Peer> o = Peer.parse(message.getRemotePeer());
        if (!o.isPresent()) {
            log.error("cannot parse remote peer");
            close();
            return;
        }
        if (remote != null && !remote.equals(o.get())) {
            log.error("peer not equals");
            close();
            return;
        }
        pinged = true;
        remote = o.get();
    }

    @Override
    public void onError(Throwable throwable) {
        close();
    }

    @Override
    public void onCompleted() {
        close();
    }

    public void close() {
        out.onCompleted();
        closed = true;
        handler.onClose(remote);
    }

    public void write(Message message) {
        if (closed) {
            log.error("the channel is closed");
        }
        try {
            out.onNext(message);
        } catch (Exception e) {
            close();
        }
    }

    public Peer getRemote() {
        return remote;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setRemote(Peer remote) {
        this.remote = remote;
    }
}
