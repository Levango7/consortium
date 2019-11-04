package org.wisdom.consortium.consensus;

import org.wisdom.common.*;
import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class ConsensusEngineAdapter implements ConsensusEngine {
    @Override
    public Block getGenesis() {
        return null;
    }

    @Override
    public void load(Properties properties, ConsortiumRepository repository) throws ConsensusEngineLoadException {

    }

    @Override
    public ValidateResult validateBlock(Block block, Block dependency) {
        return null;
    }

    @Override
    public List<Block> getConfirmed(List<Block> unconfirmed) {
        return null;
    }

    @Override
    public HexBytes getHash(Block block) {
        return null;
    }

    @Override
    public HexBytes getHash(Transaction transaction) {
        return null;
    }

    @Override
    public HexBytes getHash(Header header) {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void addListeners(MinerListener... listeners) {

    }

    @Override
    public void onBlockWritten(Block block) {

    }

    @Override
    public void onNewBestBlock(Block block) {
    }

    @Override
    public void onBlockConfirmed(Block block) {

    }

    @Override
    public void onTransactionsConfirmed(Transaction... transactions) {

    }

    @Override
    public ValidateResult validateTransaction(Transaction transaction) {
        return null;
    }

    @Override
    public <T extends State<T>> void registerGenesis(T genesisState) {

    }

    @Override
    public <T extends State<T>> Optional<T> getState(Block last, Class<T> clazz) {
        return Optional.empty();
    }
}
