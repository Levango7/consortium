package org.wisdom.consortium.consensus;

import org.wisdom.common.*;
import org.wisdom.exception.ConsensusEngineLoadException;
import org.wisdom.exception.StateUpdateException;

import java.util.Collection;
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
    public ValidateResult validateTransaction(Transaction transaction) {
        return null;
    }

    @Override
    public <T extends State<T>> void register(Block genesis, T genesisState) throws StateUpdateException {

    }

    @Override
    public <T extends ForkAbleState<T>> void register(Block genesis, T... forkAbleStates) {

    }

    @Override
    public <T extends State<T>> Optional<T> get(byte[] hash, Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    public <T extends ForkAbleState<T>> Optional<T> get(byte[] hash, String id, Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    public void update(Block b, State state) {

    }

    @Override
    public void update(Block b, Collection<ForkAbleState> forkAbleStates, Class<? extends ForkAbleState> clazz) {

    }

    @Override
    public void update(Block b) {

    }

    @Override
    public void confirm(byte[] hash) {

    }

    @Override
    public void onMessage(Context context, PeerServer server) {

    }

    @Override
    public void onStart(PeerServer server) {

    }

    @Override
    public void onNewPeer(Peer peer, PeerServer server) {

    }

    @Override
    public void onDisconnect(Peer peer, PeerServer server) {

    }
}
