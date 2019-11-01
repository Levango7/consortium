package org.wisdom.consortium.dao;

import org.springframework.data.domain.Pageable;
import org.wisdom.consortium.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TransactionDao extends JpaRepository<Transaction, byte[]> {
    List<Transaction> findByBlockHashIn(Collection<byte[]> blockHashes);

    List<Transaction> findByBlockHashOrderByPosition(byte[] blockHash);

    List<Transaction> findByFromOrderByHeightAscPositionAsc(byte[] from, Pageable pageable);

    List<Transaction> findByFromAndType(byte[] from, int type, Pageable pageable);

    List<Transaction> findByTo(byte[] to, Pageable pageable);

    List<Transaction> findByToAndType(byte[] to, int type, Pageable pageable);

    List<Transaction> findByFromAndTo(byte[] from, byte[] to, Pageable pageable);

    List<Transaction> findByFromAndToAndType(byte[] from, byte[] to, int type, Pageable pageable);

    List<Transaction> findByPayload(byte[] payload);

    List<Transaction> findByHeightOrderByPosition(long height);

    boolean existsByPayload(byte[] payload);
}
