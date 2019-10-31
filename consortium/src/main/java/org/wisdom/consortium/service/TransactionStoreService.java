package org.wisdom.consortium.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.wisdom.common.Transaction;
import org.wisdom.common.TransactionStore;
import org.wisdom.consortium.dao.Mapping;
import org.wisdom.consortium.dao.TransactionDao;

import java.util.List;
import java.util.Optional;

public class TransactionStoreService implements TransactionStore {
    @Autowired
    private TransactionDao transactionDao;

    @Override
    public boolean hasTransaction(@NonNull byte[] hash) {
        return transactionDao.existsById(hash);
    }

    @Override
    public boolean hasPayload(@NonNull byte[] payload) {
        return transactionDao.existsByPayload(payload);
    }

    @Override
    public Optional<Transaction> getTransactionByHash(byte[] hash) {
        return transactionDao.findById(hash).map(Mapping::getFromTransactionEntity);
    }

    @Override
    public List<Transaction> getTransactionsByFrom(byte[] from, int offset, int limit) {
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByFromAndType(int type, byte[] from, int offset, int limit) {
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByTo(byte[] to, int offset, int limit) {
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByToAndType(int type, byte[] from, int offset, int limit) {
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByFromAndTo(byte[] from, byte[] to, int offset, int limit) {
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByFromAndToAndType(int type, byte[] from, byte[] to, int offset, int limit) {
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByPayload(byte[] payload) {
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByBlockHash(byte[] blockHash) {
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByBlockHeight(long height) {
        return null;
    }
}
