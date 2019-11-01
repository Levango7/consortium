package org.wisdom.consortium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.wisdom.consortium.dao.BlockDao;
import org.wisdom.consortium.service.BlockStoreService;

import javax.annotation.PostConstruct;

@Component
public class SimpleBean {

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private BlockStoreService blockStoreService;

    @PostConstruct
    public void init(){
//        for(int i = 0; i< 100; i++){
//            blockStoreService.writeBlock(
//                    Mapping.getFromBlockEntity(getBlock(i))
//            );
//        }
//        blockStoreService.getHeadersBetween(1, 100);
    }


}
