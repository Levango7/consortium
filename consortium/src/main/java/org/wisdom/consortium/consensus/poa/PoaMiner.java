package org.wisdom.consortium.consensus.poa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.wisdom.common.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.wisdom.consortium.ApplicationConstants;
import org.wisdom.consortium.config.ConsortiumConfig;
import org.wisdom.consortium.consensus.poa.config.Genesis;
import org.wisdom.consortium.exception.ApplicationException;
import org.wisdom.util.BigEndian;

import javax.annotation.PostConstruct;

@Slf4j
@ConditionalOnProperty(
        name = ApplicationConstants.CONSENSUS_NAME_PROPERTY,
        havingValue = ApplicationConstants.CONSENSUS_POA
)
@Component
public class PoaMiner implements Miner {
    @Getter
    @AllArgsConstructor
    public static class MinedResult {
        private boolean success;
        private Block block;
        private String reason;
    }

    private ConsortiumConfig.ConsensusConfig consensusConfig;

    @Autowired
    public void setConsensusConfig(ConsortiumConfig consortiumConfig) {
        consensusConfig = consortiumConfig.getConsensus();
    }

    @Autowired
    private BlockStore blockStore;

    @Autowired
    private Genesis genesis;

    private List<MinerListener> listeners;

    public PoaMiner() {
        listeners = new ArrayList<>();
    }

    public Optional<Proposer> getProposer(Block parent, long currentTimeSeconds) {
        if (genesis.miners.size() == 0) {
            return Optional.empty();
        }
        if (parent.getHeight() == 0) {
            return Optional.of(new Proposer(genesis.miners.get(0).address, 0, Long.MAX_VALUE));
        }
        if (parent.getBody() == null ||
                parent.getBody().size() == 0 ||
                parent.getBody().get(0).getTo() == null
        ) return Optional.empty();
        String prev = new String(parent.getBody().get(0).getTo().getBytes(), StandardCharsets.UTF_8);
        int prevIndex = genesis.miners.stream().map(x -> x.address).collect(Collectors.toList()).indexOf(prev);
        if (prevIndex < 0) {
            return Optional.empty();
        }

        long step = (currentTimeSeconds - parent.getCreatedAt())
                / consensusConfig.getBlockInterval() + 1;

        int currentIndex = (int) (prevIndex + step) % genesis.miners.size();
        long endTime = parent.getCreatedAt() + step * consensusConfig.getBlockInterval();
        long startTime = endTime - consensusConfig.getBlockInterval();
        return Optional.of(new Proposer(
                genesis.miners.get(currentIndex).address,
                startTime,
                endTime
        ));
    }

    @PostConstruct
    public void init() throws Exception{
        log.info("PoA miner loaded");
        Block b = genesis.getBlock();
        Optional<Block> o = blockStore.getBlockByHeight(0);
        if (!o.isPresent()){
            blockStore.writeBlock(b);
            return;
        }
        Block another = o.get();
        if (!b.getHash().equals(another.getHash())){
            throw new ApplicationException("the genesis in database not equals " + consensusConfig.getGenesis());
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Scheduled(fixedRate = 1000)
    public void tryMine() {
        if (!consensusConfig.isEnableMining()) {
            return;
        }
        String coinBase = consensusConfig.getMinerCoinBase();
        Block best = blockStore.getBestBlock();
        // 判断是否轮到自己出块
        Optional<Proposer> o = getProposer(
                best,
                System.currentTimeMillis() / 1000
        ).filter(p -> p.getAddress().equals(coinBase));
        if (!o.isPresent()) return;
        log.info("try to mining at height " + best.getHeight() + 1);
        try {
            Block b = createBlock(blockStore.getBestBlock());
            log.info("mining success");
            listeners.forEach(l -> l.onBlockMined(b));
            Assert.isTrue(b.getHash().equals(new HexBytes(PoAUtils.getHash(b))), "block hash is equal");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Transaction createCoinBase(long height) throws DecoderException {
        Transaction tx = Transaction.builder()
                .height(height)
                .version(PoAConstants.TRANSACTION_VERSION)
                .createdAt(System.currentTimeMillis() / 1000)
                .nonce(height)
                .from(PoAConstants.ZERO_BYTES)
                .amount(EconomicModelImpl.getConsensusRewardAtHeight(height))
                .payload(PoAConstants.ZERO_BYTES)
                .to(new HexBytes(consensusConfig.getMinerCoinBase().getBytes(StandardCharsets.UTF_8)))
                .signature(PoAConstants.ZERO_BYTES).build();
        tx.setHash(new HexBytes(PoAUtils.getHash(tx)));
        return tx;
    }

    private Block createBlock(Block parent) throws DecoderException {
        Header header = Header.builder()
                .version(parent.getVersion())
                .hashPrev(parent.getHash())
                .merkleRoot(PoAConstants.ZERO_BYTES)
                .height(parent.getHeight() + 1)
                .createdAt(System.currentTimeMillis() / 1000)
                .payload(PoAConstants.ZERO_BYTES)
                .hash(new HexBytes(BigEndian.encodeInt64(parent.getHeight() + 1))).build();
        Block b = new Block(header);
        b.getBody().add(createCoinBase(parent.getHeight() + 1));
        b.setHash(new HexBytes(PoAUtils.getHash(b)));
        return b;
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

    public static class EconomicModelImpl {

        private static final long INITIAL_SUPPLY = 20;

        private static final long HALF_PERIOD = 10000000;


        public static long getConsensusRewardAtHeight(long height) {
            long era = height / HALF_PERIOD;
            long reward = INITIAL_SUPPLY;
            for (long i = 0; i < era; i++) {
                reward = reward * 52218182 / 100000000;
            }
            return reward;
        }

    }
}
