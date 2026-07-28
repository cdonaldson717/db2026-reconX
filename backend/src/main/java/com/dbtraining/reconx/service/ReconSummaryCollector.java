package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.dto.ReconResult.Status;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/** Collects reconciliation rows into immutable outcome counts. */
public final class ReconSummaryCollector
        implements Collector<ReconResult, ReconSummary.Builder, ReconSummary> {

    @Override
    public Supplier<ReconSummary.Builder> supplier() {
        return ReconSummary.Builder::new;
    }

    @Override
    public BiConsumer<ReconSummary.Builder, ReconResult> accumulator() {
        return (summary, result) -> {
            summary.total++;
            if (result.status() == Status.MATCHED) {
                summary.matched++;
            } else {
                summary.broken++;
            }
        };
    }

    @Override
    public BinaryOperator<ReconSummary.Builder> combiner() {
        return (left, right) -> {
            ReconSummary.Builder combined = new ReconSummary.Builder();
            combined.total = left.total + right.total;
            combined.matched = left.matched + right.matched;
            combined.broken = left.broken + right.broken;
            return combined;
        };
    }

    @Override
    public Function<ReconSummary.Builder, ReconSummary> finisher() {
        return summary -> new ReconSummary(
                summary.total, summary.matched, summary.broken);
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
