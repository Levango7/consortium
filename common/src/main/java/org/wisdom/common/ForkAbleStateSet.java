package org.wisdom.common;


import org.wisdom.exception.StateUpdateException;

import java.util.*;
import java.util.stream.Collectors;

public class ForkAbleStateSet<T extends ForkAbleState<T>> implements Cloneable<ForkAbleStateSet<T>>, Chained {
    private T some;

    private HexBytes hashPrev;
    private HexBytes hash;

    @Override
    public HexBytes getHashPrev() {
        return hashPrev;
    }

    @Override
    public HexBytes getHash() {
        return hash;
    }


    private ForkAbleStateSet(){}

    public ForkAbleStateSet(Block genesis, T... states) {
        if (states.length == 0) throw new RuntimeException("at lease one states required");
        this.some = states[0];
        this.cache = new HashMap<>();
        for (T s : states) {
            cache.put(s.getIdentifier(), s);
        }
        this.hash = genesis.getHash();
        this.hashPrev = genesis.getHashPrev();
    }

    private Map<String, T> cache;

    ForkAbleStateSet<T> parent;

    Optional<T> findRecursively(String id) {
        if (cache.containsKey(id)) {
            return Optional.of(cache.get(id).clone());
        }
        if (parent == null) {
            return Optional.empty();
        }
        return parent.findRecursively(id);
    }


    void put(Chained node, Collection<? extends T> allStates){
        for(T s: allStates){
            cache.put(s.getIdentifier(), s);
        }
        hash = node.getHash();
        hashPrev = node.getHashPrev();
    }

    void merge(ForkAbleStateSet<T> sets) {
        for (String k : sets.cache.keySet()) {
            this.cache.put(k, sets.cache.get(k));
        }
    }

    @Override
    public ForkAbleStateSet<T> clone() {
        ForkAbleStateSet<T> res = new ForkAbleStateSet<>();
        res.some = this.some;
        res.hashPrev = this.hashPrev;
        res.hash = this.hash;
        res.cache = new HashMap<>(cache);
        res.parent = parent;
        return res;
    }
}
