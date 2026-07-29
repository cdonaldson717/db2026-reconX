package com.dbtraining.reconx.exception;

/**
 * Signals that two trades differ beyond the active reconciliation tolerance.
 */
public class ReconciliationMismatchException extends ReconException {

    public ReconciliationMismatchException(String message) {
        super(message);
    }

    public ReconciliationMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
