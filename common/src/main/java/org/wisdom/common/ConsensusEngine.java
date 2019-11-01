package org.wisdom.common;

public interface ConsensusEngine extends Miner, BlockValidator{
    Block getGenesis(String resourcePath);
}
