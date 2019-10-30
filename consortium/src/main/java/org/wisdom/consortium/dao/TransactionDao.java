package org.wisdom.consortium.dao;

import org.wisdom.consortium.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TransactionDao extends JpaRepository<Transaction, String> {
    List<Transaction> getTransactionsByBlockHashIn(Collection<byte[]> blockHashes);
}
