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
import org.wisdom.common.Block;
import org.wisdom.common.BlockStore;
import org.wisdom.common.ConsensusEngine;
import org.wisdom.common.MinerListener;
import org.wisdom.consortium.consensus.poa.PoA;
import org.wisdom.consortium.consensus.poa.PoAConfig;


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
    public ConsensusEngine consensusEngine(ConsensusProperties consensusProperties, BlockStore blockStore) throws Exception{
        String name = consensusProperties.getConsensus().getProperty(ConsensusProperties.CONSENSUS_NAME);
        ConsensusEngine engine;
        switch (name){
            default:
                engine = new PoA();
        }
        engine.load(consensusProperties.getConsensus());
        blockStore.writeBlock(engine.getGenesis());
        engine.use(blockStore);
        engine.subscribe(new MinerListener() {
            @Override
            public void onBlockMined(Block block) {
                blockStore.writeBlock(block);
            }

            @Override
            public void onMiningFailed(Block block) {

            }
        });
        engine.start();
        return engine;
    }
}
