package org.wisdom.common;

import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.Properties;

public interface ConsensusEngine {
    void load(Properties properties, ConsortiumRepository repository) throws ConsensusEngineLoadException;

    Block genesis();

    Validator validator();

    Miner miner();

    ConfirmedBlocksProvider provider();

    HashPolicy policy();

    StateRepository repository();

    PeerServerListener handler();

    interface Validator extends BlockValidator, PendingTransactionValidator {
    }
}
