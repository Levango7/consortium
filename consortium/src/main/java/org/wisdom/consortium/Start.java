package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
public class Start {

    public static void main(String[] args) {
        SpringApplication.run(Start.class, args);
    }

    @Bean
    public ObjectMapper getObjectMapper(){
        ObjectMapper mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .enable(JsonParser.Feature.ALLOW_COMMENTS);
        SimpleModule module = new SimpleModule();
        return mapper.registerModule(module);
    }
}
