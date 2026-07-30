package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV022 - DerivativeTrade with Builder pattern
 *
 * WHAT:    Option/derivative trade - underlying, strike, expiry, optionType.
 * HOW:     Same builder pattern. notional() = strike * quantity in the
 *          trade's currency (simplified - real derivatives use delta-adjusted).
 *          Expiry is validated against tradeDate, not the current date, so an
 *          expired derivative remains a valid historical trade record.
 * ============================================================================
 */
public final class DerivativeTrade extends Trade implements TradeType {

    public enum OptionType {
        CALL,
        PUT
    }

    private final String underlying;
    private final BigDecimal strike;
    private final BigDecimal quantity;
    private final LocalDate expiry;
    private final OptionType optionType;
    private final Currency currency;
    private final Side side;
    private final long counterpartyId;

    private DerivativeTrade(Builder b) {
        super(b.tradeRef, new Money(b.strike.multiply(b.quantity), b.currency), b.tradeDate);
        this.underlying     = b.underlying;
        this.strike         = b.strike;
        this.quantity       = b.quantity;
        this.expiry         = b.expiry;
        this.optionType     = b.optionType;
        this.currency       = b.currency;
        this.side           = b.side;
        this.counterpartyId = b.counterpartyId;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AssetClass assetClass() {
        return AssetClass.DERIVATIVE;
    }

    public String underlying() {
        return underlying;
    }

    public BigDecimal strike() {
        return strike;
    }

    public BigDecimal quantity() {
        return quantity;
    }

    public LocalDate expiry() {
        return expiry;
    }

    public OptionType optionType() {
        return optionType;
    }

    public Currency currency() {
        return currency;
    }

    public Side side() {
        return side;
    }

    public long counterpartyId() {
        return counterpartyId;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof DerivativeTrade other
                && tradeRef().equals(other.tradeRef());
    }

    @Override
    public int hashCode() {
        return tradeRef().hashCode();
    }

    @Override
    public String toString() {
        return "DerivativeTrade[ref=%s, %s %s on %s, strike=%s %s, qty=%s, expiry=%s, side=%s]"
                .formatted(
                        tradeRef(),
                        optionType,
                        underlying,
                        tradeDate(),
                        strike.toPlainString(),
                        currency.getCurrencyCode(),
                        quantity.toPlainString(),
                        expiry,
                        side);
    }

    public static final class Builder {

        private TradeRef tradeRef;
        private String underlying;
        private BigDecimal strike;
        private BigDecimal quantity;
        private LocalDate expiry;
        private LocalDate tradeDate;
        private OptionType optionType;
        private Currency currency;
        private Side side;
        private long counterpartyId;

        public Builder tradeRef(TradeRef value) {
            this.tradeRef = value;
            return this;
        }

        public Builder underlying(String value) {
            this.underlying = value;
            return this;
        }

        public Builder strike(BigDecimal value) {
            this.strike = value;
            return this;
        }

        public Builder quantity(BigDecimal value) {
            this.quantity = value;
            return this;
        }

        public Builder expiry(LocalDate value) {
            this.expiry = value;
            return this;
        }

        public Builder optionType(OptionType value) {
            this.optionType = value;
            return this;
        }

        public Builder currency(String code) {
            this.currency = Currency.getInstance(code);
            return this;
        }

        public Builder side(Side value) {
            this.side = value;
            return this;
        }

        public Builder tradeDate(LocalDate value) {
            this.tradeDate = value;
            return this;
        }

        public Builder counterpartyId(long value) {
            this.counterpartyId = value;
            return this;
        }

        public DerivativeTrade build() {
            Objects.requireNonNull(tradeRef, "tradeRef");
            Objects.requireNonNull(underlying, "underlying");
            Objects.requireNonNull(strike, "strike");
            Objects.requireNonNull(quantity, "quantity");
            Objects.requireNonNull(expiry, "expiry");
            Objects.requireNonNull(optionType, "optionType");
            Objects.requireNonNull(currency, "currency");
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(tradeDate, "tradeDate");

            return new DerivativeTrade(this);
        }
    }
}
