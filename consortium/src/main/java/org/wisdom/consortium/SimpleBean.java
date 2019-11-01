package org.wisdom.consortium;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wisdom.consortium.config.ConsortiumConfig;
import org.wisdom.consortium.consensus.config.Genesis;
import org.wisdom.consortium.dao.BlockDao;
import org.wisdom.consortium.service.BlockStoreService;

import javax.annotation.PostConstruct;

@Component
public class SimpleBean {

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private BlockStoreService blockStoreService;

    @Autowired
    private Genesis genesis;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsortiumConfig consortiumConfig;

    @PostConstruct
    public void init() throws JsonProcessingException {
//        for(int i = 0; i< 100; i++){
//            blockStoreService.writeBlock(
//                    Mapping.getFromBlockEntity(getBlock(i))
//            );
//        }
//        blockStoreService.getHeadersBetween(1, 100);
        System.out.println(objectMapper.writeValueAsString(genesis));
        System.out.println(consortiumConfig.getConsensus().getGenesis());
    }


}
