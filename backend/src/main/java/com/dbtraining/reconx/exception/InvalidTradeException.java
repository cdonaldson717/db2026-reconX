package com.dbtraining.reconx.exception;

/**
 * Signals that an inbound trade payload cannot be parsed or validated into a domain trade.
 */
public class InvalidTradeException extends ReconException {

    /**
     * Create an invalid-trade error with the validation or parsing detail.
     *
     * @param message the specific reason the trade is invalid.
     */
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

    public InvalidTradeException(String message, Throwable cause) {
        super(message, cause);
    }
}