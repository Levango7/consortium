package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.lang.reflect.Type;
import java.util.*;

/**
 * State tree for account related object storage
 */
public class ForkAbleStatesTree<T extends ForkAbleState<T>> {
    private ForkAbleStateSet<T> root;
    private ChainCache<ForkAbleStateSet<T>> cache;
    private T some;
    // where of the root
    private HexBytes where;

    public ForkAbleStatesTree(Block genesis, T... states) {
        if (states.length == 0) throw new RuntimeException("at lease one states required");
        some = states[0];
        root = new ForkAbleStateSet<>(genesis, states);
        cache = new ChainCache<>();
        cache.add(root);
        where = genesis.getHash();
    }

    public void update(Block b) {
        if (cache.contains(b.getHash().getBytes())) return;
        Optional<ForkAbleStateSet<T>> o = cache.get(b.getHashPrev().getBytes());
        if (!o.isPresent()) throw new RuntimeException(
                "state sets not found at " + b.getHashPrev()
        );
        ForkAbleStateSet<T> parent = o.get();
        ForkAbleStateSet<T> copied = parent.clone();
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
        Optional<ForkAbleStateSet<T>> o = cache.get(b.getHashPrev().getBytes());
        if (!o.isPresent()) throw new RuntimeException(
                "state sets not found at " + b.getHashPrev()
        );
        ForkAbleStateSet<T> parent = o.get();
        ForkAbleStateSet<T> copied = parent.clone();
        copied.update(b, allStates);
        copied.parent = parent;
        cache.add(copied);
    }

    public Optional<T> get(String id, byte[] where) {
        return cache.get(where)
                .flatMap(x -> x.findRecursively(id));
    }

    public T getLastConfirmed(String id){
        return cache.get(where.getBytes()).flatMap(x -> x.findRecursively(id)).orElse(some.createEmpty(id));
    }

    public synchronized void confirm(byte[] hash) {
        HexBytes h = new HexBytes(hash);
        List<ForkAbleStateSet<T>> children = cache.getChildren(where.getBytes());
        Optional<ForkAbleStateSet<T>> o = children.stream().filter(x -> x.getHash().equals(h)).findFirst();
        if(!o.isPresent()){
            throw new RuntimeException("the state to confirm not found or confirmed block is not child of current node");
        }
        ForkAbleStateSet<T> set = o.get();
        children.stream().filter(x -> !x.getHash().equals(h))
                .forEach(n -> cache.remove(n.getHash().getBytes()));
        set.merge(root);
        this.root = set;
        where = h;
    }
}
