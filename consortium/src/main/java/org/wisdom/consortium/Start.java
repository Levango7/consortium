package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wisdom.common.*;
import org.wisdom.consortium.consensus.poa.PoA;
import org.wisdom.consortium.consensus.poa.PoAConfig;
import org.wisdom.consortium.service.BlockStoreService;


@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
// use SPRING_CONFIG_LOCATION environment to locate spring config
// for example: SPRING_CONFIG_LOCATION=classpath:\application.yml,some-path\custom-config.yml
public class Start {
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
    public ConsensusEngine consensusEngine(ConsensusProperties consensusProperties, ForkAbleDataStore forkAbleDataStore) throws Exception{
        String name = consensusProperties.getConsensus().getProperty(ConsensusProperties.CONSENSUS_NAME);
        ConsensusEngine engine;
        switch (name.toLowerCase()){
            // use poa as default consensus
            // another engine: pow, pos, pow+pos, vrf
            default:
                engine = new PoA();
        }
        engine.load(consensusProperties.getConsensus());
        forkAbleDataStore.saveGenesis(engine.getGenesis());
        engine.use(forkAbleDataStore);
        forkAbleDataStore.use(engine);
        engine.subscribe(new MinerListener() {
            @Override
            public void onBlockMined(Block block) {
                forkAbleDataStore.writeBlock(block);
            }
            @Override
            public void onMiningFailed(Block block) {

            }
        });
        engine.start();
        return engine;
    }
}
