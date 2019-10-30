package org.wisdom.consortium.entity;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Table(name = "header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block extends AbstractHeader{
    @Id
    @Column(name = "block_hash", nullable = false)
    private byte[] hash;

    @Column(name = "block_version", nullable = false)
    private int version;

    @Column(name = "hash_prev", nullable = false)
    private byte[] hashPrev;

    @Column(name = "merkle_root", nullable = false)
    private byte[] merkleRoot;

    @Column(name = "block_height", nullable = false)
    private long height;

    @Column(name = "block_created_at", nullable = false)
    private long createdAt;

    @Column(name = "block_payload", nullable = false)
    private byte[] payload;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(
            name = "block_hash",
            referencedColumnName = "block_hash",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    List<Transaction> body;
}
