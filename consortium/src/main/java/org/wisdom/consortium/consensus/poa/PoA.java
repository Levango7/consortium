package org.wisdom.consortium.consensus.poa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.wisdom.common.Block;
import org.wisdom.common.BlockStore;
import org.wisdom.common.MinerListener;
import org.wisdom.consortium.ApplicationConstants;

import javax.annotation.PostConstruct;

@Component
@ConditionalOnProperty(
        name = ApplicationConstants.CONSENSUS_NAME_PROPERTY,
        havingValue = ApplicationConstants.CONSENSUS_POA
)
public class PoA implements MinerListener {
    @Autowired
    private PoaMiner poaMiner;

    @Autowired
    private BlockStore blockStore;

    // avoid cycle dependency here
    @PostConstruct
    public void init(){
        poaMiner.subscribe(this);
        blockStore.subscribe(poaMiner);
    }

    @Override
    public void onBlockMined(Block block) {
        blockStore.writeBlock(block);
    }

    @Override
    public void onMiningFailed(Block block) {

    }
}
