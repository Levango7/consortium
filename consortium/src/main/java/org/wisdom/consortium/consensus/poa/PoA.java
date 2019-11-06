package org.wisdom.consortium.consensus.poa;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import lombok.experimental.Delegate;
import org.springframework.core.io.Resource;
import org.wisdom.common.*;
import org.wisdom.consortium.account.PublicKeyHash;
import org.wisdom.consortium.consensus.poa.config.Genesis;
import org.wisdom.consortium.state.Account;
import org.wisdom.consortium.util.FileUtils;
import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.*;

// poa is a minimal non-trivial consensus engine
public class PoA implements ConsensusEngine {
    private PoAConfig poAConfig;

    @Delegate
    private Miner miner;

    @Delegate
    private HashPolicy hashPolicy;

    @Delegate
    private BlockValidator blockValidator;

    @Delegate
    private PendingTransactionValidator transactionValidator;

    @Delegate
    private StateRepository repository;

    private Genesis genesis;

    private Block genesisBlock;

    public PoA() {
        this.hashPolicy = PoAHashPolicy.HASH_POLICY;
        PoaValidator validator = new PoaValidator();
        this.blockValidator = validator;
        this.transactionValidator = validator;
    }

    @Override
    public Block getGenesis() {
        if (genesisBlock != null) return genesisBlock;
        genesisBlock = genesis.getBlock();
        return genesisBlock;
    }

    @Override
    public List<Block> getConfirmed(List<Block> unconfirmed) {
        return new ArrayList<>();
    }

    @Override
    public void load(Properties properties, ConsortiumRepository repository) throws ConsensusEngineLoadException {
        JavaPropsMapper mapper = new JavaPropsMapper();
        ObjectMapper objectMapper = new ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS);
        try{
            poAConfig = mapper.readPropertiesAs(properties, PoAConfig.class);
        }catch (Exception e){
            String schema = "";
            try{
                schema = mapper.writeValueAsProperties(new PoAConfig()).toString();
            }catch (Exception ignored){};
            throw new ConsensusEngineLoadException(
                    "load properties failed :" + properties.toString() + " expecting " + schema
            );
        }
        PoAMiner poaMiner = new PoAMiner();
        Resource resource;
        try{
            resource = FileUtils.getResource(poAConfig.getGenesis());
        }catch (Exception e){
            throw new ConsensusEngineLoadException(e.getMessage());
        }
        try{
            genesis = objectMapper.readValue(resource.getInputStream(), Genesis.class);
        }catch (Exception e){
            throw new ConsensusEngineLoadException("failed to parse genesis");
        }
        poaMiner.setPoAConfig(poAConfig);
        poaMiner.setGenesis(genesis);
        poaMiner.setRepository(repository);
        this.miner = poaMiner;
        this.repository = new ConsortiumStateRepository();
        Optional<PublicKeyHash> o = PublicKeyHash.from(poAConfig.getMinerCoinBase());

        // register miner accounts
        if (!o.isPresent()) return;
        this.repository.register(getGenesis(), Collections.singleton(new Account(o.get(), 0)));

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
