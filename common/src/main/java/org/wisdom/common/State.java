package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.util.Collection;

public interface State<T> extends Cloneable<T>, Serializable {
    void updateTransaction(Block b, Transaction t) throws StateUpdateException;

    void updateBlock(Block b) throws StateUpdateException;

    void updateBlocks(Collection<? super Block> blocks) throws StateUpdateException;
}
