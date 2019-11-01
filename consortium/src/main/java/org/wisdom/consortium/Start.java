package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wisdom.consortium.config.ConsortiumConfig;
import org.wisdom.consortium.consensus.config.Genesis;
import org.wisdom.consortium.exception.ApplicationException;


@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
// use SPRING_CONFIG_LOCATION environment to locate spring config
// for example: SPRING_CONFIG_LOCATION=classpath:\application.yml,some-path\custom-config.yml
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(JsonParser.Feature.ALLOW_COMMENTS);
        SimpleModule module = new SimpleModule();
        return mapper.registerModule(module);
    }

    // load configuration dynamically

    @Bean
    public Genesis genesis(ConsortiumConfig config, ObjectMapper objectMapper)
            throws Exception {
        String genesisPath = config.getConsensus().getGenesis();

        Resource resource = new FileSystemResource(genesisPath);
        if (!resource.exists()) {
            resource = new ClassPathResource(genesisPath);
        }
        if (!resource.exists()){
            throw new ApplicationException("load genesis failed: unable to open genesis file " + genesisPath);
        }
        return objectMapper.readValue(resource.getInputStream(), Genesis.class);
    }
}
