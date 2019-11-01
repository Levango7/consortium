package org.wisdom.consortium.consensus.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Genesis {
    public String coinbase;

    public String nonce;

    public String hashBlock;

    public String parentHash;

    public long timestamp;

    public static class MinerInfo {
        @JsonProperty("addr")
        public String address;
    }

    public List<MinerInfo> miners;

}
