package org.wisdom.consortium;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wisdom.common.Block;
import org.wisdom.common.BlockStore;
import org.wisdom.common.Transaction;
import org.wisdom.common.TransactionStore;
import org.wisdom.util.BigEndian;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.wisdom.consortium.TestUtils.BYTES;
import static org.wisdom.consortium.TestUtils.getBlock;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Start.class)
// use SPRING_CONFIG_LOCATION environment to locate spring config
// for example: SPRING_CONFIG_LOCATION=classpath:\application.yml,some-path\custom-config.yml
public class TransactionStoreTests {
    @Autowired
    private TransactionStore transactionStore;

    @Autowired
    private BlockStore blockStore;

    private void assertTransaction(Transaction transaction){
        String h = Long.toString(BigEndian.decodeInt64(transaction.getBlockHash().getBytes()));
        assert Long.toString(transaction.getHeight()).equals(h);
        assert new String(transaction.getHash().getBytes()).startsWith(h);
        assert transaction.getVersion() == 0;
        assert Arrays.equals(transaction.getFrom().getBytes(), BYTES) &&
                Arrays.equals(transaction.getPayload().getBytes(), BYTES) &&
                Arrays.equals(transaction.getTo().getBytes(), BYTES) &&
                Arrays.equals(transaction.getSignature().getBytes(), BYTES);
    }

    @Before
    public void saveBlocks() {
        if (blockStore.getBlockByHeight(0).isPresent()){
            return;
        }
        for (int i = 0; i < 10; i++) {
            Block b = getBlock(i);
            assert !blockStore.hasBlock(b.getHash().getBytes());
            blockStore.writeBlock(b);
        }
    }

    @Test
    public void test(){
        assert transactionStore != null;
        assert blockStore != null;
    }

    @Test
    public void testHasTransaction(){
        for (int i = 0; i < 10; i++) {
            for(int j = 0; j < 3; j++){
                assert transactionStore.hasTransaction((i + "" + j ).getBytes());
            }
        }
        assert !transactionStore.hasTransaction((1 + "" + 1000 ).getBytes());
    }

    @Test
    public void testHasPayload(){
        assert transactionStore.hasPayload(BYTES);
        assert !transactionStore.hasPayload(new byte[]{-1});
    }

    @Test
    public void testGetTransactionByHash(){
        Optional<Transaction> o = transactionStore.getTransactionByHash("00".getBytes());
        assert o.isPresent();
        assertTransaction(o.get());
    }

    @Test
    public void testGetTransactionByFrom(){
        assert transactionStore.getTransactionsByFrom(BYTES, 0, Integer.MAX_VALUE).size() == 30;
        List<Transaction> transactions = transactionStore.getTransactionsByFrom(BYTES, 0, 3);
        assert transactions.size() == 3;
        transactions.forEach(t -> {
            assert t.getHeight() == 0;
//            assertTransaction(t);
        });
    }
}
