package org.wisdom.common;

import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.Properties;

public interface ConsensusEngine extends Miner, BlockValidator, PendingTransactionValidator, ConfirmedBlocksProvider {
    Block getGenesis();

    void load(Properties properties) throws ConsensusEngineLoadException;

    void use(ForkAbleDataStore dataStore);
}
