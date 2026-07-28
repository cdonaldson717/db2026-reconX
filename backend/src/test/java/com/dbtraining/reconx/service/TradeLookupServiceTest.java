package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeLookupServiceTest {

    @Test
    void counterpartyForTradeRef_returnsResolvedCounterparty() {
        Counterparty association = counterparty(42L, "Associated");
        Counterparty expected = counterparty(42L, "Resolved");
        Trade trade = tradeWith(association);
        TradeLookupService service = new TradeLookupService(
                tradeRepository(Map.of("EQU-20260603-0001", trade)),
                counterpartyRepository(Map.of(42L, expected), new AtomicBoolean()));

        Counterparty actual = service.counterpartyForTradeRef("EQU-20260603-0001");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void counterpartyForTradeRef_missingTrade_throwsWithReference() {
        AtomicBoolean counterpartyLookupCalled = new AtomicBoolean();
        TradeLookupService service = new TradeLookupService(
                tradeRepository(Map.of()),
                counterpartyRepository(Map.of(), counterpartyLookupCalled));

        assertThatThrownBy(() -> service.counterpartyForTradeRef("MISSING-REF"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("MISSING-REF");
        assertThat(counterpartyLookupCalled).isFalse();
    }

    @Test
    void counterpartyForTradeRef_unresolvableCounterparty_throwsWithReference() {
        Trade trade = tradeWith(counterparty(99L, "Missing"));
        TradeLookupService service = new TradeLookupService(
                tradeRepository(Map.of("EQU-20260603-0002", trade)),
                counterpartyRepository(Map.of(), new AtomicBoolean()));

        assertThatThrownBy(() -> service.counterpartyForTradeRef("EQU-20260603-0002"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("EQU-20260603-0002");
    }

    private Trade tradeWith(Counterparty counterparty) {
        Trade trade = new Trade();
        trade.setCounterparty(counterparty);
        return trade;
    }

    private Counterparty counterparty(long id, String name) {
        Counterparty counterparty = new Counterparty();
        counterparty.setName(name);
        try {
            Field idField = Counterparty.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(counterparty, id);
            return counterparty;
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to prepare counterparty fixture", ex);
        }
    }

    private TradeRepository tradeRepository(Map<String, Trade> trades) {
        return (TradeRepository) Proxy.newProxyInstance(
                TradeRepository.class.getClassLoader(),
                new Class<?>[]{TradeRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findByTradeRef")) {
                        return Optional.ofNullable(trades.get((String) args[0]));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private CounterpartyRepository counterpartyRepository(
            Map<Long, Counterparty> counterparties,
            AtomicBoolean lookupCalled) {
        return (CounterpartyRepository) Proxy.newProxyInstance(
                CounterpartyRepository.class.getClassLoader(),
                new Class<?>[]{CounterpartyRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findById")) {
                        lookupCalled.set(true);
                        return Optional.ofNullable(counterparties.get((Long) args[0]));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
