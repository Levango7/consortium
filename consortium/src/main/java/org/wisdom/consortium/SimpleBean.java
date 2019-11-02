package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wisdom.consortium.dao.BlockDao;
import org.wisdom.consortium.service.BlockRepositoryService;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class SimpleBean {

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private BlockRepositoryService blockStoreService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() throws JsonProcessingException {
        log.info("config loaded success");
    }
}
