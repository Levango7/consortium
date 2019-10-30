package org.wisdom.consortium.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {
    @Id
    @Column(name = "block_hash")
    private byte[] hash;

    @Column(name = "block_height", nullable = false)
    private long height;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(
            name = "block_hash",
            referencedColumnName = "block_hash",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    List<Transaction> body;
}
