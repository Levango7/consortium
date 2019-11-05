package org.wisdom.common;

import java.util.*;

public class ForkAbleStateSet<T extends ForkAbleState<T>> implements Chained {
    private HexBytes hashPrev;
    private HexBytes hash;

    public void setHashPrev(HexBytes hashPrev) {
        this.hashPrev = hashPrev;
    }

    public void setHash(HexBytes hash) {
        this.hash = hash;
    }

    @Override
    public HexBytes getHashPrev() {
        return hashPrev;
    }

    @Override
    public HexBytes getHash() {
        return hash;
    }


    ForkAbleStateSet(HexBytes hashPrev, HexBytes hash, Collection<? extends T> states) {
        this.cache = new HashMap<>();
        for (T s : states) {
            cache.put(s.getIdentifier(), s);
        }
        this.hash = hash;
        this.hashPrev = hashPrev;
    }

    Map<String, T> cache;

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
}
