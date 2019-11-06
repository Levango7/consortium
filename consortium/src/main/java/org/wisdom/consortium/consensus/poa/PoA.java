package org.wisdom.consortium.consensus.poa;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.springframework.core.io.Resource;
import org.wisdom.common.*;
import org.wisdom.consortium.consensus.poa.config.Genesis;
import org.wisdom.consortium.state.Account;
import org.wisdom.consortium.util.FileUtils;
import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.*;

import static org.wisdom.consortium.consensus.poa.PoAHashPolicy.HASH_POLICY;

// poa is a minimal non-trivial consensus engine
public class PoA implements ConsensusEngine {
    private PoAConfig poAConfig;

    private Miner miner;

    @Override
    public Miner miner() {
        return miner;
    }

    @Override
    public StateRepository repository() {
        return repository;
    }

    public HashPolicy policy() {
        return HASH_POLICY;
    }

    private Validator validator;

    private StateRepository repository;

    private Genesis genesis;

    private Block genesisBlock;

    public PoA() {
        this.validator = new PoaValidator();
    }

    @Override
    public Block genesis() {
        if (genesisBlock != null) return genesisBlock;
        genesisBlock = genesis.getBlock();
        return genesisBlock;
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

        // register miner accounts
        this.repository.register(genesis(), Collections.singleton(new Account(poaMiner.minerPublicKeyHash, 0)));
    }

    @Override
    public Validator validator() {
        return validator;
    }

    @Override
    public ConfirmedBlocksProvider provider() {
        return unconfirmed -> unconfirmed;
    }

    @Override
    public PeerServerListener handler() {
        return new PeerServerListener() {
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
        };
    }
}
