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

    public Optional<T> get(byte[] hash) {
        return cache.get(hash).map(ChainedState::get);
    }

    @Override
    public void update(Block b) {
        Optional<ChainedState<T>> s = cache.get(b.getHashPrev().getBytes());
        if (!s.isPresent()) throw new RuntimeException("state not found at " + b.getHashPrev());
        ChainedState<T> copied = s.get().clone();
        try {
            copied.update(b);
        } catch (StateUpdateException e) {
            e.printStackTrace();
        }
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
    public void confirm(byte[] hash) {
        HexBytes h = new HexBytes(hash);
        List<ChainedState<T>> children = cache.getChildren(where.getBytes());
        Optional<ChainedState<T>> o = children.stream().filter(x -> x.getHash().equals(h)).findFirst();
        if(!o.isPresent()){
            throw new RuntimeException("the state at "
                    + h +
                    " to confirm not found or confirmed block is not child of current node"
            );
        }
        ChainedState<T> s = o.get();
        children.stream().filter(x -> !x.getHash().equals(h))
                .forEach(n -> cache.removeDescendants(n.getHash().getBytes()));
        Optional<ChainedState<T>> root = cache.get(where.getBytes());
        if (!root.isPresent()){
            throw new RuntimeException("confirmed state missing");
        }
        cache.remove(root.get().getHash().getBytes());
        where = h;
    }

    @Override
    public T getLastConfirmed() {
        Optional<ChainedState<T>> root = cache.get(where.getBytes());
        if (!root.isPresent()){
            throw new RuntimeException("confirmed state missing");
        }
        return root.get().get();
    }
}
