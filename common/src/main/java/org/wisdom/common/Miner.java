package org.wisdom.common;


public interface Miner extends ConsortiumRepositoryListener {
    void start();
    void stop();
    void addListeners(MinerListener... listeners);
}
