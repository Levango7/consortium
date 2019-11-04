package org.wisdom.consortium.consensus.poa.config;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.wisdom.common.Block;
import org.wisdom.consortium.consensus.poa.Proposer;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ConsensusConfigImpl implements ConsensusConfig {

    @Autowired
    private Genesis genesis;

    @Override
    public boolean isEnableMining() {
        return false;
    }

    @Override
    public String getMinerPubKeyHash() {
        return null;
    }

    @Override
    public List<String> getPeers() {
        return null;
    }

    @Override
    public List<String> getValidators() {
        return genesis.miners.stream().map(x -> Hex.encodeHexString(wallet.KeystoreAction.addressToPubKeyHash(x.address))).collect(Collectors.toList());
    }

    @Override
    public void setHashFunction(Function<byte[], byte[]> hashFunction) {

    }

    @Override
    public Function<byte[], byte[]> getHashFunction() {
        return null;
    }

    @Override
    public Optional<Proposer> getProposer(Block parentBlock, long timeStamp) {
        return Optional.empty();
    }
}
