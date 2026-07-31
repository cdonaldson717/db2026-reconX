package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TradeMetricsTest {

    @Test
    void incrementTradeCreated_increasesCounterByOne() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ReconBreakRepository breakRepository =
                mock(ReconBreakRepository.class);

        TradeMetrics metrics =
                new TradeMetrics(registry, breakRepository);

        double before = registry
                .get("trade_created_total")
                .counter()
                .count();

        metrics.incrementTradeCreated();

        double after = registry
                .get("trade_created_total")
                .counter()
                .count();

        assertThat(after).isEqualTo(before + 1.0);
    }

    @Test
    void tradeCreatedCounter_hasDescription() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ReconBreakRepository breakRepository =
                mock(ReconBreakRepository.class);

        new TradeMetrics(registry, breakRepository);

        String description = registry
                .get("trade_created_total")
                .counter()
                .getId()
                .getDescription();

        assertThat(description)
                .isEqualTo("Total trades created");
    }
}