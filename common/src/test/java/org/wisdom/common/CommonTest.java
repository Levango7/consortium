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

        Header header = mapper.readValue("{\"timeStamp\":\"100\"}", Header.class);
        Block block = mapper.readValue("{\"timeStamp\":\"100\", \"body\" : [\"a\"] }", Block.class);
        System.out.println(header.getTimeStamp());
        System.out.println(block.getTimeStamp());
        System.out.println(block.getBody().get(0));
    }
}
