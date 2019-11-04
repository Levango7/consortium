package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

public interface State<T> extends Cloneable<T>, Chained{
    void update(Block b, Transaction t) throws StateUpdateException;
}
