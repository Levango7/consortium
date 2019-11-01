package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
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
import org.wisdom.consortium.consensus.config.Genesis;

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
    public Genesis genesis(@Value("${consortium.consensus.genesis}") String genesis, ObjectMapper objectMapper)
            throws Exception {
        Resource resource = new FileSystemResource(genesis);
        if (!resource.exists()) {
            resource = new ClassPathResource(genesis);
        }
        return objectMapper.readValue(resource.getInputStream(), Genesis.class);
    }

}
