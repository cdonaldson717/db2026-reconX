package com.dbtraining.reconx.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ============================================================================
 * TICKET-ADV026 — ReconciliationRule enum with configurable thresholds
 *
 * WHAT:    Each enum value carries its own price tolerance (%) and quantity
 *          tolerance (absolute units). {@link #matches} returns true if the
 *          internal vs external trade pair is within tolerance.
 * HOW:     Enum-with-state pattern — instance fields + a behaviour method.
 * WHY:     Putting the rule on the enum keeps "what is a match" co-located
 *          with the rule's name, so the reconciliation engine is just:
 *          {@code if (rule.matches(internal, external)) ... matched ...}.
 * OBSERVE: PRICE_TOLERANCE_1PCT.matches(p, p*1.005) is true; *1.02 is false.
 * ============================================================================
 */
public enum ReconciliationRule {

    EXACT(BigDecimal.ZERO, BigDecimal.ZERO),
    PRICE_TOLERANCE_1PCT(new BigDecimal("0.01"), BigDecimal.ZERO),
    PRICE_TOLERANCE_50BPS(new BigDecimal("0.005"), BigDecimal.ZERO),
    QTY_TOLERANCE_5UNITS(BigDecimal.ZERO, new BigDecimal("5")),
    LOOSE(new BigDecimal("0.05"), new BigDecimal("10"));

    private final BigDecimal priceTolerancePct;
    private final BigDecimal qtyToleranceAbs;

    ReconciliationRule(BigDecimal priceTolerancePct, BigDecimal qtyToleranceAbs) {
        this.priceTolerancePct = priceTolerancePct;
        this.qtyToleranceAbs = qtyToleranceAbs;
    }

    /**
     * Return the allowed price drift for this rule as a percentage of the internal price.
     *
     * @return the permitted price difference expressed as a decimal percentage.
     */
    public BigDecimal priceTolerancePct() { return priceTolerancePct; }

    /**
     * Return the allowed quantity drift for this rule in absolute units.
     *
     * @return the permitted quantity difference as an absolute amount.
     */
    public BigDecimal qtyToleranceAbs() { return qtyToleranceAbs; }

    /**
     * Decide whether two prices/quantities are within this rule's tolerance.
     *
     * @param internalPrice the internal-system trade price used as the price-drift denominator.
     * @param internalQty the internal-system trade quantity.
     * @param externalPrice the external/counterparty trade price to compare.
     * @param externalQty the external/counterparty trade quantity to compare.
     * @return {@code true} if both the price drift and quantity drift are within tolerance.
     */
    public boolean matches(BigDecimal internalPrice, BigDecimal internalQty,
                           BigDecimal externalPrice, BigDecimal externalQty) {
        BigDecimal priceDiff = internalPrice.subtract(externalPrice).abs();
        BigDecimal priceDiffPct = internalPrice.signum() == 0
                ? (priceDiff.signum() == 0 ? BigDecimal.ZERO : BigDecimal.ONE)
                : priceDiff.divide(internalPrice.abs(), 8, RoundingMode.HALF_UP);
        BigDecimal qtyDiff = internalQty.subtract(externalQty).abs();

        return priceDiffPct.compareTo(priceTolerancePct) <= 0
                && qtyDiff.compareTo(qtyToleranceAbs) <= 0;
    }
}
