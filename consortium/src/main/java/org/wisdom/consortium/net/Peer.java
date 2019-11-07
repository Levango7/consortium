package org.wisdom.consortium.net;

import lombok.*;
import org.wisdom.common.HexBytes;
import org.wisdom.crypto.ed25519.Ed25519PrivateKey;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Peer implements org.wisdom.common.Peer {
    private String host;
    private int port;
    private HexBytes ID;
    private Ed25519PrivateKey privateKey;
}
