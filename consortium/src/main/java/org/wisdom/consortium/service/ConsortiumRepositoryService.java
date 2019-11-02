package org.wisdom.consortium.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wisdom.common.*;
import org.wisdom.exception.GenesisConflictsException;
import org.wisdom.exception.WriteGenesisFailedException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ConsortiumRepositoryService implements ConsortiumRepository {
    @Autowired
    private BlockRepositoryService blockStoreService;

    @Autowired
    private TransactionRepositoryService transactionStoreService;

    @Override
    public Block getLastConfirmed() {
        return null;
    }

    @Override
    public List<Block> getUnconfirmed() {
        return null;
    }

    @Override
    public void setProvider(ConfirmedBlocksProvider provider) {

    }

    @Override
    public void addListeners(BlockRepositoryListener... listeners) {
        blockStoreService.addListeners(listeners);
    }

    @Override
    public void saveGenesis(Block block) throws GenesisConflictsException, WriteGenesisFailedException {
        blockStoreService.saveGenesis(block);
    }

    @Override
    public Block getGenesis() {
        return blockStoreService.getGenesis();
    }

    @Override
    public boolean hasBlock(byte[] hash) {
        return blockStoreService.hasBlock(hash);
    }

    @Override
    public Header getBestHeader() {
        return blockStoreService.getBestHeader();
    }

    @Override
    public Block getBestBlock() {
        return blockStoreService.getBestBlock();
    }

    @Override
    public Optional<Header> getHeader(byte[] hash) {
        return blockStoreService.getHeader(hash);
    }

    @Override
    public Optional<Block> getBlock(byte[] hash) {
        return blockStoreService.getBlock(hash);
    }

    @Override
    public List<Header> getHeaders(long startHeight, int limit) {
        return blockStoreService.getHeaders(startHeight, limit);
    }

    @Override
    public List<Block> getBlocks(long startHeight, int limit) {
        return blockStoreService.getBlocks(startHeight, limit);
    }

    @Override
    public List<Header> getHeadersBetween(long startHeight, long stopHeight) {
        return blockStoreService.getHeadersBetween(startHeight, stopHeight);
    }

    @Override
    public List<Block> getBlocksBetween(long startHeight, long stopHeight) {
        return blockStoreService.getBlocksBetween(startHeight, stopHeight);
    }

    @Override
    public List<Header> getHeadersBetween(long startHeight, long stopHeight, int limit) {
        return blockStoreService.getHeadersBetween(startHeight, stopHeight, limit);
    }

    @Override
    public List<Header> getHeadersBetweenDescend(long startHeight, long stopHeight, int limit) {
        return blockStoreService.getHeadersBetweenDescend(startHeight, stopHeight, limit);
    }

    @Override
    public List<Block> getBlocksBetween(long startHeight, long stopHeight, int limit) {
        return blockStoreService.getBlocksBetween(startHeight, stopHeight, limit);
    }

    @Override
    public List<Block> getBlocksBetweenDescend(long startHeight, long stopHeight, int limit) {
        return blockStoreService.getBlocksBetweenDescend(startHeight, stopHeight, limit);
    }

    @Override
    public Optional<Header> getHeaderByHeight(long height) {
        return blockStoreService.getHeaderByHeight(height);
    }

    @Override
    public Optional<Block> getBlockByHeight(long height) {
        return blockStoreService.getBlockByHeight(height);
    }

    @Override
    public Optional<Header> getAncestorHeader(byte[] hash, long ancestorHeight) {
        return blockStoreService.getAncestorHeader(hash, ancestorHeight);
    }

    @Override
    public Optional<Block> getAncestorBlock(byte[] hash, long ancestorHeight) {
        return blockStoreService.getAncestorBlock(hash, ancestorHeight);
    }

    @Override
    public List<Header> getAncestorHeaders(byte[] hash, int limit) {
        return blockStoreService.getAncestorHeaders(hash, limit);
    }

    @Override
    public List<Block> getAncestorBlocks(byte[] hash, int limit) {
        return blockStoreService.getAncestorBlocks(hash, limit);
    }

    @Override
    @Transactional
    public boolean writeBlock(Block block) {
        return blockStoreService.writeBlock(block);
    }

    @Override
    public boolean hasTransaction(@NonNull byte[] hash) {
        return transactionStoreService.hasTransaction(hash);
    }

    @Override
    public boolean hasPayload(@NonNull byte[] payload) {
        return transactionStoreService.hasPayload(payload);
    }

    @Override
    public Optional<Transaction> getTransactionByHash(byte[] hash) {
        return transactionStoreService.getTransactionByHash(hash);
    }

    @Override
    public List<Transaction> getTransactionsByFrom(byte[] from, int page, int size) {
        return transactionStoreService.getTransactionsByFrom(from, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByFromAndType(byte[] from, int type, int page, int size) {
        return transactionStoreService.getTransactionsByFromAndType(from, type, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByTo(byte[] to, int page, int size) {
        return transactionStoreService.getTransactionsByTo(to, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByToAndType(byte[] to, int type, int page, int size) {
        return transactionStoreService.getTransactionsByToAndType(to, type, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByFromAndTo(byte[] from, byte[] to, int page, int size) {
        return transactionStoreService.getTransactionsByFromAndTo(from, to, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByFromAndToAndType(byte[] from, byte[] to, int type, int page, int size) {
        return transactionStoreService.getTransactionsByFromAndToAndType(from, to, type, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByPayload(byte[] payload, int page, int size) {
        return transactionStoreService.getTransactionsByPayload(payload, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByPayloadAndType(byte[] payload, int type, int page, int size) {
        return transactionStoreService.getTransactionsByPayloadAndType(payload, type, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByBlockHash(byte[] blockHash, int page, int size) {
        return transactionStoreService.getTransactionsByBlockHash(blockHash, page, size);
    }

    @Override
    public List<Transaction> getTransactionsByBlockHeight(long height, int page, int size) {
        return transactionStoreService.getTransactionsByBlockHeight(height, page, size);
    }
}
