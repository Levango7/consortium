package org.wisdom.consortium.net;

import lombok.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.wisdom.common.HexBytes;
import org.wisdom.crypto.KeyPair;
import org.wisdom.crypto.ed25519.Ed25519;
import org.wisdom.crypto.ed25519.Ed25519PrivateKey;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Peer implements org.wisdom.common.Peer {
    private static final int PUBLIC_KEY_LENGTH = 32;
    private static final int PRIVATE_KEY_LENGTH = 64;

    private String protocol;
    private String host;
    private int port;
    private HexBytes ID;
    private Ed25519PrivateKey privateKey;

    public String toString() {
        return String.format("%s://%s@%s:%d", protocol, ID, host, port);
    }

    public static Optional<Peer> parse(String url) {
        URI u;
        try {
            u = new URI(url.trim());
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
        Peer p = new Peer();
        String scheme = u.getScheme();
        p.protocol = (scheme == null || scheme.equals("")) ? PeerServerConfig.DEFAULT_PROTOCOL : scheme;
        p.port = u.getPort();
        if (p.port <= 0) {
            p.port = PeerServerConfig.DEFAULT_PORT;
        }
        p.host = u.getHost();
        System.out.println(u.getRawAuthority());
        System.out.println(u.getRawUserInfo());
        if (u.getRawUserInfo() == null || u.getRawUserInfo().equals("")) return Optional.empty();

        try {
            p.ID = new HexBytes(u.getRawUserInfo());
        } catch (DecoderException e) {
            return Optional.empty();
        }
        if (p.ID.size() != PRIVATE_KEY_LENGTH && p.ID.size() != PUBLIC_KEY_LENGTH) {
            return Optional.empty();
        }
        if (p.ID.size() == PRIVATE_KEY_LENGTH) {
            p.privateKey = new Ed25519PrivateKey(p.ID.slice(0, 32).getBytes());
            p.ID = p.ID.slice(32, p.ID.size());
        }
        return Optional.of(p);
    }

    // create self as peer from input
    public static Peer create(String url) throws Exception {
        URI u = new URI(url);
        String scheme = u.getScheme();
        scheme = (scheme == null || scheme.equals("")) ? PeerServerConfig.DEFAULT_PROTOCOL : scheme;
        int port = u.getPort();
        port = port <= 0 ? PeerServerConfig.DEFAULT_PORT : port;
        if (u.getRawUserInfo() == null || u.getRawUserInfo().equals("")) {
            KeyPair kp = Ed25519.generateKeyPair();
            url = String.format("%s://%s@%s:%d", scheme,
                    Hex.encodeHexString(kp.getPrivateKey().getEncoded()) + Hex.encodeHexString(kp.getPublicKey().getEncoded()),
                    u.getHost(), port
            );
        }

        String errorMsg = "failed to parse url " + url;
        return Peer.parse(url).orElseThrow(() -> new Exception(errorMsg));
    }

    public static void main(String[] args) throws Exception {
        Peer p = Peer.parse("wisdom://f43d5ab89d1705cc02131ffe18137e60e0d35e0569cb334f61ca6db7db4c964716d4b57a3de0a6adcf0bc9e3c8da39870bdabc1027fa05b8e25f36484afddfd9@192.168.1.142:9589").get();
        System.out.println(p);
        Peer p2 = create("enode://localhost");
        System.out.println(p2);
    }
}