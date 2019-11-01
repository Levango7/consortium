package org.wisdom.common;

import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

public interface ConsensusEngine extends Miner, BlockValidator, PendingTransactionValidator{
    Block getGenesis();

    Optional<Block> getConfirmed(List<Block> unconfirmed);

    void load(Properties properties) throws ConsensusEngineLoadException;

    void use(BlockStore blockStore);

    void use(TransactionStore transactionStore);
}
