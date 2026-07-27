package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class TradeTypeOrderingTest {

    @Test
    void treeSetOrdersHeterogeneousTradesNewestFirst() {
        TradeType fx = FXTrade.builder()
                .tradeRef(TradeRef.of("FXT-20260701-0001"))
                .ccy1("EUR")
                .ccy2("USD")
                .notionalCcy1(new BigDecimal("1000000"))
                .fxRate(new BigDecimal("1.10"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 1))
                .counterpartyId(1L)
                .build();

        TradeType equity = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260703-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 3))
                .counterpartyId(2L)
                .build();

        TradeType derivative = DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DER-20260702-0001"))
                .underlying("AAPL")
                .strike(new BigDecimal("190"))
                .quantity(new BigDecimal("10"))
                .expiry(LocalDate.of(2026, 9, 18))
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 2))
                .counterpartyId(3L)
                .build();

        TradeType bond = BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260630-0001"))
                .isin("US1234567890")
                .faceValue(new BigDecimal("500000"))
                .couponRate(new BigDecimal("0.045"))
                .maturityDate(LocalDate.of(2028, 6, 30))
                .currency("USD")
                .side(Side.SELL)
                .tradeDate(LocalDate.of(2026, 6, 30))
                .counterpartyId(4L)
                .build();

        TreeSet<TradeType> trades = new TreeSet<>(List.of(fx, equity, derivative, bond));

        assertThat(trades)
                .extracting(t -> t.tradeRef().value())
                .containsExactly(
                        "EQU-20260703-0001",
                        "DER-20260702-0001",
                        "FXT-20260701-0001",
                        "BND-20260630-0001"
                );
    }

    @Test
    void compareToBreaksSameDateTiesByTradeRefAscending() {
        TradeType first = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260703-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 3))
                .counterpartyId(1L)
                .build();

        TradeType second = EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260703-0002"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 3))
                .counterpartyId(2L)
                .build();

        assertThat(first.compareTo(second)).isLessThan(0);
        assertThat(second.compareTo(first)).isGreaterThan(0);
    }
}
