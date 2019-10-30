package org.wisdom.common;

public interface BlockStoreListener {
    void onBlockWritten(Block block);
    void onNewBestBlock(Block block);
}
