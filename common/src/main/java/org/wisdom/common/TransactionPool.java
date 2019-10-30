package org.wisdom.common;

import java.util.Optional;

public interface TransactionPool {

    // collect transactions into transaction pool
    void collect(Transaction... transactions);

    // pop a transaction from pool
    Optional<Transaction> pop();

    // get size of current transaction pool
    int size();
}
