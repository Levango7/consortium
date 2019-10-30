package org.wisdom.common;

import java.util.List;
import java.util.Optional;

public interface TransactionStore {
    boolean hasTransaction(byte[] hash);

    boolean hasPayload(byte[] payload);

    Optional<Transaction> getTransactionByHash(byte[] hash);

    List<Transaction> getTransactionsByFrom(byte[] from, int offset, int limit);

    List<Transaction> getTransactionsByFromAndType(int type, byte[] from, int offset, int limit);

    List<Transaction> getTransactionsByTo(byte[] to, int offset, int limit);

    List<Transaction> getTransactionsByToAndType(int type, byte[] from, int offset, int limit);

    List<Transaction> getTransactionsByFromAndTo(byte[] from, byte[] to, int offset, int limit);

    List<Transaction> getTransactionsByFromAndToAndType(int type, byte[] from, byte[] to, int offset, int limit);

    List<Transaction> getTransactionsByPayload(byte[] payload);

    List<Transaction> getTransactionsByBlockHash(byte[] blockHash);

    List<Transaction> getTransactionsByBlockHeight(long height);
}
