package org.wisdom.consortium.consensus.poa;

import org.wisdom.common.Block;
import org.wisdom.common.ConsensusEngine;
import org.wisdom.common.MinerListener;
import org.wisdom.common.ValidateResult;

public class PoA implements ConsensusEngine {
    @Override
    public Block getGenesis(String resourcePath) {
        return null;
    }

    @Override
    public ValidateResult validateBlock(Block block, Block dependency) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void subscribe(MinerListener... listeners) {

    }

    @Override
    public void onBlockWritten(Block block) {

    }

    @Override
    public void onNewBestBlock(Block block) {

    }
}
