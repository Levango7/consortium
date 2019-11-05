package org.wisdom.common;


import org.wisdom.exception.StateUpdateException;

import java.util.*;
import java.util.stream.Collectors;

public class ForkAbleStateSets<T extends ForkAbleState<T>> implements Cloneable<ForkAbleStateSets<T>>, Chained {
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


    private ForkAbleStateSets(){}

    public ForkAbleStateSets(Block genesis, T... states) {
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

    ForkAbleStateSets<T> parent;

    Optional<T> findRecursively(String id) {
        if (cache.containsKey(id)) {
            return Optional.of(cache.get(id).clone());
        }
        if (parent == null) {
            return Optional.empty();
        }
        return parent.findRecursively(id);
    }

    void update(Block b) throws StateUpdateException {
        Set<String> all = new HashSet<>();
        b.getBody().stream().map(some::getIdentifiersOf).forEach(all::addAll);
        Map<String, T> states = all.stream()
                .map(id -> findRecursively(id).orElse(some.createEmpty(id)))
                .collect(Collectors.toMap(ForkAbleState::getIdentifier, (s) -> s));

        for (Transaction tx : b.getBody()) {
            for (T t : states.values()) {
                t.update(b, tx);
            }
        }
        states.forEach((k, v) -> cache.put(k, v));
        hash = b.getHash();
        hashPrev = b.getHashPrev();
    }

    void update(Block b, Collection<? extends T> allStates){
        Set<String> all = new HashSet<>();
        b.getBody().stream().map(some::getIdentifiersOf).forEach(all::addAll);
        for(T s: allStates){
            if (!all.contains(s.getIdentifier())) throw new RuntimeException(
                    "not enough related state provided, missing " + s.getIdentifier());
            cache.put(s.getIdentifier(), s);
        }
        hash = b.getHash();
        hashPrev = b.getHashPrev();
    }

    void merge(ForkAbleStateSets<T> sets) {
        for (String k : sets.cache.keySet()) {
            this.cache.put(k, sets.cache.get(k));
        }
    }

    @Override
    public ForkAbleStateSets<T> clone() {
        ForkAbleStateSets<T> res = new ForkAbleStateSets<>();
        res.some = this.some;
        res.hashPrev = this.hashPrev;
        res.hash = this.hash;
        res.cache = new HashMap<>(cache);
        res.parent = parent;
        return res;
    }
}
