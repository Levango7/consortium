package org.wisdom.consortium.consensus.economic;

import org.springframework.stereotype.Component;

@Component
public class EconomicModelImpl implements org.wisdom.consortium.consensus.economic.EconomicModel {

    private static final long INITIAL_SUPPLY = 20;

    private static final long HALF_PERIOD = 10000000;

    @Override
    public long getConsensusRewardAtHeight(long height) {
        long era = height / HALF_PERIOD;
        long reward = INITIAL_SUPPLY;
        for(long i = 0; i < era; i++){
            reward = reward * 52218182 / 100000000;
        }
        return reward;
    }

}
