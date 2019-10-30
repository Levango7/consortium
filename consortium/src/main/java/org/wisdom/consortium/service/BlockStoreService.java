package org.wisdom.consortium.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.wisdom.common.*;
import org.wisdom.consortium.dao.BlockDao;
import org.wisdom.consortium.dao.HeaderDao;
import org.wisdom.consortium.dao.Mapping;
import org.wisdom.consortium.dao.TransactionDao;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlockStoreService implements BlockStore {
    @Autowired
    private BlockDao blockDao;

    @Autowired
    private HeaderDao headerDao;

    @Autowired
    private TransactionDao transactionDao;

    private List<BlockStoreListener> listeners;

    private void emitNewBlockWritten(Block block){
        listeners.forEach(x -> x.onBlockWritten(block));
    }

    private void emitNewBestBlock(Block block){
        listeners.forEach(x -> x.onNewBestBlock(block));
    }

    private void getBlocksFromHeaders(Collection<Block> headers){
        List<org.wisdom.consortium.entity.Transaction> transactions = transactionDao.findTransactionsByBlockHashIn(
                headers.stream().map(h -> h.getHash().getBytes()).collect(Collectors.toList())
        );

        Map<String, List<org.wisdom.consortium.entity.Transaction>> transactionLists = new HashMap<>();
        transactions.forEach(t -> {
            String key = HexBytes.encode(t.getBlockHash());
            transactionLists.putIfAbsent(HexBytes.encode(t.getBlockHash()), new ArrayList<>());
            transactionLists.get(key).add(t);
        });

        for(Block b: headers){
            List<org.wisdom.consortium.entity.Transaction> list = transactionLists.get(b.getHash().toString());
            if (list == null){
                continue;
            }
            list.sort((x, y) -> x.getPosition() - y.getPosition());
            b.setBody(list.stream().map(Mapping::getFromTransactionEntity).collect(Collectors.toList()));
        }
    }

    @PostConstruct
    public void init(){

    }

    @Override
    public void subscribe(BlockStoreListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    @Override
    public Block getGenesis() {
        return Mapping.getFromBlockEntity(blockDao.findTopByOrderByHeightAsc().get());
    }

    @Override
    public boolean hasBlock(byte[] hash) {
        return headerDao.getByHash(hash).isPresent();
    }

    @Override
    public Header getBestHeader() {
        return Mapping.getFromHeaderEntity(headerDao.findTopByOrderByHeightAsc().get());
    }

    @Override
    public Block getBestBlock() {
        return Mapping.getFromBlockEntity(blockDao.findTopByOrderByHeightAsc().get());
    }

    @Override
    public Optional<Header> getHeader(byte[] hash) {
        return headerDao.getByHash(hash).map(Mapping::getFromHeaderEntity);
    }

    @Override
    public Optional<Block> getBlock(byte[] hash) {
        return blockDao.getByHash(hash).map(Mapping::getFromBlockEntity);
    }

    @Override
    public List<Header> getHeaders(long startHeight, int limit) {
        return null;
    }

    @Override
    public List<Block> getBlocks(long startHeight, int limit) {
        return null;
    }

    @Override
    public List<Header> getHeadersBetween(long startHeight, long stopHeight) {
        return headerDao.getHeadersByHeightBetween(startHeight, stopHeight).stream()
                .map(Mapping::getFromHeaderEntity).collect(Collectors.toList());
    }

    @Override
    public List<Block> getBlocksBetween(long startHeight, long stopHeight) {
        List<Block> blocks = getHeadersBetween(startHeight, stopHeight)
                .stream().map(Block::new).collect(Collectors.toList());
        getBlocksFromHeaders(blocks);
        return blocks;
    }

    @Override
    public List<Header> getHeadersBetween(long startHeight, long stopHeight, int limit, boolean descend) {
        return null;
    }

    @Override
    public List<Block> getBlocksBetween(long startHeight, long stopHeight, int limit, boolean descend) {
        return null;
    }

    @Override
    public Optional<Header> getHeaderByHeight(long height) {
        return Optional.empty();
    }

    @Override
    public Optional<Block> getBlockByHeight(long height) {
        return Optional.empty();
    }

    @Override
    public Optional<Header> getAncestorHeader(byte[] hash, long ancestorHeight) {
        return Optional.empty();
    }

    @Override
    public Block getAncestorBlock(byte[] hash, long ancestorHeight) {
        return null;
    }

    @Override
    public List<Header> getAncestorHeaders(byte[] hash, int limit) {
        return null;
    }

    @Override
    public List<Block> getAncestorBlocks(byte[] hash, int limit) {
        return null;
    }

    @Override
    public boolean writeBlock(Block block) {
        return false;
    }
}
