package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.EquityTrade;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.Side;
import com.dbtraining.reconx.model.TradeRef;
import com.dbtraining.reconx.observability.ReconConfigMBean;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReconciliationEngineRuntimeConfigTest {

    @Test
    void readsLatestManagedToleranceForEveryRun() {
        ReconConfigMBean config = new ReconConfigMBean(new ConcurrentMapCacheManager());
        ReconciliationEngine engine = new ReconciliationEngine(config);
        EquityTrade internal = equity("100.00");
        EquityTrade external = equity("102.00");

        try {
            assertThat(engine.reconcile(List.of(internal), List.of(external),
                    ReconciliationRule.EXACT).getFirst().status()).isEqualTo(ReconResult.Status.BREAK);

            config.setPriceTolerance(0.02);

            assertThat(engine.reconcile(List.of(internal), List.of(external),
                    ReconciliationRule.EXACT).getFirst().status()).isEqualTo(ReconResult.Status.MATCHED);
        } finally {
            engine.shutdown();
        }
    }

    private EquityTrade equity(String price) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of("EQU-20260603-0096"))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
