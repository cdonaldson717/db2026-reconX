package com.dbtraining.reconx.exception;

/**
 * Signals that a trade reference expected to be unique already exists.
 */
public class DuplicateTradeRefException extends ReconException {

    /**
     * Create a duplicate-reference error with the conflicting trade reference detail.
     *
     * @param message the message describing the duplicate business key.
     */
    public DuplicateTradeRefException(String message) {
        super(message);
    }

    /**
     * Create a duplicate-reference error with detail plus the underlying cause.
     *
     * @param message the high-level duplicate-reference description.
     * @param cause the underlying exception that exposed the duplicate.
     */
    public DuplicateTradeRefException(String message, Throwable cause) {
        super(message, cause);
    }
}
