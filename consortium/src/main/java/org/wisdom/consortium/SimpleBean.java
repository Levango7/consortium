package org.wisdom.consortium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Component;
import org.wisdom.consortium.dao.BlockDao;
import org.wisdom.consortium.dao.Mapping;
import org.wisdom.consortium.entity.Block;
import org.wisdom.consortium.entity.HeaderAdapter;
import org.wisdom.consortium.entity.Transaction;
import org.wisdom.consortium.service.BlockStoreService;
import org.wisdom.util.BigEndian;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;

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
