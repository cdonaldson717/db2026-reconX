package com.dbtraining.reconx.model;

/**
 * ============================================================================
 * TICKET-ADV019 / ADV020 / ADV021 / ADV022 — Side enum
 *
 * WHAT:    Closed set describing the commercial direction of a trade.
 * HOW:     Enum rather than a string so invalid directions fail at compile time.
 * WHY:     Trade builders and reconciliation logic should not rely on free-form
 *          text for one of the most important commercial fields on the model.
 * ============================================================================
 */
public enum Side {
    BUY, SELL
}
