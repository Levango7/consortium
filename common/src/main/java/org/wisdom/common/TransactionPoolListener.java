package org.wisdom.common;

public interface TransactionPoolListener {
    void onNewTransactionCollected(Transaction transaction);
}
