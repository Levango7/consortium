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
import java.util.List;

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

    private void assertHeader(Header header){
        assert header.getVersion() == 1;
        assert Arrays.equals(header.getHash().getBytes(), BigEndian.encodeInt64(header.getHeight()));
        assert Arrays.equals(header.getHashPrev().getBytes(), BYTES);
        assert Arrays.equals(header.getMerkleRoot().getBytes(), BYTES);
        assert Arrays.equals(header.getPayload().getBytes(), BYTES);
    }

    private void assertBody(Block block){
        for(int i = 0; i < block.getBody().size(); i++){
            assert new String(block.getBody().get(i).getHash().getBytes()).equals(block.getHeight() + "" + i);
        }
    }

    private void assertBlock(Block block){
        assertHeader(block.getHeader());
        assertBody(block);
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
        assertBlock(best);
    }

    @Test
    public void testGetBestHeader(){
        Header best = blockStore.getBestHeader();
        assert best.getHeight() == 9L;
        assertHeader(best);
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

    @Test
    public void testGetHeaders(){
        assert blockStore.getHeaders(0, 10).size() == 10;
        assert blockStore.getHeaders(0, 0).size() == 0;
        assert blockStore.getHeaders(0, -1).size() == 10;
    }

    @Test
    public void testGetBlocks(){
        List<Block> blocks = blockStore.getBlocks(0, 10);
        assert blocks.size() == 10;
        assert blockStore.getBlocks(0, 0).size() == 0;
        assert blockStore.getBlocks(0, -1).size() == 10;
        blocks.forEach(this::assertBlock);
    }

    @Test
    public void testGetHeadersBetween(){
        List<Header> headers = blockStore.getHeadersBetween(0, 9);
        assert headers.size() == 10;
        headers.forEach(this::assertHeader);
        assert blockStore.getHeadersBetween(0, 0).size() == 1;
        assert blockStore.getHeadersBetween(0, -1).size() == 0;
        assert blockStore.getHeadersBetween(0, 9, 0).size() == 0;
        assert blockStore.getHeadersBetween(0, 9, -1).size() == 10;
        assert blockStore.getHeadersBetween(0, 9, 10).size() == 10;
    }

    @Test
    public void testGetBlocksBetween(){
        List<Block> blocks = blockStore.getBlocksBetween(0, 9);
        assert blocks.size() == 10;
        blocks.forEach(this::assertBlock);
        assert blockStore.getBlocksBetween(0, 0).size() == 1;
        assert blockStore.getBlocksBetween(0, -1).size() == 0;
        assert blockStore.getBlocksBetween(0, 9, 0).size() == 0;
        assert blockStore.getBlocksBetween(0, 9, -1).size() == 10;
        assert blockStore.getBlocksBetween(0, 9, 10).size() == 10;
    }

}
