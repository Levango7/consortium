package org.wisdom.consortium.service;

import org.wisdom.common.Transaction;
import org.wisdom.common.TransactionStore;

import java.util.List;
import java.util.Optional;

public class TransactionStoreService implements TransactionStore {
    @Override
    public boolean hasTransaction(byte[] hash) {
        return false;
    }

    @Override
    public boolean hasPayload(byte[] payload) {
        return false;
    }

    @Override
    public Optional<Transaction> getTransactionByHash(byte[] hash) {
        return Optional.empty();
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
