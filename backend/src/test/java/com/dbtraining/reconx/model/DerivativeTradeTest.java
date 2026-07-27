package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DerivativeTradeTest {

    @Test
    void builder_buildsAndCalculatesNotional() {
        DerivativeTrade trade = validBuilder().build();

        assertThat(trade.underlying()).isEqualTo("SAP.DE");
        assertThat(trade.optionType()).isEqualTo(DerivativeTrade.OptionType.CALL);
        assertThat(trade.notional().amount()).isEqualByComparingTo("5000");
        assertThat(trade.notional().currency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.DERIVATIVE);
    }

    @Test
    void builder_acceptsExpiredHistoricalTrade() {
        DerivativeTrade trade = validBuilder()
                .tradeDate(LocalDate.of(2019, 1, 1))
                .expiry(LocalDate.of(2020, 1, 1))
                .build();

        assertThat(trade.expiry()).isBefore(LocalDate.now());
    }

    @Test
    void builder_rejectsExpiryOnOrBeforeTradeDate() {
        LocalDate tradeDate = LocalDate.of(2026, 6, 3);

        assertThatThrownBy(() -> validBuilder().expiry(tradeDate).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("expiry must be after tradeDate");
        assertThatThrownBy(() -> validBuilder().expiry(tradeDate.minusDays(1)).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("expiry must be after tradeDate");
    }

    @Test
    void builder_rejectsNonPositiveStrikeAndQuantity() {
        assertThatThrownBy(() -> validBuilder().strike(BigDecimal.ZERO).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("strike must be > 0");
        assertThatThrownBy(() -> validBuilder().quantity(new BigDecimal("-1")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("quantity must be > 0");
    }

    @Test
    void builder_rejectsBlankUnderlying() {
        assertThatThrownBy(() -> validBuilder().underlying("   ").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("underlying must not be blank");
    }

    @Test
    void builder_missingRequiredField_throwsNamedException() {
        assertThatThrownBy(() -> DerivativeTrade.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("tradeRef");
    }

    private DerivativeTrade.Builder validBuilder() {
        return DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DRV-20260603-0001"))
                .underlying("SAP.DE")
                .strike(new BigDecimal("50"))
                .quantity(new BigDecimal("100"))
                .expiry(LocalDate.of(2027, 6, 3))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L);
    }
}
