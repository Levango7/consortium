package org.wisdom.common;

public interface PendingTransactionValidator {
    ValidateResult validate(Transaction transaction);
}
