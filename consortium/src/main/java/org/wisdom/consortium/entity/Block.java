package org.wisdom.consortium.entity;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = HeaderAdapter.TABLE_HEADER)
public class Block extends HeaderAdapter {
    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(
            name = Transaction.COLUMN_BLOCK_HASH,
            referencedColumnName = HeaderAdapter.COLUMN_HASH,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Getter
    @Setter
    List<Transaction> body;

    public Block() {
    }

    public Block(HeaderAdapter adapter){
        super(adapter.getHash(), adapter.getVersion(), adapter.getHashPrev(), adapter.getMerkleRoot(), adapter.getHeight(), adapter.getCreatedAt(), adapter.getPayload());
    }

    public Block(byte[] hash, int version, byte[] hashPrev, byte[] merkleRoot, long height, long createdAt, byte[] payload) {
        super(hash, version, hashPrev, merkleRoot, height, createdAt, payload);
    }
}
