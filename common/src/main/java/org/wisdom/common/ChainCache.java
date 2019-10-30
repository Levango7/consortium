package org.wisdom.common;

import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author sal 1564319846@qq.com
 */
public class ChainCache<T extends Chained> implements Cloneable<ChainCache<T>> {
    private Map<String, T> nodes;
    private Map<String, Set<String>> childrenHashes;
    private Map<Long, Set<String>> heightIndex;
    private int sizeLimit;

    // lru
    public ChainCache(int sizeLimit) {
        this();
        this.sizeLimit = sizeLimit;
    }

    public ChainCache() {
        this.nodes = new HashMap<>();
        this.childrenHashes = new HashMap<>();
        this.heightIndex = new TreeMap<>();
    }

    public ChainCache(T node) {
        this();
        add(node);
    }

    public ChainCache(Collection<? extends T> nodes) {
        this();
        add(nodes);
    }

    public Optional<T> get(byte[] hash) {
        return Optional.ofNullable(nodes.get(HexBytes.encode(hash)));
    }

    private List<T> getNodes(Collection<String> hashes) {
        return new HashSet<>(hashes).stream()
                .map(k -> nodes.get(k))
                .filter(Objects::nonNull)
                .sorted((x, y) -> (int) (x.getHeight() - y.getHeight()))
                .collect(Collectors.toList());
    }

    public ChainCache<T> clone() {
        ChainCache<T> copied = new ChainCache<>();
        copied.nodes = new HashMap<>(nodes);
        copied.childrenHashes = new HashMap<>(childrenHashes);
        copied.heightIndex = new TreeMap<>(heightIndex);
        return copied;
    }

    public List<T> getDescendantBlocks(T node) {
        LinkedList<Set<String>> descendantBlocksHash = new LinkedList<>();
        String key = node.getHash().toString();
        descendantBlocksHash.add(Collections.singleton(key));
        while (true) {
            Set<String> tmp = new HashSet<>();
            for (String k : descendantBlocksHash.getLast()) {
                if (!childrenHashes.containsKey(k)) {
                    continue;
                }
                tmp.addAll(childrenHashes.get(k));
            }
            if (tmp.size() == 0) {
                break;
            }
            descendantBlocksHash.add(tmp);
        }
        Set<String> all = descendantBlocksHash.stream().reduce(new HashSet<>(), (x, y) -> {
            Set<String> prev = new HashSet<>(x);
            prev.addAll(y);
            return prev;
        });
        return getNodes(all);
    }

    public List<List<T>> getAllForks() {
        List<List<T>> res = getLeavesHash().stream()
                .map(k -> nodes.get(k))
                .map(this::getAncestors)
                .sorted(Comparator.comparingLong(List::size))
                .collect(Collectors.toList());
        Collections.reverse(res);
        return res;
    }


    private void remove(T node) {
        String hash = node.getHashPrev().toString();
        String prevHash = node.getHashPrev().toString();
        nodes.remove(hash);
        if (childrenHashes.containsKey(prevHash)) {
            childrenHashes.get(prevHash).remove(hash);
        }
        if (childrenHashes.containsKey(prevHash) && childrenHashes.get(prevHash).size() == 0) {
            childrenHashes.remove(prevHash);
        }
        if (heightIndex.containsKey(node.getHeight())) {
            heightIndex.get(node.getHeight()).remove(hash);
        }
        if (heightIndex.containsKey(node.getHeight()) && heightIndex.get(node.getHeight()).size() == 0) {
            heightIndex.remove(node.getHeight());
        }
    }

    public final void remove(Collection<? extends T> nodes) {
        for (T node : nodes) {
            remove(node);
        }
    }

    // leaves not has children
    private Set<String> getLeavesHash() {
        Set<String> res = new HashSet<>();
        for (String key : nodes.keySet()) {
            if (!childrenHashes.containsKey(key) || childrenHashes.get(key).isEmpty()) {
                res.add(key);
            }
        }
        return res;
    }


    public List<T> getLeaves() {
        return getNodes(getLeavesHash());
    }

    public List<T> getInitials() {
        return getNodes(getInitialsHash());
    }

    // initials not has parent
    private Set<String> getInitialsHash() {
        Set<String> res = new HashSet<>();
        for (String key : nodes.keySet()) {
            T node = nodes.get(key);
            if (nodes.get(key).getHeight() == 0) {
                res.add(key);
                continue;
            }
            if (!nodes.containsKey(node.getHashPrev().toString())) {
                res.add(key);
            }
        }
        return res;
    }

    public void add(@NonNull T node) {
        while (sizeLimit != 0 && this.nodes.size() > sizeLimit) {
            this.nodes.values()
                    .stream()
                    .min(Comparator.comparingLong(Chained::getHeight))
                    .ifPresent(this::remove);
        }
        String key = node.getHash().toString();
        if (this.nodes.containsKey(key)) {
            return;
        }
        nodes.put(key, node);
        String prevHash = node.getHashPrev().toString();
        if (!childrenHashes.containsKey(prevHash)) {
            childrenHashes.put(prevHash, new HashSet<>());
        }
        childrenHashes.get(prevHash).add(key);
        if (!heightIndex.containsKey(node.getHeight())) {
            heightIndex.put(node.getHeight(), new HashSet<>());
        }
        heightIndex.get(node.getHeight()).add(key);
    }

    public void add(@NonNull Collection<? extends T> nodes) {
        for (T b : nodes) {
            add(b);
        }
    }

    public List<T> getAll() {
        return getNodes(nodes.keySet());
    }

    public List<T> popLongestChain() {
        List<List<T>> res = getAllForks();
        if (res.size() == 0) {
            return new ArrayList<>();
        }
        List<T> longest = res.get(res.size() - 1);
        remove(longest);
        return longest;
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean hasBlock(byte[] hash) {
        return nodes.containsKey(HexBytes.encode(hash));
    }

    public Optional<T> getAncestor(T node, long height) {
        String parentKey = node.getHashPrev().toString();
        while (node != null && node.getHeight() > height) {
            node = nodes.get(parentKey);
        }
        if (node != null && node.getHeight() == height) {
            return Optional.of(node);
        }
        return Optional.empty();
    }

    public List<T> getAncestors(@NonNull T node) {
        List<T> res = new ArrayList<>();
        while (node != null) {
            res.add(node);
            node = nodes.get(node.getHashPrev().toString());
        }
        Collections.reverse(res);
        return res;
    }
}