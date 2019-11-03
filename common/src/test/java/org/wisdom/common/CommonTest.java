package org.wisdom.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CommonTest {
    public static ObjectMapper getObjectMapper(){
        ObjectMapper mapper = new ObjectMapper()
                .enable(JsonParser.Feature.ALLOW_COMMENTS);
        SimpleModule module = new SimpleModule();
        return mapper.registerModule(module);
    }

    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper mapper = getObjectMapper();
        System.out.println(mapper.writeValueAsString(
                Header.builder().build()
        ));
        System.out.println(mapper.writeValueAsString(new Block()));

        Header header = mapper.readValue("{\"createdAt\":\"1572766736\"}", Header.class);
        Block block = mapper.readValue("{\"createdAt\":\"1572766736\", \"body\" : [{\"type\": 100}] }", Block.class);
        System.out.println(header.getCreatedAt());
        System.out.println(block.getCreatedAt());
        System.out.println(block.getBody().get(0).getType());
        System.out.println(mapper.writeValueAsString(header));
    }
}
