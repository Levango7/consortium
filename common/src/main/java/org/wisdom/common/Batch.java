package org.wisdom.common;

import java.util.Optional;

public interface Batch<K, V>{
    Optional<V> get(K k);

    void put(K k, V v);

    void remove(K k, V v);

    void flush();
}
