package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import com.dbtraining.reconx.model.TradeType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationEngineAsyncTest {

    @Test
    void reconcileByCounterparty_usesExplicitExecutorAndMergesEveryResult() {
        ExecutorService executor = Executors.newFixedThreadPool(
                2, task -> new Thread(task, "test-recon-worker"));
        Set<String> threadsUsed = ConcurrentHashMap.newKeySet();
        ReconciliationEngine engine = new ReconciliationEngine(executor) {
            @Override
            public List<ReconResult> reconcile(List<TradeType> internal,
                                               List<TradeType> external,
                                               ReconciliationRule rule) {
                threadsUsed.add(Thread.currentThread().getName());
                return super.reconcile(internal, external, rule);
            }
        };

        try {
            EquityTrade first = equity("EQU-20260603-0001", "100");
            EquityTrade second = equity("EQU-20260603-0002", "200");
            EquityTrade third = equity("EQU-20260603-0003", "300");
            Map<Long, List<TradeType>> internal = Map.of(
                    1L, List.of(first, second),
                    2L, List.of(third));
            Map<Long, List<TradeType>> external = Map.of(
                    1L, List.of(first, second),
                    2L, List.of(third));

            List<ReconResult> results = engine
                    .reconcileByCounterparty(internal, external, ReconciliationRule.EXACT)
                    .join();

            assertThat(results).hasSize(3)
                    .allMatch(result -> result.status() == ReconResult.Status.MATCHED);
            assertThat(threadsUsed).isNotEmpty()
                    .allMatch(name -> name.startsWith("test-recon-worker"));
        } finally {
            engine.shutdown();
        }

        assertThat(executor.isShutdown()).isTrue();
    }

    @Test
    void reconcileByCounterparty_handlesMissingExternalBatchAndEmptyInput() {
        ReconciliationEngine engine = new ReconciliationEngine();
        try {
            EquityTrade trade = equity("EQU-20260603-0004", "100");

            List<ReconResult> missing = engine.reconcileByCounterparty(
                    Map.of(9L, List.of(trade)), Map.of(), ReconciliationRule.EXACT).join();
            List<ReconResult> empty = engine.reconcileByCounterparty(
                    Map.of(), Map.of(), ReconciliationRule.EXACT).join();

            assertThat(missing).singleElement().satisfies(result -> {
                assertThat(result.status()).isEqualTo(ReconResult.Status.BREAK);
                assertThat(result.discrepancyType()).isEqualTo("MISSING_EXTERNAL");
            });
            assertThat(empty).isEmpty();
        } finally {
            engine.shutdown();
        }
    }

    private EquityTrade equity(String ref, String price) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal("10"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
