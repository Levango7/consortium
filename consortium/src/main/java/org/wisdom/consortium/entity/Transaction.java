package org.wisdom.consortium.entity;

import lombok.*;
import org.wisdom.common.HexBytes;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "transaction", indexes = {
        @Index(name = "transaction_block_hash_index", columnList = "block_hash"),
        @Index(name = "transaction_hash_index", columnList = "transaction_hash"),
        @Index(name = "transaction_type_index", columnList = "transaction_type"),
        @Index(name = "transaction_created_at_index", columnList = "transaction_created_at"),
        @Index(name = "transaction_nonce_index", columnList = "transaction_nonce"),
        @Index(name = "transaction_from_index", columnList = "transaction_from"),
        @Index(name = "transaction_amount_index", columnList = "transaction_amount"),
        @Index(name = "transaction_to_index", columnList = "transaction_to"),
        @Index(name = "transaction_position_index", columnList = "transaction_position"),
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    public static void sortTransactions(List<Transaction> transactions) {
        transactions.sort(Comparator.comparingInt(Transaction::getPosition));
    }

    @Column(name = "block_hash")
    private byte[] blockHash;

    @Id
    @Column(name = "transaction_hash")
    private byte[] hash;

    @Column(name = "transaction_version")
    private int version;

    @Column(name = "transaction_type")
    private int type;

    @Column(name = "transaction_created_at")
    private long createdAt;

    @Column(name = "transaction_nonce")
    private long nonce;

    @Column(name = "transaction_from")
    private byte[] from;

    @Column(name = "transaction_gas_price")
    private long gasPrice;

    @Column(name = "transaction_amount")
    private long amount;

    @Column(name = "transaction_payload")
    public byte[] payload;

    @Column(name = "transaction_to")
    private byte[] to;

    @Column(name = "transaction_signature")
    private byte[] signature;

    @Column(name = "transaction_position")
    private int position;
}
