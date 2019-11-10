package org.wisdom.consortium.net;

import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.wisdom.consortium.proto.Message;

@AllArgsConstructor
public class GRpcChannelOut implements ChannelOut{
    private StreamObserver<Message> out;

    @Override
    public void write(Message message) {
        out.onNext(message);
    }

    @Override
    public void close() {
        out.onCompleted();
    }
}
