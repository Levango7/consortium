package org.wisdom.common;

import java.util.List;
import java.util.Optional;

public interface TransactionPool extends MinerListener{

    // collect transactions into transaction pool
    void collect(Transaction... transactions);

    // pop a transaction from pool
    Optional<Transaction> pop();

    // pop at most n transactions
    // if limit < 0, pop all transactions
    List<Transaction> pop(int limit);

    // get size of current transaction pool
    int size();
}
