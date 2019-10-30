package org.wisdom.consortium.entity;


import lombok.*;

import javax.persistence.*;
import java.util.List;

@Table(name = "header")
@Entity
public class Block extends HeaderAdapter {

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(
            name = "block_hash",
            referencedColumnName = "block_hash",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Getter
    @Setter
    List<Transaction> body;
}
