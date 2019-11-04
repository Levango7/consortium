package org.wisdom.common;

import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChainCacheWrapper<T extends Chained> extends ChainCache<T> {
    private ReadWriteLock lock;

    public ChainCacheWrapper(int sizeLimit) {
        super(sizeLimit);
        lock = new ReentrantReadWriteLock();
    }

    public ChainCacheWrapper() {
        super();
        lock = new ReentrantReadWriteLock();
    }

    public ChainCacheWrapper(T node) {
        super(node);
        lock = new ReentrantReadWriteLock();
    }

    public ChainCacheWrapper(Collection<? extends T> nodes) {
        super(nodes);
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public Optional<T> get(byte[] hash) {
        lock.readLock().lock();
        try {
            return super.get(hash);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public ChainCache<T> clone() {
        lock.readLock().lock();
        try {
            return super.clone();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<T> getDescendants(T node) {
        lock.readLock().lock();
        try {
            return super.getDescendants(node);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<List<T>> getAllForks() {
        lock.readLock().lock();
        try {
            return super.getAllForks();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void remove(T node) {
        lock.writeLock().lock();
        try{
            super.remove(node);
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public final void remove(Collection<? extends T> nodes) {
        lock.writeLock().lock();
        try{
            super.remove(nodes);
        }finally {
            lock.writeLock().unlock();
        }
    }


    @Override
    public List<T> getLeaves() {
        lock.readLock().lock();
        try {
            return super.getLeaves();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<T> getInitials() {
        lock.readLock().lock();
        try {
            return super.getInitials();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void add(T node) {
        lock.writeLock().lock();
        try {
            super.add(node);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void add(Collection<? extends T> nodes) {
        lock.writeLock().lock();
        try {
            super.add(nodes);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<T> getAll() {
        lock.readLock().lock();
        try {
            return super.getAll();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<T> popLongestChain() {
        lock.writeLock().lock();
        try{
            return super.popLongestChain();
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try{
            return super.size();
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try{
            return super.isEmpty();
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(byte[] hash) {
        lock.readLock().lock();
        try{
            return super.contains(hash);
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<T> getAncestor(T node, long height) {
        lock.readLock().lock();
        try{
            return super.getAncestor(node, height);
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<T> getAncestors(@NonNull T node) {
        lock.readLock().lock();
        try{
            return super.getAncestors(node);
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<T> getChildren(byte[] hash) {
        lock.readLock().lock();
        try{
            return super.getChildren(hash);
        }finally {
            lock.readLock().unlock();
        }
    }
}
