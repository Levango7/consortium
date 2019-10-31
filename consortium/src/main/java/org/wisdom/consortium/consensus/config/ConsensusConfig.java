package org.wisdom.consortium.consensus.config;

import org.wisdom.common.Block;
import org.wisdom.consortium.consensus.poa.Proposer;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface ConsensusConfig {

    int mineBlockMaxSize = 5000;

    boolean isEnableMining();

    String getMinerPubKeyHash();

    List<String> getPeers();

    List<String> getValidators();

    void setHashFunction(Function<byte[], byte[]> hashFunction);

    Function<byte[], byte[]> getHashFunction();

    Optional<Proposer> getProposer(Block parentBlock, long timeStamp);
}
