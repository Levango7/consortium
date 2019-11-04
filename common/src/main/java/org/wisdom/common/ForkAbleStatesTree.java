package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.util.*;

/**
 * State tree for account related object storage
 */
public class ForkAbleStatesTree<T extends ForkAbleState<T>> {
    private ForkAbleStateSets<T> root;
    private ChainCache<ForkAbleStateSets<T>> cache;

    // where of the root
    private HexBytes where;

    public ForkAbleStatesTree(Block genesis, T... states) {
        root = new ForkAbleStateSets<>(genesis, states);
        cache = new ChainCache<>();
        cache.add(root);
        where = genesis.getHash();
    }

    public void update(Block b) {
        if (cache.contains(b.getHash().getBytes())) return;
        Optional<ForkAbleStateSets<T>> o = cache.get(b.getHashPrev().getBytes());
        if (!o.isPresent()) throw new RuntimeException(
                "state sets not found at " + b.getHashPrev()
        );
        ForkAbleStateSets<T> parent = o.get();
        ForkAbleStateSets<T> copied = parent.clone();
        try {
            copied.update(b);
        } catch (StateUpdateException e) {
            // this should never happen, for the block b had been validated
            throw new RuntimeException(e.getMessage());
        }
        copied.parent = parent;
        cache.add(copied);
    }

    // provide all already updated state
    public void update(Block b, Collection<? extends T> allStates){
        if (cache.contains(b.getHash().getBytes())) return;
        Optional<ForkAbleStateSets<T>> o = cache.get(b.getHashPrev().getBytes());
        if (!o.isPresent()) throw new RuntimeException(
                "state sets not found at " + b.getHashPrev()
        );
        ForkAbleStateSets<T> parent = o.get();
        ForkAbleStateSets<T> copied = parent.clone();
        copied.update(b, allStates);
        copied.parent = parent;
        cache.add(copied);
    }

    public Optional<T> get(String id, HexBytes where) {
        return cache.get(where.getBytes())
                .flatMap(x -> x.findRecursively(id));
    }

    public synchronized void confirm(Block block) {
        if (!block.getHashPrev().equals(where)) {
            throw new RuntimeException("confirmed block is not child of current root node");
        }
        List<ForkAbleStateSets<T>> children = cache.getChildren(root.getHash().getBytes());
        // clear
        for (ForkAbleStateSets<T> node : children) {
            if (!node.getHash().equals(block.getHash())) {
                cache.remove(cache.getDescendants(node));
                continue;
            }
            node.merge(root);
            this.root = node;
        }
    }
}
