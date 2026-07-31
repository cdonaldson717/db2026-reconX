package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.TradeRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TradesByStatusGaugeTest {

    @Test
    void registersGaugeForEveryTradeStatus() {
        TradeRepository repository = statusCountingRepository();
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        new TradesByStatusGauge(registry, repository);

        for (int index = 0; index < TradesByStatusGauge.STATUSES.size(); index++) {
            String status = TradesByStatusGauge.STATUSES.get(index);
            double value = registry.get("trades_by_status")
                    .tag("status", status)
                    .gauge()
                    .value();

            assertEquals(index, value);
        }
    }

    private TradeRepository statusCountingRepository() {
        return (TradeRepository) Proxy.newProxyInstance(
                TradeRepository.class.getClassLoader(),
                new Class<?>[]{TradeRepository.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("countByStatus")) {
                        return (long) TradesByStatusGauge.STATUSES.indexOf(arguments[0]);
                    }
                    if (method.getName().equals("toString")) {
                        return "statusCountingRepository";
                    }
                    if (method.getName().equals("hashCode")) {
                        return System.identityHashCode(proxy);
                    }
                    if (method.getName().equals("equals")) {
                        return proxy == arguments[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
