package com.dbtraining.reconx.exception;

public class ReconciliationMismatchException extends ReconException {

    public ReconciliationMismatchException(String message) {
        super(message);
    }

    public ReconciliationMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}