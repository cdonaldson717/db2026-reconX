package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.repository.entity.TradeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TradeMapperTest {

    private final TradeMapper mapper = new TradeMapperImpl();

    @Test
    void toResponse_flattensNestedCounterpartyAndInstrumentFields() {
        Counterparty counterparty = new Counterparty();
        ReflectionTestUtils.setField(counterparty, "id", 42L);
        counterparty.setName("Goldman Sachs");

        Instrument instrument = new Instrument();
        ReflectionTestUtils.setField(instrument, "id", 7L);
        ReflectionTestUtils.setField(instrument, "symbol", "SAP.DE");

        Trade trade = new Trade();
        ReflectionTestUtils.setField(trade, "id", 99L);
        trade.setTradeRef("EQU-20260603-0001");
        trade.setCounterparty(counterparty);
        trade.setInstrument(instrument);
        trade.setAssetClass("EQUITY");
        trade.setSide("BUY");
        trade.setQuantity(new BigDecimal("1000"));
        trade.setPrice(new BigDecimal("125.50"));
        trade.setTradeDate(LocalDate.of(2026, 6, 3));
        trade.setStatus("PENDING");
        ReflectionTestUtils.setField(trade, "createdAt", Instant.parse("2026-06-03T10:15:30Z"));
        ReflectionTestUtils.setField(trade, "modifiedAt", Instant.parse("2026-06-03T11:00:00Z"));

        TradeResponse response = mapper.toResponse(trade);

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.tradeRef()).isEqualTo("EQU-20260603-0001");
        assertThat(response.counterpartyId()).isEqualTo(42L);
        assertThat(response.counterpartyName()).isEqualTo("Goldman Sachs");
        assertThat(response.instrumentId()).isEqualTo(7L);
        assertThat(response.instrumentSymbol()).isEqualTo("SAP.DE");
        assertThat(response.assetClass()).isEqualTo("EQUITY");
        assertThat(response.side()).isEqualTo("BUY");
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void toEntity_mapsWireFieldsAndLeavesServiceOwnedFieldsUnset() {
        TradeRequest request = new TradeRequest(
                "EQU-20260603-0002",
                7L,
                42L,
                "EQUITY",
                "SELL",
                new BigDecimal("250"),
                new BigDecimal("99.95"),
                LocalDate.of(2026, 6, 3));

        Trade trade = mapper.toEntity(request);

        assertThat(trade.getId()).isNull();
        assertThat(trade.getTradeRef()).isEqualTo("EQU-20260603-0002");
        assertThat(trade.getInstrument()).isNull();
        assertThat(trade.getCounterparty()).isNull();
        assertThat(trade.getAssetClass()).isEqualTo("EQUITY");
        assertThat(trade.getSide()).isEqualTo("SELL");
        assertThat(trade.getQuantity()).isEqualByComparingTo("250");
        assertThat(trade.getPrice()).isEqualByComparingTo("99.95");
        assertThat(trade.getTradeDate()).isEqualTo(LocalDate.of(2026, 6, 3));
        assertThat(trade.getStatus()).isEqualTo(TradeStatus.PENDING);
        assertThat(trade.getDeletedAt()).isNull();
        assertThat(trade.getCreatedAt()).isNull();
        assertThat(trade.getModifiedAt()).isNull();
    }
}
