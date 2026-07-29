package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ReconSummaryCollectorTest {

    @Test
    void serialAndParallelCollectionProduceIdenticalSummaryForTenThousandResults() {
        List<ReconResult> results = IntStream.range(0, 10_000)
                .mapToObj(index -> index % 4 == 0
                        ? ReconResult.matched("REF-" + index)
                        : ReconResult.breakResult("REF-" + index, "VALUE_MISMATCH", "details"))
                .toList();

        ReconSummary serial = results.stream().collect(new ReconSummaryCollector());
        ReconSummary parallel = results.parallelStream().collect(new ReconSummaryCollector());

        assertThat(serial).isEqualTo(new ReconSummary(10_000, 2_500, 7_500));
        assertThat(parallel).isEqualTo(serial);
    }

    @Test
    void emptyStreamProducesEmptySummary() {
        ReconSummary summary = List.<ReconResult>of().stream()
                .collect(new ReconSummaryCollector());

        assertThat(summary).isEqualTo(ReconSummary.empty());
    }

    @Test
    void collectorIsUnorderedButNeitherConcurrentNorIdentityFinish() {
        assertThat(new ReconSummaryCollector().characteristics())
                .isEqualTo(Set.of(java.util.stream.Collector.Characteristics.UNORDERED));
    }
}
