package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FXTradeTest {

    @Test
    void builder_buildsAndConvertsNotionalToQuoteCurrency() {
        FXTrade trade = validBuilder().build();

        assertThat(trade.ccy1().getCurrencyCode()).isEqualTo("EUR");
        assertThat(trade.ccy2().getCurrencyCode()).isEqualTo("USD");
        assertThat(trade.notionalCcy1()).isEqualByComparingTo("1000");
        assertThat(trade.notional().amount()).isEqualByComparingTo("1100");
        assertThat(trade.notional().currency().getCurrencyCode()).isEqualTo("USD");
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.FX);
    }

    @Test
    void currencySetter_rejectsInvalidIsoCodeImmediately() {
        assertThatThrownBy(() -> FXTrade.builder().ccy1("EURR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void builder_rejectsEqualCurrencies() {
        assertThatThrownBy(() -> validBuilder().ccy2("EUR").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ccy1 and ccy2 must differ");
    }

    @Test
    void builder_rejectsNonPositiveNotionalAndRate() {
        assertThatThrownBy(() -> validBuilder().notionalCcy1(BigDecimal.ZERO).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("notionalCcy1 must be > 0");

        assertThatThrownBy(() -> validBuilder().fxRate(new BigDecimal("-1")).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("fxRate must be > 0");
    }

    @Test
    void builder_missingRequiredField_throwsNamedException() {
        assertThatThrownBy(() -> FXTrade.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("tradeRef");
    }

    private FXTrade.Builder validBuilder() {
        return FXTrade.builder()
                .tradeRef(TradeRef.of("FXS-20260603-0001"))
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("1000"))
                .fxRate(new BigDecimal("1.10"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L);
    }
}
