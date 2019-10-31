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
    private static final byte[] BYTES = new byte[32];

    @Autowired
    private BlockDao blockDao;

    @Autowired
    private BlockStoreService blockStoreService;

    @PostConstruct
    public void init(){
        blockStoreService.writeBlock(
                Mapping.getFromBlockEntity(getBlock(0))
        );
    }

    private Block getBlock(long height){
        Block b = new Block(BigEndian.encodeInt64(height), 1, BYTES, BYTES, height, System.currentTimeMillis() / 1000, BYTES);
        Transaction.TransactionBuilder builder = Transaction.builder().blockHash(BigEndian.encodeInt64(height))
                .from(BYTES).payload(BYTES).to(BYTES)
                .signature(BYTES);
        b.setBody(Arrays.asList(
                builder.position(0).hash((height + "" + 0).getBytes()).build(),
                builder.position(1).hash((height + "" + 1).getBytes()).build(),
                builder.position(2).hash((height + "" + 2).getBytes()).build()
        ));
        return b;
    }
}
