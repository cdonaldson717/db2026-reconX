package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * ============================================================================
 * TICKET-ADV024 — Immutable value object: Money
 *
 * WHAT:    Record bundling a {@link BigDecimal} amount with a {@link Currency}.
 *          Used everywhere a monetary value crosses a boundary (DTO, event,
 *          metric).
 * HOW:     Compact constructor enforces: non-null amount, non-null currency,
 *          non-negative amount. {@link BigDecimal} (not double) prevents
 *          accumulating floating-point error on aggregations.
 * WHY:     Passing raw BigDecimal around loses currency context — a USD 100
 *          can be silently added to a EUR 100. Money makes the mismatch
 *          fail at the type level: {@code plus()} throws if currencies differ.
 * OBSERVE: {@code Money.of("100.00","USD").plus(Money.of("50","EUR"))} throws.
 *          {@code Money.of("100","USD").plus(Money.of("50","USD"))} returns 150 USD.
 * ============================================================================
 */
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative: " + amount);
        }
    }

    /**
     * Create a money value from string inputs.
     *
     * @param amount the decimal amount in plain string form such as {@code 100.25}.
     * @param currencyCode the ISO-4217 currency code for the amount, such as {@code USD}.
     * @return a validated {@code Money} instance carrying both amount and currency.
     * @throws NumberFormatException if {@code amount} is not a valid decimal literal.
     * @throws IllegalArgumentException if {@code currencyCode} is not a valid ISO-4217 code
     *                                  or if the parsed amount is negative.
     */
    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    /**
     * Create a money value from a pre-parsed amount and a currency code.
     *
     * @param amount the monetary amount to wrap.
     * @param currencyCode the ISO-4217 currency code for the amount.
     * @return a validated {@code Money} instance.
     * @throws NullPointerException if {@code amount} is {@code null}.
     * @throws IllegalArgumentException if {@code currencyCode} is not a valid ISO-4217 code
     *                                  or if {@code amount} is negative.
     */
    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    /**
     * Add another monetary value of the same currency.
     *
     * @param other the amount to add to this amount.
     * @return a new {@code Money} whose amount is {@code this.amount + other.amount}.
     * @throws NullPointerException if {@code other} is {@code null}.
     * @throws IllegalArgumentException if {@code other} uses a different currency.
     */
    public Money plus(Money other) {
        Objects.requireNonNull(other, "other");

        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot add %s to %s — currency mismatch"
                            .formatted(other.currency, this.currency));
        }

        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Scale this monetary value by a numeric multiplier.
     *
     * @param multiplier the scalar applied to the current amount.
     * @return a new {@code Money} with the scaled amount and the same currency.
     * @throws NullPointerException if {@code multiplier} is {@code null}.
     * @throws IllegalArgumentException if the multiplication produces a negative amount.
     */
    public Money times(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier");
        return new Money(this.amount.multiply(multiplier), this.currency);
    }
}