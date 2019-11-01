package org.wisdom.consortium;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
// use SPRING_CONFIG_LOCATION environment to locate spring config
// for example: SPRING_CONFIG_LOCATION=classpath:\application.yml,some-path\custom-config.yml
public class StartTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void test(){
        assert objectMapper != null;
        System.out.println("===========================================");
    }

    @Test
    public void testSaveBlocks(){

    }
}
