package org.wisdom.consortium.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Header {
    @Id
    @Column(name = "block_hash")
    private byte[] hash;

    @Column(name = "block_height", nullable = false)
    private long height;
}