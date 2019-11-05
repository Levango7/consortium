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

    public ForkAbleStatesTree(Block genesis, T... states) {
        if (states.length == 0) throw new RuntimeException("at lease one states required");
        some = states[0];
        root = new ForkAbleStateSet<>(genesis, states);
        cache = new ChainCache<>();
        cache.put(root);
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
                .map(id -> this.get(id, parent.getHash().getBytes()).orElse(some.createEmpty(id)))
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
        cache.put(copied);
    }

    public Optional<T> get(String id, byte[] where) {
        if(root.getHash().equals(new HexBytes(where))){
            return Optional.ofNullable(root.cache.get(id)).map(Cloneable::clone);
        }
        Optional<ForkAbleStateSet<T>> set = cache.get(where);
        if(!set.isPresent()){
            return Optional.empty();
        }
        if (set.get().cache.containsKey(id))
            return Optional.of(set.get().cache.get(id).clone());
        return get(id, set.get().getHashPrev().getBytes());
    }

    public T getLastConfirmed(String id) {
        if (root.cache.containsKey(id)) return root.cache.get(id);
        return some.createEmpty(id);
    }

    public void confirm(byte[] hash) {
        HexBytes h = new HexBytes(hash);
        if(root.getHash().equals(h)) return;
        List<ForkAbleStateSet<T>> children = cache.getChildren(root.getHash().getBytes());
        Optional<ForkAbleStateSet<T>> o = children.stream().filter(x -> x.getHash().equals(h)).findFirst();
        if (!o.isPresent()) {
            throw new RuntimeException("the state to confirm not found or confirmed block is not child of current node");
        }
        ForkAbleStateSet<T> set = o.get();
        children.stream().filter(x -> !x.getHash().equals(h))
                .forEach(n -> cache.removeDescendants(n.getHash().getBytes()));
        set.merge(root);
        this.root = set;
    }
}
