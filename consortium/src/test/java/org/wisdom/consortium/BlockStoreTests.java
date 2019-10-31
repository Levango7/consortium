package org.wisdom.consortium;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wisdom.common.Block;
import org.wisdom.common.BlockStore;
import org.wisdom.common.Header;
import org.wisdom.consortium.dao.Mapping;
import org.wisdom.util.BigEndian;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
// use SPRING_CONFIG_LOCATION environment to set test.yml
// for example: SPRING_CONFIG_LOCATION=classpath:\application.yml,some-path\custom-config.yml
public class BlockStoreTests {
    @Autowired
    private BlockStore blockStore;

    private static final byte[] BYTES = new byte[32];

    private Block getBlock(long height){
        org.wisdom.consortium.entity.Block b = new org.wisdom.consortium.entity.Block(BigEndian.encodeInt64(height), 1, BYTES, BYTES, height, System.currentTimeMillis() / 1000, BYTES);
        org.wisdom.consortium.entity.Transaction.TransactionBuilder builder = org.wisdom.consortium.entity.Transaction.builder().blockHash(BigEndian.encodeInt64(height))
                .from(BYTES).payload(BYTES).to(BYTES)
                .signature(BYTES);
        b.setBody(Arrays.asList(
                builder.position(0).hash((height + "" + 0).getBytes()).build(),
                builder.position(1).hash((height + "" + 1).getBytes()).build(),
                builder.position(2).hash((height + "" + 2).getBytes()).build()
        ));
        return Mapping.getFromBlockEntity(b);
    }

    @Before
    public void saveBlocks(){
        for(int i = 0; i < 10; i++){
            blockStore.writeBlock(getBlock(i));
        }
    }

    @Test
    public void testGetBestBlock(){
        Block best = blockStore.getBestBlock();
        assert best.getHeight() == 9;
        assert best.getBody().size() == 3;
        assert new String(best.getBody().get(0).getHash().getBytes()).equals("90");
        assert new String(best.getBody().get(1).getHash().getBytes()).equals("91");
        assert new String(best.getBody().get(2).getHash().getBytes()).equals("92");
    }

    @Test
    public void testGetBestHeader(){
        Header best = blockStore.getBestHeader();
        assert best.getHeight() == 9L;
        assert best.getVersion() == 1;
        assert Arrays.equals(best.getHash().getBytes(), BigEndian.encodeInt64(9L));
        assert Arrays.equals(best.getHashPrev().getBytes(), BYTES);
        assert Arrays.equals(best.getMerkleRoot().getBytes(), BYTES);
        assert Arrays.equals(best.getPayload().getBytes(), BYTES);
    }

    @Test
    public void testGetHeader(){
        assert blockStore.getHeader(BigEndian.encodeInt64(5)).isPresent();
        assert !blockStore.getHeader(BigEndian.encodeInt64(-1)).isPresent();
    }

    @Test
    public void testGetBlock(){
        assert blockStore.getBlock(BigEndian.encodeInt64(5)).isPresent();
        assert !blockStore.getBlock(BigEndian.encodeInt64(-1)).isPresent();
    }
}
