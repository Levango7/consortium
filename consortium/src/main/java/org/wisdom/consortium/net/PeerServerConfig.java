package org.wisdom.consortium.net;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wisdom.util.BigEndian;

import java.net.URI;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PeerServerConfig {
    public static final int DEFAULT_PORT = BigEndian.decodeInt32(new byte[]{'w', 'i'});
    public static final String DEFAULT_PROTOCOL = "node";
    public static final long DEFAULT_MAX_TTL = 8;

    private String name;
    @JsonProperty("max-peers")
    private int maxPeers;
    @JsonProperty("max-ttl")
    private long maxTTL;
    private String address;
    private List<URI> bootstraps;
}
