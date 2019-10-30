package org.wisdom.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class Block extends Header {
    private void copyFromHeader(Header header) {
        setVersion(header.getVersion());
        setHashPrev(header.getHashPrev());
        setMerkleRoot(header.getMerkleRoot());
        setHeight(header.getHeight());
        setCreatedAt(header.getCreatedAt());
        setPayload(header.getPayload());
    }

    @Getter
    @Setter
    private List<Transaction> body;

    public Block clone() {
        Block b = new Block();
        b.copyFromHeader(this);
        b.setBody(body.stream().map(Transaction::clone).collect(Collectors.toList()));
        return b;
    }
}
