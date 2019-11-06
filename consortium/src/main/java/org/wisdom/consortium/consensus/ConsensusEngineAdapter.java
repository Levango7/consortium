package org.wisdom.consortium.consensus;

import org.wisdom.common.*;
import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.Properties;

public class ConsensusEngineAdapter implements ConsensusEngine {
    @Override
    public void load(Properties properties, ConsortiumRepository repository) throws ConsensusEngineLoadException {

    }

    @Override
    public Block genesis() {
        return null;
    }

    @Override
    public Validator validator() {
        return null;
    }

    @Override
    public Miner miner() {
        return null;
    }

    @Override
    public ConfirmedBlocksProvider provider() {
        return null;
    }

    @Override
    public HashPolicy policy() {
        return null;
    }

    @Override
    public StateRepository repository() {
        return null;
    }

    @Override
    public PeerServerListener handler() {
        return null;
    }
}
