package org.wisdom.consortium.net;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.consortium.proto.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


// communicating channel with peer
@Slf4j
public class PeerChannel implements StreamObserver<Message>, Channel {
    private boolean closed;
    private PeerImpl remote;
    private StreamObserver<Message> out;
    private boolean pinged;
    private List<ChannelListener> listeners = new ArrayList<>();

    public PeerChannel() {
    }

    public void setOut(StreamObserver<Message> out) {
        this.out = out;
    }

    @Override
    public void onNext(Message message) {
        if(closed) return;
        handlePing(message);
        listeners.forEach(l -> l.onMessage(message, this));
    }

    private void handlePing(Message message) {
        if (pinged) return;
        Optional<PeerImpl> o = PeerImpl.parse(message.getRemotePeer());
        if (!o.isPresent()) {
            log.error("cannot parse remote peer");
            close();
            return;
        }
        pinged = true;
        remote = o.get();
        listeners.forEach(l -> l.onConnect(remote, this));
    }

    @Override
    public void onError(Throwable throwable) {
        listeners.forEach(l -> l.onError(throwable, this));
        close();
    }

    @Override
    public void onCompleted() {
        close();
    }

    public void close() {
        if(closed) return;
        listeners.forEach(l -> l.onClose(this));
        out.onCompleted();
        closed = true;
    }

    public void write(Message message) {
        if (closed) {
            log.error("the channel is closed");
            return;
        }
        try {
            out.onNext(message);
        } catch (Throwable e) {
            listeners.forEach(l -> l.onError(e, this));
            close();
        }
    }

    public Optional<PeerImpl> getRemote() {
        return Optional.ofNullable(remote);
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void addListener(ChannelListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }
}
