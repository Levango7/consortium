package org.wisdom.common;

import java.util.Optional;

public interface StateFactory {
    <T extends State<T>> void registerGenesis(T genesisState);

    <T extends State<T>> Optional<T> getState(Chained node, Class<T> clazz);
}
