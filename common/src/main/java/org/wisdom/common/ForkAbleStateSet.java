package org.wisdom.common;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ForkAbleStateSet<T extends ForkAbleState<T>> extends ChainedWrapper<Map<String, T>> {
    public ForkAbleStateSet(HexBytes hashPrev, HexBytes hash, Map<String, T> data) {
        super(hashPrev, hash, data);
    }

    public ForkAbleStateSet(HexBytes hashPrev, HexBytes hash, Collection<? extends T> states) {
        this.data = states.stream().collect(Collectors.toMap(ForkAbleState::getIdentifier, Function.identity()));
        this.hashPrev = hashPrev;
        this.hash = hash;
    }

    void put(Chained node, Collection<? extends T> allStates) {
        for (T s : allStates) {
            data.put(s.getIdentifier(), s);
        }
        hash = node.getHash();
        hashPrev = node.getHashPrev();
    }

    void merge(ForkAbleStateSet<T> set) {
        for (String k : set.data.keySet()) {
            data.put(k, set.data.get(k));
        }
    }
}
