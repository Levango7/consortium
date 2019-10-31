package org.wisdom.consortium.consensus.poa;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Bytes;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.wisdom.common.*;
import org.wisdom.consortium.consensus.config.ConsensusConfig;
import org.wisdom.consortium.consensus.economic.EconomicModel;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.wisdom.consortium.consensus.event.NewBlockMinedEvent;
import org.wisdom.util.BigEndian;
import org.springframework.stereotype.Component;

@Component
public class PoaMiner implements Miner {

    @Autowired
    private TransactionPool pool;

    @Autowired
    private BlockStore blockStore;

    @Autowired
    private EconomicModel economicModel;

    private EventBus eventBus;

    public PoaMiner() {
        this.eventBus = new EventBus("miner");
        eventBus.register(this);
    }

    @Autowired
    private ConsensusConfig consensusConfig;

    private volatile MineThread thread;

    @Autowired
    private ApplicationContext ctx;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Scheduled(fixedRate = 1000)
    public void tryMine() {
        if (thread != null && !thread.isTerminated()) {
            return;
        }
        if (!consensusConfig.isEnableMining()) {
            return;
        }
        // 判断是否轮到自己出块
        Optional<Proposer> p = consensusConfig.getProposer(blockStore.getBestBlock(), System.currentTimeMillis() / 1000);
        p.ifPresent(proposer -> {
            if (!proposer.getPubkeyHash().equals(consensusConfig.getMinerPubKeyHash())) {
                return;
            }
            try {
                Block b = createBlock();
                thread = ctx.getBean(MineThread.class);
                thread.mine(this.eventBus, b, proposer.getStartTimeStamp(), proposer.getEndTimeStamp());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Transaction createCoinBase(long height) throws DecoderException {
        Transaction tx = new Transaction();
        tx.setAmount(economicModel.getConsensusRewardAtHeight(height));
        tx.setTo(new HexBytes(Hex.decodeHex(consensusConfig.getMinerPubKeyHash().toCharArray())));
        return tx;
    }

    private Block createBlock() throws DecoderException {
        Block parent = blockStore.getBestBlock();
        Block block = new Block();
        block.setVersion(parent.getVersion());
        block.setHashPrev(parent.getHash());
        block.setHeight(parent.getHeight() + 1);
        List<Transaction> txs = new ArrayList<>();
        txs.add(createCoinBase(block.getHeight()));
        List<Transaction> pops = pool.pop(Math.min(pool.size(), consensusConfig.mineBlockMaxSize));
        for (Transaction tx : pops) {
            txs.get(0).setAmount(txs.get(0).getAmount() + tx.getAmount());
            txs.add(tx);
        }
        Transaction coinBaseTx = txs.get(0);
        byte[] bytes = Stream.of(
                new byte[]{(byte) coinBaseTx.getVersion()},
                new byte[]{(byte) coinBaseTx.getType()},
                BigEndian.encodeInt64(coinBaseTx.getAmount()),
                coinBaseTx.getPayload().getBytes(),
                BigEndian.encodeInt64(coinBaseTx.getNonce()),
                coinBaseTx.getFrom().getBytes(),
                coinBaseTx.getTo().getBytes(),
                coinBaseTx.getSignature().getBytes(),
                BigEndian.encodeInt64(coinBaseTx.getGasPrice()),
                coinBaseTx.getPayload().getBytes()
        ).filter(Objects::nonNull).reduce(new byte[0], Bytes::concat);
        txs.get(0).setHash(new HexBytes(consensusConfig.getHashFunction().apply(bytes)));
        block.setBody(txs);
        return block;
    }

    private List<Block> blocks = new ArrayList<>();

    @Subscribe
    public void listen(NewBlockMinedEvent event) {
        Block o = event.getBlock();
        blocks.add(o);
    }


    @Override
    public void subscribe(MinerListener... listeners) {
//        listeners[0].onBlockMined();
    }

    @Override
    public void onBlockWritten(Block block) {
        blocks.remove(block);
    }

    @Override
    public void onNewBestBlock(Block block) {
        blocks.add(block);
    }
}
