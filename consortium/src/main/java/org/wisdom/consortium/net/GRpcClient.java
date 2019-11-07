package org.wisdom.consortium.net;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.wisdom.consortium.proto.EntryGrpc;
import org.wisdom.consortium.proto.Message;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GRpcClient implements Plugin{
    private ConcurrentHashMap<Peer, Channel> channels;

    void dial(String host, int port, Message message){

    }

    void dial(Peer peer, Message message) {
        if (channels.containsKey(peer) && !channels.get(peer).isClosed()) {
            channels.get(peer).write(message);
        }
        try{
            Channel ch = openChannel(peer.getHost(), peer.getPort());
            ch.write(message);
        }catch (Exception e){
            log.error("cannot connect to peer " + peer);
        }
    }

    @Override
    public void onMessage(Context context) {

    }

    @Override
    public void onClose(Peer remote) {
    }

    public Channel openChannel(String host, int port){
        ManagedChannel ch = ManagedChannelBuilder
                .forAddress(host, port).usePlaintext().build();
        EntryGrpc.EntryStub stub = EntryGrpc.newStub(ch);
        Channel channel = new Channel(this);
        channel.setOut(stub.entry(channel));
        return channel;
    }
}
