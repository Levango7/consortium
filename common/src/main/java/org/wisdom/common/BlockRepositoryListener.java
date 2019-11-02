package org.wisdom.common;

public interface BlockRepositoryListener {
    void onBlockWritten(Block block);
    void onNewBestBlock(Block block);
}
