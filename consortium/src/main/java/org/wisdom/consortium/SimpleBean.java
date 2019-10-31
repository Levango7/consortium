package org.wisdom.consortium;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.stereotype.Component;
import org.wisdom.consortium.dao.BlockDao;
import org.wisdom.consortium.entity.Block;
import org.wisdom.consortium.entity.HeaderAdapter;
import org.wisdom.consortium.service.BlockStoreService;
import org.wisdom.util.BigEndian;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;

@Component
public class SimpleBean {
    private static final byte[] BYTES = new byte[32];

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private BlockStoreService blockStoreService;

    @PostConstruct
    public void init(){
        for(int i = 0; i < 100; i++){
            blockDao.save(getBlock(i));
        }
        blockDao.findById(BigEndian.encodeInt64(1)).ifPresent(x -> {
            System.out.println(x.getBody() == null);
            System.out.println(x.getBody().size());
        });
    }

    private Block getBlock(long height){
        Block b = new Block(BigEndian.encodeInt64(height), 1, BYTES, BYTES, height, System.currentTimeMillis() / 1000, BYTES);
        b.setBody(new ArrayList<>());
        return b;
    }
}
