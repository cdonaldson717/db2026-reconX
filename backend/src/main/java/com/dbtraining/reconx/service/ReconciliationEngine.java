package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.BondTrade;
import com.dbtraining.reconx.model.DerivativeTrade;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.FXTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.observability.ReconConfigMBean;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * TICKET-ADV033 — ReconciliationEngine using Streams (parallel matching)
 * TICKET-ADV037 — CompletableFuture: parallel recon by counterparty
 * TICKET-ADV047 — Edge cases: empty/single/all-mismatched inputs handled
 * TICKET-ADV084 — @Timed exports reconciliation_duration_seconds histogram
 *
 * WHAT:    Compares internal trades against external (counterparty) trades and
 *          returns a ReconResult per internal trade (MATCHED or BREAK).
 * HOW:     Index externals by tradeRef, then stream internals and look each
 *          up. CompletableFuture variant batches by counterparty for
 *          throughput on large books.
 * WHY:     This is the spine of the product. Everything else (REST API,
 *          Kafka consumers, dashboard) ultimately calls into here.
 * OBSERVE: Histogram appears at /actuator/prometheus under
 *          reconciliation_duration_seconds.
 * ============================================================================
 */
@Service
public class ReconciliationEngine {

    private static final AtomicInteger THREAD_SEQUENCE = new AtomicInteger();

    private final ExecutorService executor;
    private final ReconConfigMBean runtimeConfig;

    public ReconciliationEngine() {
        this(Executors.newFixedThreadPool(
                Math.max(1, Runtime.getRuntime().availableProcessors()),
                reconciliationThreadFactory()), null);
    }

    ReconciliationEngine(ExecutorService executor) {
        this(executor, null);
    }

    @Autowired
    public ReconciliationEngine(ReconConfigMBean runtimeConfig) {
        this(Executors.newFixedThreadPool(
                Math.max(1, Runtime.getRuntime().availableProcessors()),
                reconciliationThreadFactory()), runtimeConfig);
    }

    private ReconciliationEngine(ExecutorService executor, ReconConfigMBean runtimeConfig) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.runtimeConfig = runtimeConfig;
    }

    private static ThreadFactory reconciliationThreadFactory() {
        return task -> {
            Thread thread = new Thread(task,
                    "recon-counterparty-" + THREAD_SEQUENCE.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    @Timed(value = "reconciliation.duration", description = "Wall time of reconcile()",
           percentiles = {0.5, 0.95, 0.99}, histogram = true)
    public List<ReconResult> reconcile(List<TradeType> internal,
                                       List<TradeType> external,
                                       ReconciliationRule rule) {
        if (internal == null || internal.isEmpty()) {
            return List.of();
        }
        Objects.requireNonNull(rule, "rule");

        Map<String, TradeType> externalByRef = (external == null ? List.<TradeType>of() : external)
                .stream()
                .collect(Collectors.toMap(
                        trade -> trade.tradeRef().value(),
                        Function.identity(),
                        (first, duplicate) -> first));

        return internal.parallelStream()
                .map(trade -> matchOne(
                        trade,
                        externalByRef.get(trade.tradeRef().value()),
                        rule))
                .toList();
    }

    /**
     * TICKET-ADV037 — split by counterparty, reconcile each batch concurrently,
     * combine into a single result list. Caller passes one external feed per
     * counterparty (typical real-world shape).
     */
    public CompletableFuture<List<ReconResult>> reconcileByCounterparty(
            Map<Long, List<TradeType>> internalByCp,
            Map<Long, List<TradeType>> externalByCp,
            ReconciliationRule rule) {
        Objects.requireNonNull(internalByCp, "internalByCp");
        Objects.requireNonNull(externalByCp, "externalByCp");
        Objects.requireNonNull(rule, "rule");

        List<CompletableFuture<List<ReconResult>>> futures = internalByCp.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(
                        () -> reconcile(
                                entry.getValue(),
                                externalByCp.getOrDefault(entry.getKey(), List.of()),
                                rule),
                        executor))
                .toList();

        CompletableFuture<Void> allCompleted = CompletableFuture.allOf(
                futures.toArray(CompletableFuture[]::new));
        return allCompleted.thenApply(ignored -> futures.stream()
                .flatMap(future -> future.join().stream())
                .toList());
    }

    private ReconResult matchOne(TradeType internal, TradeType external, ReconciliationRule rule) {
        String ref = internal.tradeRef().value();
        if (external == null) {
            return ReconResult.breakResult(
                    ref, "MISSING_EXTERNAL", "No external trade found for " + ref);
        }

        BigDecimal[] internalPair = priceQty(internal);
        BigDecimal[] externalPair = priceQty(external);
        if (matches(internalPair, externalPair, rule)) {
            return ReconResult.matched(ref);
        }
        return ReconResult.breakResult(
                ref,
                "VALUE_MISMATCH",
                "internal=%s/%s external=%s/%s".formatted(
                        internalPair[0], internalPair[1], externalPair[0], externalPair[1]));
    }

    private boolean matches(BigDecimal[] internal, BigDecimal[] external, ReconciliationRule rule) {
        if (runtimeConfig == null) {
            return rule.matches(internal[0], internal[1], external[0], external[1]);
        }

        BigDecimal priceDifference = internal[0].subtract(external[0]).abs();
        BigDecimal allowedPriceDifference = internal[0].abs()
                .multiply(BigDecimal.valueOf(runtimeConfig.getPriceTolerance()));
        BigDecimal quantityDifference = internal[1].subtract(external[1]).abs();
        return priceDifference.compareTo(allowedPriceDifference) <= 0
                && quantityDifference.compareTo(rule.qtyToleranceAbs()) <= 0;
    }

    /** TICKET-ADV018 — exhaustive switch over the sealed hierarchy. */
    private BigDecimal[] priceQty(TradeType t) {
        return switch (t) {
            case EquityTrade equity -> new BigDecimal[]{equity.price(), equity.quantity()};
            case FXTrade fx -> new BigDecimal[]{fx.fxRate(), fx.notionalCcy1()};
            case BondTrade bond -> new BigDecimal[]{bond.couponRate(), bond.faceValue()};
            case DerivativeTrade derivative ->
                    new BigDecimal[]{derivative.strike(), derivative.quantity()};
        };
    }

    /** Stops the bounded executor owned by this service. */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
