package org.wisdom.common;

import java.util.Optional;

public interface Store<K, V> {
    Optional<V> get(K k);

    void put(K k, V v);
}
