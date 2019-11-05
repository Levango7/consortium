package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.util.List;
import java.util.Optional;

public class InMemoryStateFactory<T extends State<T>> implements StateFactory<T> {
    private ChainCache<ChainedState<T>> cache;
    private HexBytes where;

    public InMemoryStateFactory(Block genesis, T state) {
        this.cache = new ChainCache<>();
        cache.add(new ChainedState<>(genesis.getHashPrev(), genesis.getHash(), state));
        this.where = genesis.getHash();
    }

    public Optional<T> get(Block block) {
        return cache.get(block.getHash().getBytes()).map(ChainedState::getState);
    }

    @Override
    public void update(Block b) {
        Optional<ChainedState<T>> s = cache.get(b.getHashPrev().getBytes());
        if (!s.isPresent()) throw new RuntimeException("state not found at " + b.getHashPrev());
        ChainedState<T> copied = s.get().clone();
        for (Transaction tx : b.getBody()) {
            try {
                copied.update(b, tx);
            } catch (StateUpdateException e) {
                // this should never happen, for the block b had been validated
                throw new RuntimeException(e.getMessage());
            }
        }
        cache.add(copied);
    }

    @Override
    public void update(Block b, T state) {
        cache.add(new ChainedState<>(b.getHashPrev(), b.getHash(), state));
    }

    @Override
    public void confirm(Block b) {
        if (!b.getHashPrev().equals(where)) {
            throw new RuntimeException("confirmed block is not child of current root node");
        }
        List<ChainedState<T>> children = cache.getChildren(where.getBytes());
        // clear
        for (ChainedState<T> node : children) {
            if (!node.getHash().equals(b.getHash())) {
                cache.remove(cache.getDescendants(node));
            }
        }
        cache.get(where.getBytes()).ifPresent(n -> cache.remove(n));
        where = b.getHash();
    }
}
