package org.wisdom.consortium.dao;

import org.wisdom.consortium.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TransactionDao extends JpaRepository<Transaction, byte[]> {
    List<Transaction> findTransactionsByBlockHashIn(Collection<byte[]> blockHashes);
    List<Transaction> findTransactionsByBlockHashOrderByPosition(byte[] blockHash);
    boolean existsByPayload(byte[] payload);
}
