package org.wisdom.consortium.consensus.poa;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import lombok.experimental.Delegate;
import org.springframework.core.io.Resource;
import org.wisdom.common.*;
import org.wisdom.consortium.consensus.poa.config.Genesis;
import org.wisdom.consortium.util.FileUtils;
import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class PoA implements ConsensusEngine {
    private PoAConfig poAConfig;

    @Delegate
    private Miner miner;

    @Delegate
    private HashPolicy hashPolicy;

    private Genesis genesis;

    public PoA() {
        this.hashPolicy = PoAHashPolicy.HASH_POLICY;
    }

    @Override
    public Block getGenesis() {
        return genesis.getBlock();
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
        PoaMiner poaMiner = new PoaMiner();
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
    }

    @Override
    public ValidateResult validateBlock(Block block, Block dependency) {
        return ValidateResult.success();
    }


    @Override
    public ValidateResult validateTransaction(Transaction transaction) {
        return ValidateResult.success();
    }

    @Override
    public <T extends State<T>> Optional<T> getState(Chained node, Class<T> clazz) {
        return Optional.empty();
    }

    @Override
    public <T extends State<T>> void registerGenesis(T genesisState) {

    }


}
