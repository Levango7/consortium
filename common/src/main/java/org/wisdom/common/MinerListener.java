package org.wisdom.common;

public interface MinerListener {

    void onBlockMined(Block block);

    void onMiningFailed(Block block);
}
