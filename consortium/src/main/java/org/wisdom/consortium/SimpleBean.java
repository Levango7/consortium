package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wisdom.consortium.config.ConsortiumConfig;
import org.wisdom.consortium.consensus.poa.config.Genesis;
import org.wisdom.consortium.dao.BlockDao;
import org.wisdom.consortium.service.BlockStoreService;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class SimpleBean {

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private BlockStoreService blockStoreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsortiumConfig consortiumConfig;

    @PostConstruct
    public void init() throws JsonProcessingException {
        log.info("config loaded success");
        log.info(objectMapper.writeValueAsString(consortiumConfig));
    }


}
