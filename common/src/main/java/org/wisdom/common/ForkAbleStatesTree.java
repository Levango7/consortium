package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.util.*;

public class ForkAbleStatesTree<T extends ForkAbleState<T>> {
    private ForkAbleStateSets<T> root;
    private ChainCache<ForkAbleStateSets<T>> cache;

    // where of the root
    private HexBytes where;

    public ForkAbleStatesTree(T empty, Block genesis) throws StateUpdateException {
        root = new ForkAbleStateSets<>(empty);
        root.updateBlock(genesis);
        cache = new ChainCache<>();
        cache.add(root);
        where = genesis.getHash();
    }

    public void update(Block b) throws StateUpdateException {
        if (cache.contains(b.getHash().getBytes())) return;
        Optional<ForkAbleStateSets<T>> o = cache.get(b.getHashPrev().getBytes());
        if (!o.isPresent()) throw new StateUpdateException(
                "state sets not found at " + b.getHashPrev()
        );
        ForkAbleStateSets<T> parent = o.get();
        ForkAbleStateSets<T> copied = parent.clone();
        copied.updateBlock(b);
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
        for(ForkAbleStateSets<T> node: children){
            if(!node.getHash().equals(block.getHash())){
                cache.remove(cache.getDescendants(node));
                continue;
            }
            node.merge(root);
            this.root = node;
        }
    }
}
