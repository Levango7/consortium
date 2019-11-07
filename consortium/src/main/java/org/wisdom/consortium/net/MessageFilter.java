package org.wisdom.consortium.net;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.wisdom.crypto.ed25519.Ed25519PublicKey;

@Slf4j
public class MessageFilter extends Plugin {
    private Cache<String, Boolean> cache;

    public MessageFilter(PeerServerConfig config) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(config.getMaxPeers() * 2).build();
    }

    @Override
    public void onMessage(ContextImpl context, ProtoPeerServer server) {
        System.out.println("==============================================================");
        // filter invalid signatures
        if (!new Ed25519PublicKey(context.getRemote().getID().getBytes()).verify(
                Util.getRawForSign(context.message), context.message.getSignature().toByteArray()
        )) {
            log.error("invalid signature received");
            context.exit();
            return;
        }
        // filter from your self
        if (context.getRemote().equals(server.getSelf())) {
            log.error("message received from yourself");
            context.exit();
            return;
        }
        // filter message which ttl < 0
        if (context.message.getTtl() < 0) {
            log.error("receive message ttl less than 0");
            context.exit();
            return;
        }
        String k = Hex.encodeHexString(context.message.getSignature().toByteArray());
        // filter message had been received
        if (cache.asMap().containsKey(k)) {
            context.exit();
        }
        cache.put(k, true);
    }

    @Override
    public void onStart(ProtoPeerServer server) {

    }
}
