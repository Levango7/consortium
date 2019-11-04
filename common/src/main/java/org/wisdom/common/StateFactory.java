package org.wisdom.common;

import java.util.Optional;

public interface StateFactory<T extends State> {
    Optional<T> get(Block b);
}
