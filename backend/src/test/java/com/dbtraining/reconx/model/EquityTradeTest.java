package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EquityTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {
        EquityTrade trade = sampleEquity("EQU-20260603-0001");

        assertThat(trade.tradeRef()).isEqualTo(TradeRef.of("EQU-20260603-0001"));
        assertThat(trade.notional().amount()).isEqualByComparingTo("10000");
        assertThat(trade.notional().currency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.EQUITY);
    }

    @Test
    void builder_missingPrice_throws() {
        assertThatThrownBy(() -> EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("price");
    }

    @Test
    void equality_byTradeRef() {
        EquityTrade first = sampleEquity("EQU-20260603-0001");
        EquityTrade sameRef = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0001"))
                .instrumentSymbol("SIE.DE")
                .quantity(new BigDecimal("250"))
                .price(new BigDecimal("175"))
                .currency("EUR").side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 6, 4))
                .counterpartyId(2L).build();
        EquityTrade differentRef = sampleEquity("EQU-20260603-0002");

        assertThat(first).isEqualTo(sameRef).hasSameHashCodeAs(sameRef);
        assertThat(first).isNotEqualTo(differentRef);
        assertThat(new HashSet<TradeType>(List.of(first, sameRef))).hasSize(1);
    }

    private EquityTrade sampleEquity(String ref) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L).build();
    }
}
