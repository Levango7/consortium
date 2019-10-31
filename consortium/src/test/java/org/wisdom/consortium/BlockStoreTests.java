package org.wisdom.consortium;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wisdom.common.BlockStore;
import org.wisdom.consortium.dao.Mapping;
import org.wisdom.consortium.entity.Block;
import org.wisdom.consortium.entity.Transaction;
import org.wisdom.util.BigEndian;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class) // use SPRING_CONFIG_LOCATION environment to set test.yml
public class BlockStoreTests {
    @Autowired
    private BlockStore blockStore;

    private static final byte[] BYTES = new byte[32];

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

    @Test
    public void testSaveBlocks(){
        for(int i = 0; i < 100; i++){
            blockStore.writeBlock(Mapping.getFromBlockEntity(getBlock(i)));
        }
    }

    @Test
    public void testGetBlocks(){
        for(int i = 0; i < 100; i++){
            blockStore.writeBlock(Mapping.getFromBlockEntity(getBlock(i)));
        }
        assert blockStore.getBestBlock().getHeight() == 99;
    }
}
