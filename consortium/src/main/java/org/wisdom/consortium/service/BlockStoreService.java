package org.wisdom.consortium.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wisdom.common.Block;
import org.wisdom.common.BlockStore;
import org.wisdom.common.BlockStoreListener;
import org.wisdom.common.Header;
import org.wisdom.consortium.dao.BlockDao;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BlockStoreService implements BlockStore {
    @Autowired
    private BlockDao blockDao;

    private List<BlockStoreListener> listeners;

    private void emitNewBlockWritten(Block block){
        listeners.forEach(x -> x.onBlockWritten(block));
    }

    private void emitNewBestBlock(Block block){
        listeners.forEach(x -> x.onNewBestBlock(block));
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
        return blockDao.getBlockByHeight(0).get();
    }

    @Override
    public boolean hasBlock(byte[] hash) {
        return false;
    }

    @Override
    public Header getBestHeader() {
        return null;
    }

    @Override
    public Block getBestBlock() {
        return null;
    }

    @Override
    public Optional<Header> getHeader(byte[] hash) {
        return Optional.empty();
    }

    @Override
    public Optional<Block> getBlock(byte[] hash) {
        return Optional.empty();
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
        return null;
    }

    @Override
    public List<Block> getBlocksBetween(long startHeight, long stopHeight) {
        return null;
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
