package org.wisdom.common;

import java.util.List;

public interface ForkAbleDataStore extends BlockStore, TransactionStore{
    Block getLastConfirmed();
    List<Block> getUnconfirmed();
    void use(ConfirmedBlocksProvider provider);
}
