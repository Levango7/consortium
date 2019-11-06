package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.wisdom.common.*;
import org.wisdom.consortium.consensus.ConsensusEngineAdapter;
import org.wisdom.consortium.consensus.poa.PoA;

import java.util.Optional;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
@Slf4j
// use SPRING_CONFIG_LOCATION environment to locate spring config
// for example: SPRING_CONFIG_LOCATION=classpath:\application.yml,some-path\custom-config.yml
public class Start {
    private static final boolean ENABLE_ASSERTION = "true".equals(System.getenv("ENABLE_ASSERTION"));

    public static void devAssert(boolean truth, String error){
        if (!ENABLE_ASSERTION) return;
        Assert.isTrue(truth, error);
    }

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .enable(JsonParser.Feature.ALLOW_COMMENTS);

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    @Bean
    public Miner miner(ConsensusEngine engine, NewMinedBlockWriter writer){
        Miner miner = engine.miner();
        miner.addListeners(writer);
        miner.start();
        return miner;
    }

    @Bean
    public StateRepository stateRepository(ConsensusEngine engine){return engine.repository();}

    @Bean
    public PendingTransactionValidator transactionValidator(ConsensusEngine engine){return engine.validator();}

    @Bean
    public ConsensusEngine consensusEngine(ConsensusProperties consensusProperties, ConsortiumRepository consortiumRepository) throws Exception {
        String name = consensusProperties.getProperty(ConsensusProperties.CONSENSUS_NAME);
        name = name == null ? "" : name;
        final ConsensusEngine engine;
        switch (name.toLowerCase()) {
            // none consensus selected, used for unit test
            case ApplicationConstants.CONSENSUS_NONE:
                log.warn("none consensus engine selected, please ensure you are in test mode");
                return new ConsensusEngineAdapter();
            case ApplicationConstants.CONSENSUS_POA:
                // use poa as default consensus
                // another engine: pow, pos, pow+pos, vrf
                engine = new PoA();
                break;
            default:
                log.error(
                        "none available consensus configured by consortium.consensus.name=" + name +
                                " please provide available consensus engine");
                log.error("roll back to poa consensus");
                engine = new PoA();
        }
        engine.load(consensusProperties, consortiumRepository);
        consortiumRepository.setProvider(engine.provider());
        consortiumRepository.addListeners(engine.repository());
        consortiumRepository.saveGenesis(engine.genesis());
        return engine;
    }

    abstract static class NewMinedBlockWriter implements MinerListener{
    }

    @Bean
    public NewMinedBlockWriter newMinedBlockWriter(ConsortiumRepository repository, ConsensusEngine engine){
        return new NewMinedBlockWriter() {
            @Override
            public void onBlockMined(Block block) {
                Optional<Block> o = repository.getBlock(block.getHashPrev().getBytes());
                if (!o.isPresent()) return;
                if (engine.validator().validate(block, o.get()).isSuccess()){
                    repository.writeBlock(block);
                }
            }

            @Override
            public void onMiningFailed(Block block) {

            }
        };
    }
}
