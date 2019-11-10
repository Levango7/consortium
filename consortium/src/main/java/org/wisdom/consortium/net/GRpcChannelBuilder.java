package org.wisdom.consortium.net;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.AllArgsConstructor;
import org.wisdom.consortium.proto.EntryGrpc;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
public class GRpcChannelBuilder implements ChannelBuilder{
    MessageBuilder messageBuilder;

    @Override
    public Optional<Channel> createChannel(String host, int port, Channel.ChannelListener... listeners) {
        try {
            ManagedChannel ch = ManagedChannelBuilder
                    .forAddress(host, port).usePlaintext().build();
            EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
            ProtoChannel channel = new ProtoChannel();
            channel.addListener(
                    Arrays.stream(listeners)
                            .filter(Objects::nonNull)
                            .toArray(Channel.ChannelListener[]::new
                            )
            );
            channel.setOut(new GRpcChannelOut(stub.entry(
                    new ChannelWrapper(channel)
            )));
            channel.write(messageBuilder.buildPing());
            return Optional.of(channel);
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }
}
