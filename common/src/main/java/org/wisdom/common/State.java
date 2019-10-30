package org.wisdom.common;

import java.util.Collection;

public interface State<T> extends Cloneable<T> {
    T updateTransaction(Block b, Transaction t);

    T updateBlock(Block b);

    T updateBlocks(Collection<? super Block> blocks);
}
