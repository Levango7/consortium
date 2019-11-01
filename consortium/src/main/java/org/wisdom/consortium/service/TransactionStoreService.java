package org.wisdom.consortium.service;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.wisdom.common.Transaction;
import org.wisdom.common.TransactionStore;
import org.wisdom.consortium.dao.Mapping;
import org.wisdom.consortium.dao.TransactionDao;

import java.util.List;
import java.util.Optional;

@Service
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
    public List<Transaction> getTransactionsByFrom(byte[] from, int page, int size) {
        return Mapping.getFromTransactionEntities(
                transactionDao.findByFromOrderByHeightAscPositionAsc(from, PageRequest.of(page, size))
        );
    }

    @Override
    public List<Transaction> getTransactionsByFromAndType(byte[] from, int type, int page, int size) {
        return Mapping.getFromTransactionEntities(
                transactionDao.findByFromAndType(from, type, PageRequest.of(page, size))
        );
    }

    @Override
    public List<Transaction> getTransactionsByTo(byte[] to, int page, int size) {
        return Mapping.getFromTransactionEntities(transactionDao.findByTo(to, PageRequest.of(page, size)));
    }

    @Override
    public List<Transaction> getTransactionsByToAndType(byte[] to, int type , int page, int size) {
        return Mapping.getFromTransactionEntities(transactionDao.findByToAndType(to, type, PageRequest.of(page, size)));
    }

    @Override
    public List<Transaction> getTransactionsByFromAndTo(byte[] from, byte[] to, int page, int size) {
        return Mapping.getFromTransactionEntities(transactionDao.findByFromAndTo(from, to, PageRequest.of(page, size)));
    }

    @Override
    public List<Transaction> getTransactionsByFromAndToAndType(byte[] from, byte[] to, int type, int page, int size) {
        return Mapping.getFromTransactionEntities(
                transactionDao.findByFromAndToAndType(from, to, type, PageRequest.of(page, size))
        );
    }

    @Override
    public List<Transaction> getTransactionsByPayload(byte[] payload) {
        return Mapping.getFromTransactionEntities(transactionDao.findByPayload(payload));
    }

    @Override
    public List<Transaction> getTransactionsByBlockHash(byte[] blockHash) {
        return Mapping.getFromTransactionEntities(transactionDao.findByBlockHashOrderByPosition(blockHash));
    }

    @Override
    public List<Transaction> getTransactionsByBlockHeight(long height) {
        return Mapping.getFromTransactionEntities(transactionDao.findByHeightOrderByPosition(height));
    }
}
