package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.util.List;
import java.util.Optional;

public class InMemoryStateFactory<T extends State<T>> implements StateFactory<T>{
    private ChainCache<T> cache;
    private HexBytes where;

    public InMemoryStateFactory(T genesis) {
        this.cache = new ChainCache<>();
        cache.add(genesis);
        this.where = genesis.getHash();
    }

    public Optional<T> get(Block block) {
        return cache.get(block.getHash().getBytes());
    }

    @Override
    public void update(Block b) {
        Optional<T> s = cache.get(b.getHashPrev().getBytes());
        if(!s.isPresent()) throw new RuntimeException("state not found at " + b.getHashPrev());
        T copied = s.get().clone();
        for(Transaction tx: b.getBody()){
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
    public void confirm(Block b) {
        if (!b.getHashPrev().equals(where)) {
            throw new RuntimeException("confirmed block is not child of current root node");
        }
        List<T> children = cache.getChildren(where.getBytes());
        // clear
        for (T node : children) {
            if (!node.getHash().equals(b.getHash())) {
                cache.remove(cache.getDescendants(node));
                continue;
            }
        }
        where = b.getHash();
    }
}
