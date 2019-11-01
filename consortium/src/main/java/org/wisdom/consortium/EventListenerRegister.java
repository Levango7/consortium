package org.wisdom.consortium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wisdom.common.Block;
import org.wisdom.common.BlockStore;
import org.wisdom.common.Miner;
import org.wisdom.common.MinerListener;

import javax.annotation.PostConstruct;

@Component
public class EventListenerRegister {
    @Autowired
    private Miner miner;

    @Autowired
    private BlockStore blockStore;

    @PostConstruct
    public void init(){
        miner.subscribe(new MinerListener() {
            @Override
            public void onBlockMined(Block block) {
                blockStore.writeBlock(block);
            }

            @Override
            public void onMiningFailed(Block block) {

            }
        });
    }
}
