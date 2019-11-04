package org.wisdom.common;

import java.util.Optional;

public interface StateRepository {
    <T extends State<T>> void registerGenesis(T genesisState);

    <T extends State<T>> Optional<T> getState(Block last, Class<T> clazz);
}
