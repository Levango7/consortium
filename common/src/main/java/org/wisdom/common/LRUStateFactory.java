package org.wisdom.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;

public class LRUStateFactory<T extends State<T>> implements StateFactory<T>{
    private static final long MAXIMUM_TRAVERSE_DEPTH = 2048;

    private Cache<String, T> cache;

    private BlockRepository repository;

    private T genesisState;

    public LRUStateFactory(long size, T genesis, BlockRepository repository) {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(size).build();
        cache.put(genesis.where().toString(), genesis);
        this.repository = repository;
    }

    private Optional<T> getFromCache(Block block, long depth) {
        if (depth > MAXIMUM_TRAVERSE_DEPTH) return Optional.empty();
        if (block.getHeight() == 0) {
            return Optional.of(genesisState);
        }
        String key = block.getHash().toString();
        if (cache.asMap().containsKey(key)) {
            return Optional.of(cache.asMap().get(key));
        }
        Optional<Block> o = repository.getBlock(block.getHashPrev().getBytes());
        if (!o.isPresent()) return Optional.empty();

        Optional<T> parentStateCopied =
                repository
                        .getBlock(block.getHashPrev().getBytes())
                        .flatMap(b -> getFromCache(b, depth + 1).map(State::clone));
        if (!parentStateCopied.isPresent()) return parentStateCopied;
        T s = parentStateCopied.get();
        for (Transaction t : block.getBody()) {
            try {
                s.update(block, t);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        cache.asMap().put(s.where().toString(), s);
        return Optional.of(s);
    }

    public Optional<T> get(Block block) {
        if (!repository.hasBlock(block.getHash().getBytes())) {
            return Optional.empty();
        }
        if (block.getHeight() == 0) {
            return Optional.of(genesisState);
        }
        return getFromCache(block, 0);
    }
}
