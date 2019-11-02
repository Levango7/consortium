package org.wisdom.common;


public interface Miner extends BlockRepositoryListener {
    void start();
    void stop();
    void addListeners(MinerListener... listeners);
}
