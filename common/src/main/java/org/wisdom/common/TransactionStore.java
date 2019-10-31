package org.wisdom.common;

import java.util.List;
import java.util.Optional;

public interface TransactionStore {
    boolean hasTransaction(byte[] hash);

    boolean hasPayload(byte[] payload);

    Optional<Transaction> getTransactionByHash(byte[] hash);

    List<Transaction> getTransactionsByFrom(byte[] from, int page, int size);

    List<Transaction> getTransactionsByFromAndType(byte[] from, int type, int page, int size);

    List<Transaction> getTransactionsByTo(byte[] to, int page, int size);

    List<Transaction> getTransactionsByToAndType(byte[] to, int type, int page, int size);

    List<Transaction> getTransactionsByFromAndTo(byte[] from, byte[] to, int page, int size);

    List<Transaction> getTransactionsByFromAndToAndType(byte[] from, byte[] to, int type, int page, int size);

    List<Transaction> getTransactionsByPayload(byte[] payload);

    List<Transaction> getTransactionsByBlockHash(byte[] blockHash);

    List<Transaction> getTransactionsByBlockHeight(long height);
}
