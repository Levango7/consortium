package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.util.*;
import java.util.stream.Collectors;

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
        cache.put(root);
        where = genesis.getHash();
    }

    public void update(Block b) {
        if (cache.contains(b.getHash().getBytes())) return;
        Optional<ForkAbleStateSet<T>> o = cache.get(b.getHashPrev().getBytes());
        if (!o.isPresent()) throw new RuntimeException(
                "state sets not found at " + b.getHashPrev()
        );
        ForkAbleStateSet<T> parent = o.get();
        Set<String> all = new HashSet<>();
        b.getBody().stream().map(some::getIdentifiersOf).forEach(all::addAll);
        Map<String, T> states = all.stream()
                .map(id -> parent.findRecursively(id).orElse(some.createEmpty(id)))
                .collect(Collectors.toMap(ForkAbleState::getIdentifier, (s) -> s));

        for (Transaction tx : b.getBody()) {
            for (T t : states.values()) {
                try {
                    t.update(b, tx);
                } catch (StateUpdateException e) {
                    e.printStackTrace();
                }
            }
        }
        put(b, states.values());
    }

    // provide all already updated state
    public void put(Chained node, Collection<? extends T> allStates) {
        if (cache.contains(node.getHash().getBytes())) return;
        Optional<ForkAbleStateSet<T>> o = cache.get(node.getHashPrev().getBytes());
        if (!o.isPresent()) throw new RuntimeException(
                "state sets not found at " + node.getHashPrev()
        );
        ForkAbleStateSet<T> parent = o.get();
        ForkAbleStateSet<T> copied = parent.clone();
        copied.put(node, allStates);
        copied.parent = parent;
        cache.put(copied);
    }

    public Optional<T> get(String id, byte[] where) {
        return cache.get(where)
                .flatMap(x -> x.findRecursively(id));
    }

    public T getLastConfirmed(String id) {
        return cache.get(where.getBytes()).flatMap(x -> x.findRecursively(id)).orElse(some.createEmpty(id));
    }

    public synchronized void confirm(byte[] hash) {
        HexBytes h = new HexBytes(hash);
        List<ForkAbleStateSet<T>> children = cache.getChildren(where.getBytes());
        Optional<ForkAbleStateSet<T>> o = children.stream().filter(x -> x.getHash().equals(h)).findFirst();
        if (!o.isPresent()) {
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
