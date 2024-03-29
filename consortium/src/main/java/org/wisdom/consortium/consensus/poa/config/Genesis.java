package org.wisdom.consortium.consensus.poa.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.wisdom.common.Block;
import org.wisdom.common.Header;
import org.wisdom.common.HexBytes;
import org.wisdom.consortium.consensus.poa.PoAConstants;
import org.wisdom.consortium.consensus.poa.PoAUtils;

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

    @JsonIgnore
    public Block getBlock(){
        Header h = Header.builder()
                .version(PoAConstants.BLOCK_VERSION)
                .hashPrev(PoAConstants.ZERO_BYTES)
                .merkleRoot(PoAConstants.ZERO_BYTES)
                .height(0)
                .createdAt(timestamp)
                .payload(PoAConstants.ZERO_BYTES).build();
        Block b = new Block(h);
        b.setHash(new HexBytes(PoAUtils.getHash(b)));
        return b;
    }
}
