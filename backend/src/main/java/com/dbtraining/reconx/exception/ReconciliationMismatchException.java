package com.dbtraining.reconx.exception;

public class ReconciliationMismatchException extends RuntimeException {

    public ReconciliationMismatchException(String message) {
        super(message);
    }

    public ReconciliationMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}