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
import javax.transaction.Transactional;
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

    private Block genesis;

    private List<BlockStoreListener> listeners;

    private void emitNewBlockWritten(Block block) {
        listeners.forEach(x -> x.onBlockWritten(block));
    }

    private void emitNewBestBlock(Block block) {
        listeners.forEach(x -> x.onNewBestBlock(block));
    }

    private Block getBlockFromHeader(Header header) {
        Block b = new Block(header);
        b.setBody(
                transactionDao.findTransactionsByBlockHashOrderByPosition(b.getHash().getBytes())
                        .stream().map(Mapping::getFromTransactionEntity).collect(Collectors.toList())
        );
        return b;
    }

    private List<Block> getBlocksFromHeaders(Collection<Header> headers) {
        List<org.wisdom.consortium.entity.Transaction> transactions = transactionDao.findTransactionsByBlockHashIn(
                headers.stream().map(h -> h.getHash().getBytes()).collect(Collectors.toList())
        );

        Map<String, List<org.wisdom.consortium.entity.Transaction>> transactionLists = new HashMap<>();
        transactions.forEach(t -> {
            String key = HexBytes.encode(t.getBlockHash());
            transactionLists.putIfAbsent(HexBytes.encode(t.getBlockHash()), new ArrayList<>());
            transactionLists.get(key).add(t);
        });
        List<Block> blocks = headers.stream().map(Block::new).collect(Collectors.toList());

        for (Block b : blocks) {
            List<org.wisdom.consortium.entity.Transaction> list = transactionLists.get(b.getHash().toString());
            if (list == null) {
                continue;
            }
            list.sort((x, y) -> x.getPosition() - y.getPosition());
            b.setBody(list.stream().map(Mapping::getFromTransactionEntity).collect(Collectors.toList()));
        }
        return blocks;
    }

    // if genesis not exists, write genesis here
    @PostConstruct
    public void init() {

    }

    @Override
    public void subscribe(BlockStoreListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    @Override
    public Block getGenesis() {
        return genesis;
    }

    @Override
    public boolean hasBlock(byte[] hash) {
        return headerDao.existsById(hash);
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
        return headerDao.findById(hash).map(Mapping::getFromHeaderEntity);
    }

    @Override
    public Optional<Block> getBlock(byte[] hash) {
        return blockDao.findById(hash).map(Mapping::getFromBlockEntity);
    }

    @Override
    public List<Header> getHeaders(long startHeight, int limit) {
        return Mapping.getFromHeaderEntities(headerDao
                .findByHeightGreaterThanEqual(startHeight, PageRequest.of(0, limit)));
    }

    @Override
    public List<Block> getBlocks(long startHeight, int limit) {
        return getBlocksFromHeaders(getHeaders(startHeight, limit));
    }

    @Override
    public List<Header> getHeadersBetween(long startHeight, long stopHeight) {
        return headerDao.findByHeightBetweenOrderByHeight(startHeight, stopHeight).stream()
                .map(Mapping::getFromHeaderEntity).collect(Collectors.toList());
    }

    @Override
    public List<Block> getBlocksBetween(long startHeight, long stopHeight) {
        return getBlocksFromHeaders(getHeadersBetween(startHeight, stopHeight));
    }

    @Override
    public List<Header> getHeadersBetween(long startHeight, long stopHeight, int limit) {
        return Mapping.getFromHeaderEntities(
                headerDao.findByHeightBetweenOrderByHeightAsc(
                        startHeight, startHeight, PageRequest.of(0, limit)
                )
        );
    }

    @Override
    public List<Header> getHeadersBetweenDescend(long startHeight, long stopHeight, int limit) {
        return Mapping.getFromHeaderEntities(
                headerDao.findByHeightBetweenOrderByHeightDesc(
                        startHeight, startHeight, PageRequest.of(0, limit)
                )
        );
    }

    @Override
    public List<Block> getBlocksBetween(long startHeight, long stopHeight, int limit) {
        return getBlocksFromHeaders(getHeadersBetween(startHeight, stopHeight, limit));
    }

    @Override
    public List<Block> getBlocksBetweenDescend(long startHeight, long stopHeight, int limit) {
        return getBlocksFromHeaders(getHeadersBetweenDescend(startHeight, stopHeight, limit));
    }

    @Override
    public Optional<Header> getHeaderByHeight(long height) {
        return headerDao.findByHeight(height).map(Mapping::getFromHeaderEntity);
    }

    @Override
    public Optional<Block> getBlockByHeight(long height) {
        return blockDao.findByHeight(height).map(Mapping::getFromBlockEntity);
    }

    @Override
    public Optional<Header> getAncestorHeader(byte[] hash, long ancestorHeight) {
        Optional<org.wisdom.consortium.entity.Header> header = headerDao.findById(hash);
        return header.map(h -> headerDao.findByHeightBetweenOrderByHeight(ancestorHeight, h.getHeight()))
                .map(ChainCache::new)
                .flatMap(c -> c.getAncestor(header.get(), ancestorHeight))
                .map(Mapping::getFromHeaderEntity);
    }

    @Override
    public Optional<Block> getAncestorBlock(byte[] hash, long ancestorHeight) {
        Optional<Header> header = getAncestorHeader(hash, ancestorHeight);
        return header.map(this::getBlockFromHeader);
    }

    @Override
    public List<Header> getAncestorHeaders(byte[] hash, int limit) {
        Optional<org.wisdom.consortium.entity.Header> header = headerDao.findById(hash);
        return header.map(h ->
                    headerDao.findByHeightBetweenOrderByHeightDesc(
                            header.get().getHeight() - limit + 1, h.getHeight(), PageRequest.of(0, limit)
                    )
        )
                .map(ChainCache::new)
                .map(c -> c.getAncestors(header.get()))
                .map(Mapping::getFromHeaderEntities)
                .orElse(new ArrayList<>());
    }

    @Override
    public List<Block> getAncestorBlocks(byte[] hash, int limit) {
        Optional<Block> block = getBlock(hash);
        return block.map(h ->
                getBlocksBetweenDescend(
                        block.get().getHeight() - limit + 1, h.getHeight(), limit)
                )
                .map(ChainCache::new)
                .map(c -> c.getAncestors(block.get()))
                .orElse(new ArrayList<>());
    }

    @Override
    @Transactional
    public boolean writeBlock(Block block) {
        return blockDao.save(Mapping.getEntityFromBlock(block)) != null;
    }
}
