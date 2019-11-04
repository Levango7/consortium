package org.wisdom.common;

public interface ForkAbleState<T> extends State<T> {
    String getIdentifier();

    String getIdentifierOf(Transaction transaction);

    T createEmpty(String id);
}
