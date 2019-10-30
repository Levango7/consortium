package org.wisdom.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
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
        setHash(header.getHash());
    }

    @Getter
    @Setter
    @NonNull
    private List<Transaction> body;

    public Block clone() {
        Block b = new Block();
        b.copyFromHeader(this);
        b.setBody(body.stream().map(Transaction::clone).collect(Collectors.toList()));
        return b;
    }

    @JsonIgnore
    public int size(){
        return super.size() + body.stream()
                .map(Transaction::size)
                .reduce(0, Integer::sum);
    }
}
