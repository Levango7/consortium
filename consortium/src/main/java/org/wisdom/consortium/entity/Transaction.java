package org.wisdom.consortium.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "transaction")
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
    private byte[] transactionHash;

    int position;
}
