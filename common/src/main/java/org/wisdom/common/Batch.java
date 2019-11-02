package org.wisdom.common;

public interface Batch<K, V>{
    V get(K k);

    void put(K k, V v);

    void flush();
}
