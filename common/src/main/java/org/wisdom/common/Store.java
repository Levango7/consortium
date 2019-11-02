package org.wisdom.common;

public interface Store<K, V> {
    V get(K k);

    void put(K k, V v);
}
