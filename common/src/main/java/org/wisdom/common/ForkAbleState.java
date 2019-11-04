package org.wisdom.common;

import java.util.Set;

public interface ForkAbleState<T> extends State<T> {
    String getIdentifier();

    Set<String> getIdentifiersOf(Transaction transaction);

    T createEmpty(String id);
}
