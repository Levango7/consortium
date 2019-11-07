package org.wisdom.consortium.net;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wisdom.util.BigEndian;

@Getter
@Setter
@NoArgsConstructor
public class PeerServerConfig {
    public static final int DEFAULT_PORT = BigEndian.decodeInt32(new byte[]{'w', 'i'});
    public static final String DEFAULT_PROTOCOL = "node";

    private String name;
    @JsonProperty("max-peers")
    private long maxPeers;
    private int port;
    private String protocol;
    private String address;
}
