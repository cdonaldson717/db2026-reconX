package com.dbtraining.reconx.exception;

/**
 * Signals that an inbound trade payload cannot be parsed or validated.
 */
public class InvalidTradeException extends ReconException {

    public InvalidTradeException(String message) {
        super(message);
    }

    /**
     * Create an invalid-trade error with detail plus the original cause.
     *
     * @param message the high-level validation/parsing failure message.
     * @param cause the underlying exception raised while reading the payload.
     */
    public InvalidTradeException(String message, Throwable cause) {
        super(message, cause);
    }
}