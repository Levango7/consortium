package org.wisdom.keystore.crypto;

public class CryptoException extends RuntimeException  {

    /**
     * Creates a new org.wisdom.crypto exception.
     *
     * @param message The exception message.
     */
    public CryptoException(final String message) {
        super(message);
    }

    /**
     * Creates a new org.wisdom.crypto exception.
     *
     * @param cause The exception cause.
     */
    public CryptoException(final Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new org.wisdom.crypto exception.
     *
     * @param message The exception message.
     * @param cause The exception cause.
     */
    public CryptoException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CryptoException() {

    }
}