package com.dbtraining.reconx.exception;

/**
 * Signals that two trades being reconciled differ beyond the active tolerance rule.
 */
public class ReconciliationMismatchException extends ReconException {

    /**
     * Create a mismatch error with the comparison detail.
     *
     * @param message the reason the reconciliation comparison failed.
     */
    public ReconciliationMismatchException(String message) {
        super(message);
    }

    /**
     * Create a mismatch error with detail plus the underlying cause.
     *
     * @param message the high-level reconciliation mismatch description.
     * @param cause the underlying exception that triggered the mismatch handling.
     */
    public ReconciliationMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
