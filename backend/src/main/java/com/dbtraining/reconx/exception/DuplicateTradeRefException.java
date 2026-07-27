package com.dbtraining.reconx.exception;

public class DuplicateTradeRefException extends ReconException {

    public DuplicateTradeRefException(String message) {
        super(message);
    }

    public DuplicateTradeRefException(String message, Throwable cause) {
        super(message, cause);
    }
}