package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.wisdom.consortium.config.ConsortiumConfig;
import org.wisdom.consortium.consensus.poa.config.Genesis;
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

    private Resource getResource(String path) throws ApplicationException {
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            resource = new ClassPathResource(path);
        }
        if (!resource.exists()) {
            throw new ApplicationException("resource " + path + " not found");
        }
        return resource;
    }

    public Genesis genesis(ConsortiumConfig config, ObjectMapper objectMapper)
            throws Exception {
        return objectMapper.readValue(
                getResource(
                        config.getConsensus().getGenesis()).getInputStream(),
                Genesis.class);
    }
}
