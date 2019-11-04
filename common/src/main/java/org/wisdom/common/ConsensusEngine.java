package org.wisdom.common;

import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.Properties;

public interface ConsensusEngine extends Miner,
        BlockValidator,
        PendingTransactionValidator,
        ConfirmedBlocksProvider,
        StateRepository,
        HashPolicy
{
    Block getGenesis();

    void load(Properties properties, ConsortiumRepository repository) throws ConsensusEngineLoadException;
}
