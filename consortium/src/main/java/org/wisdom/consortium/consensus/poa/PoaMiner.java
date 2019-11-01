package org.wisdom.consortium.consensus.poa;



import com.google.common.primitives.Bytes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.wisdom.common.*;
import org.wisdom.consortium.consensus.config.ConsensusConfig;
import org.wisdom.consortium.consensus.economic.EconomicModel;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.wisdom.util.BigEndian;
import org.springframework.stereotype.Component;

@Slf4j
public class PoaMiner implements Miner {

    private TransactionPool pool;

    @Autowired
    private BlockStore blockStore;

    @Autowired
    private EconomicModel economicModel;

    private List<MinerListener> listeners;

    public PoaMiner() {
        listeners = new ArrayList<>();
    }

    @Autowired
    private ConsensusConfig consensusConfig;

    private volatile CompletableFuture<Void> task;


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Scheduled(fixedRate = 1000)
    public void tryMine() {
        if (task != null && !task.isDone()) {
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
                task = CompletableFuture
                        .supplyAsync(() -> MineThread.pow(b, proposer.getStartTimeStamp(), proposer.getEndTimeStamp()))
                        .thenAcceptAsync(r -> {
                            if (r.isSuccess()) {
                                listeners.forEach(l -> l.onBlockMined(r.getBlock()));
                                return;
                            }
                            listeners.forEach(l -> l.onMiningFailed(r.getBlock()));
                        })
                ;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Transaction createCoinBase(long height) throws DecoderException {
        Transaction tx = new Transaction();
        tx.setVersion(consensusConfig.DEFAULT_TRANSACTION_VERSION);
        tx.setFrom(new HexBytes(new byte[consensusConfig.PUBLIC_KEY_SIZE]));
        tx.setTo(new HexBytes(new byte[consensusConfig.PUBLIC_KEY_HASH_SIZE]));
        tx.setSignature(new HexBytes(new byte[consensusConfig.SIGNATURE_SIZE]));
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


    @Override
    public void subscribe(MinerListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    @Override
    public void onBlockWritten(Block block) {
    }

    @Override
    public void onNewBestBlock(Block block) {
    }
}
