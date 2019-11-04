package org.wisdom.common;

public interface PendingTransactionValidator {
    ValidateResult validateTransaction(Transaction transaction);
}
