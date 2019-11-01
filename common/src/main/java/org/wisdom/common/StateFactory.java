package org.wisdom.common;

import java.util.Optional;

public interface StateFactory {
    <T extends State<T>> void registerGenesisState(T genesisState);

    <T extends State<T>> Optional<T> getState(Block block, Class<T> clazz);
}
