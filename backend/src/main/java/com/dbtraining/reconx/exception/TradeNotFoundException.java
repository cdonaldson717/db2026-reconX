package com.dbtraining.reconx.exception;

public class TradeNotFoundException extends ReconException {

    public TradeNotFoundException(String message) {
        super(message);
    }

    public TradeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}