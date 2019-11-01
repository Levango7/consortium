package org.wisdom.consortium.consensus.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.wisdom.common.HexBytes;

import java.util.List;

public class Genesis {
    public HexBytes coinbase;

    public HexBytes nonce;

    public HexBytes hashBlock;

    public HexBytes parentHash;

    public long timestamp;

    public static class MinerInfo {
        @JsonProperty("addr")
        public String address;
    }

    public List<MinerInfo> miners;

}
