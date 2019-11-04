package org.wisdom.common;

import java.util.Optional;

public interface StateFactory<T extends State> {
    Optional<T> get(Block b);
    void update(Block b);
    // provide already updated state
    void update(Block b, T state);
    void confirm(Block b);
}
