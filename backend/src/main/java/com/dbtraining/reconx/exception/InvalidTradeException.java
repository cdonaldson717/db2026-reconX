package com.dbtraining.reconx.exception;

/**
 * Signals that an inbound trade payload cannot be parsed or validated.
 */
public class InvalidTradeException extends ReconException {

    public InvalidTradeException(String message) {
        super(message);
    }

    public InvalidTradeException(String message, Throwable cause) {
        super(message, cause);
    }
}