package com.dbtraining.reconx.exception;

/**
 * Signals that a requested trade cannot be found in the current data set.
 */
public class TradeNotFoundException extends ReconException {

    /**
     * Create a missing-trade error with lookup detail.
     *
     * @param message the message describing which trade was not found.
     */
    public TradeNotFoundException(String message) {
        super(message);
    }

    /**
     * Create a missing-trade error with detail plus the underlying lookup cause.
     *
     * @param message the high-level missing-trade description.
     * @param cause the underlying exception raised while locating the trade.
     */
    public TradeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}