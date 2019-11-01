package org.wisdom.consortium.consensus.poa;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.springframework.core.io.Resource;
import org.wisdom.common.*;
import org.wisdom.consortium.consensus.poa.config.Genesis;
import org.wisdom.consortium.util.FileUtils;
import org.wisdom.exception.ConsensusEngineLoadException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PoA implements ConsensusEngine {
    private PoAConfig poAConfig;
    private PoaMiner poaMiner;
    private Genesis genesis;

    public PoA() {
        this.poaMiner = new PoaMiner();
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
    public void load(Properties properties) throws ConsensusEngineLoadException {
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
    }

    @Override
    public void use(ForkAbleDataStore blockStore) {
        poaMiner.setBlockStore(blockStore);
    }


    @Override
    public ValidateResult validateBlock(Block block, Block dependency) {
        return ValidateResult.success();
    }

    @Override
    public void start() {
        poaMiner.start();
    }

    @Override
    public void stop() {
        poaMiner.stop();
    }

    @Override
    public void subscribe(MinerListener... listeners) {
        poaMiner.subscribe(listeners);
    }

    @Override
    public void onBlockWritten(Block block) {

    }

    @Override
    public void onNewBestBlock(Block block) {

    }

    @Override
    public ValidateResult validateTransaction(Transaction transaction) {
        return ValidateResult.success();
    }
}
