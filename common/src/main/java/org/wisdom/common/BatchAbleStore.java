package org.wisdom.common;

public interface BatchAbleStore<K, V> extends Store<K, V>{
    Batch<K, V> batch();
}
