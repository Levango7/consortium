package org.wisdom.common;

import java.util.List;

public interface TransactionPool {

    void collect(Transaction... transactions);

    List<Transaction> pop();

    int size();
}
