package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BondTradeTest {

    @Test
    void builder_buildsWithFaceValueAsNotional() {
        BondTrade trade = validBuilder().build();

        assertThat(trade.isin()).isEqualTo("DE0001102341");
        assertThat(trade.faceValue()).isEqualByComparingTo("100000");
        assertThat(trade.couponRate()).isEqualByComparingTo("0.025");
        assertThat(trade.notional().amount()).isEqualByComparingTo("100000");
        assertThat(trade.notional().currency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.BOND);
    }

    @Test
    void builder_rejectsMaturityOnOrBeforeTradeDate() {
        LocalDate tradeDate = LocalDate.of(2026, 6, 3);

        assertThatThrownBy(() -> validBuilder().maturityDate(tradeDate).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("maturityDate must be after tradeDate");
        assertThatThrownBy(() -> validBuilder().maturityDate(tradeDate.minusDays(1)).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("maturityDate must be after tradeDate");
    }

    @Test
    void builder_rejectsInvalidIsinLength() {
        assertThatThrownBy(() -> validBuilder().isin("TOO-SHORT").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("isin must be exactly 12 characters");
    }

    @Test
    void builder_rejectsNonPositiveFaceValue() {
        assertThatThrownBy(() -> validBuilder().faceValue(BigDecimal.ZERO).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("faceValue must be > 0");
    }

    @Test
    void builder_acceptsZeroCouponButRejectsNegativeCoupon() {
        assertThat(validBuilder().couponRate(BigDecimal.ZERO).build().couponRate())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThatThrownBy(() -> validBuilder().couponRate(new BigDecimal("-0.01")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("couponRate must be >= 0");
    }

    @Test
    void builder_missingRequiredField_throwsNamedException() {
        assertThatThrownBy(() -> BondTrade.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("tradeRef");
    }

    private BondTrade.Builder validBuilder() {
        return BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260603-0001"))
                .isin("DE0001102341")
                .faceValue(new BigDecimal("100000"))
                .couponRate(new BigDecimal("0.025"))
                .maturityDate(LocalDate.of(2036, 6, 3))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L);
    }
}
