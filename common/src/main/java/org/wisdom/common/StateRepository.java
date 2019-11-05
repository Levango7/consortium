package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.util.Collection;
import java.util.Optional;

public interface StateRepository {
    <T extends State<T>> void register(Block genesis, T genesisState) throws StateUpdateException;

    <T extends ForkAbleState<T>> void register(Block genesis, T... forkAbleStates);

    <T extends State<T>> Optional<T> get(byte[] hash, Class<T> clazz);

    <T extends ForkAbleState<T>> Optional<T> get(byte[] hash, String id, Class<T> clazz);

    void update(Block b);

    void update(Block b, State state);

    void update(Block b, Collection<ForkAbleState> forkAbleStates, Class<? extends ForkAbleState> clazz);

    void confirm(byte[] hash);
}
