package org.wisdom.common;


public interface Miner extends BlockStoreListener{
    void start();
    void stop();
    void subscribe(MinerListener... listeners);
}
