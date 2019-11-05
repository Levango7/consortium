package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

public interface State<T> extends Cloneable<T>{
    void update(Block b, Transaction t) throws StateUpdateException;
    // some state are per-block, not per-transaction
    void update(Block b) throws StateUpdateException;
}
