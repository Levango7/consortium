package org.wisdom.common;

import java.util.Collection;
import java.util.Optional;

public class PersistentStateTree<T extends ForkAbleState<T> & Serializable & Deserializable> implements StateTree<T>{
    private BatchAbleStore<byte[], byte[]> store;
    
    @Override
    public void update(Block b) {

    }

    @Override
    public void put(Chained node, Collection<? extends T> allStates) {

    }

    @Override
    public Optional<T> get(String id, byte[] where) {
        return Optional.empty();
    }

    @Override
    public T getLastConfirmed(String id) {
        return null;
    }

    @Override
    public void confirm(byte[] hash) {

    }
}
