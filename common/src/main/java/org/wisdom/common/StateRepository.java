package org.wisdom.common;

import org.wisdom.exception.StateUpdateException;

import java.util.Optional;

public interface StateRepository {
    <T extends State<T>> void register(Block genesis, T genesisState) throws StateUpdateException;

    <T extends ForkAbleState<T>> void register(Block genesis, T... forkAbleStates);

    <T extends State<T>> Optional<T> getState(Block last, Class<T> clazz);

    <T extends ForkAbleState<T>> Optional<T> getForkAbleState(Block last, String id, Class<T> clazz);

    void update(Block b);

    void confirm(Block b);
}
