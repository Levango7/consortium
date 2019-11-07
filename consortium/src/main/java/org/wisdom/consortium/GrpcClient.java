package org.wisdom.consortium;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.wisdom.consortium.proto.EntryGrpc;
import org.wisdom.consortium.proto.Message;

import java.util.concurrent.TimeUnit;

public class GrpcClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel ch = ManagedChannelBuilder.forAddress("localhost", 9999).usePlaintext().build();
        EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
        StreamObserver<Message> observer = stub.entry(new StreamObserver<Message>() {
            @Override
            public void onNext(Message message) {
//                System.out.println("=========");
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        });
        while (true){
            TimeUnit.SECONDS.sleep(1);
            observer.onNext(Message.newBuilder().build());
        }
    }
}
