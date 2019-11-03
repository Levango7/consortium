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
import org.wisdom.consortium.exception.ApplicationException;

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
    public ConsensusEngine consensusEngine(ConsensusProperties consensusProperties, ConsortiumRepository consortiumRepository) throws Exception {
        String name = consensusProperties.getConsensus().getProperty(ConsensusProperties.CONSENSUS_NAME);
        final ConsensusEngine engine;
        switch (name.toLowerCase()) {
            // use poa as default consensus
            // another engine: pow, pos, pow+pos, vrf
            case ApplicationConstants.CONSENSUS_POA:
                engine = new PoA();
                break;
            default:
                log.error(
                        "none available consensus configured by consortium.consensus.name=" + name +
                                " please provide available consensus engine");
                engine = new ConsensusEngineAdapter();
        }

        engine.load(consensusProperties.getConsensus(), consortiumRepository);
        consortiumRepository.saveGenesis(engine.getGenesis());
        consortiumRepository.setProvider(engine);
        engine.addListeners(new MinerListener() {
            @Override
            public void onBlockMined(Block block) {
                Optional<Block> o = consortiumRepository.getBlock(block.getHashPrev().getBytes());
                if (!o.isPresent()){
                    throw new RuntimeException("successfully mined on an unknown block");
                }
                ValidateResult result = engine.validateBlock(block, o.get());
                if (!result.isSuccess()){
                    log.error("validate block failed");
                    log.error(result.getReason());
                    return;
                }
                consortiumRepository.writeBlock(block);
            }

            @Override
            public void onMiningFailed(Block block) {

            }
        });
        engine.start();
        return engine;
    }
}
