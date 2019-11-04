package org.wisdom.common;


import org.wisdom.exception.StateUpdateException;

import java.util.*;
import java.util.stream.Collectors;

public class ForkAbleStateSets<T extends ForkAbleState<T>> implements Cloneable<ForkAbleStateSets<T>>, Chained {
    private T empty;

    private HexBytes hashPrev;
    private HexBytes hash;
    private long height;

    @Override
    public HexBytes getHashPrev() {
        return hashPrev;
    }

    @Override
    public HexBytes getHash() {
        return hash;
    }

    @Override
    public long getHeight() {
        return height;
    }

    private ForkAbleStateSets(){}

    public ForkAbleStateSets(Block genesis, T... states) {
        if (states.length == 0) throw new RuntimeException("at lease one states required");
        this.empty = states[0];
        this.cache = new HashMap<>();
        for (T s : states) {
            cache.put(s.getIdentifier(), s);
        }
        this.height = genesis.getHeight();
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

    void updateBlock(Block b) throws StateUpdateException {
        Set<String> all = new HashSet<>();
        b.getBody().stream().map(empty::getIdentifiersOf).forEach(all::addAll);
        Map<String, T> states = all.stream()
                .map(id -> findRecursively(id).orElse(empty.createEmpty(id)))
                .collect(Collectors.toMap(ForkAbleState::getIdentifier, (s) -> s));

        for (Transaction tx : b.getBody()) {
            for (T t : states.values()) {
                t.update(b, tx);
            }
        }
        states.forEach((k, v) -> cache.put(k, v));
        hash = b.getHash();
        hashPrev = b.getHashPrev();
        height = b.getHeight();
    }

    void merge(ForkAbleStateSets<T> sets) {
        for (String k : sets.cache.keySet()) {
            this.cache.put(k, sets.cache.get(k));
        }
    }

    @Override
    public ForkAbleStateSets<T> clone() {
        ForkAbleStateSets<T> res = new ForkAbleStateSets<>();
        res.empty = this.empty;
        res.hashPrev = this.hashPrev;
        res.hash = this.hash;
        res.cache = new HashMap<>(cache);
        res.parent = parent;
        return res;
    }
}
