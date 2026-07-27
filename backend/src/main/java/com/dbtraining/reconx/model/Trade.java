package com.dbtraining.reconx.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Internal shared state for the closed trade hierarchy.
 *
 * <p>The public polymorphic contract remains {@link TradeType}; this base is
 * package-private so callers construct and work with the concrete trade types.
 */
abstract sealed class Trade
        permits EquityTrade, FXTrade, BondTrade, DerivativeTrade {

    private final TradeRef tradeRef;
    private final Money notional;
    private final LocalDate tradeDate;

    protected Trade(TradeRef tradeRef, Money notional, LocalDate tradeDate) {
        this.tradeRef = Objects.requireNonNull(tradeRef, "tradeRef");
        this.notional = Objects.requireNonNull(notional, "notional");
        this.tradeDate = Objects.requireNonNull(tradeDate, "tradeDate");
    }

    public final TradeRef tradeRef() {
        return tradeRef;
    }

    public final Money notional() {
        return notional;
    }

    public final LocalDate tradeDate() {
        return tradeDate;
    }
}
