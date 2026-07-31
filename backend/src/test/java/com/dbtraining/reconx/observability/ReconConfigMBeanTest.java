package com.dbtraining.reconx.observability;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ReconConfigMBeanTest {

    @Test
    void validatesPriceTolerance() {
        ReconConfigMBean config = new ReconConfigMBean(new ConcurrentMapCacheManager());

        config.setPriceTolerance(0.025);

        assertThat(config.getPriceTolerance()).isEqualTo(0.025);
        assertThatIllegalArgumentException().isThrownBy(() -> config.setPriceTolerance(-0.1));
        assertThatIllegalArgumentException().isThrownBy(() -> config.setPriceTolerance(1.1));
        assertThatIllegalArgumentException().isThrownBy(() -> config.setPriceTolerance(Double.NaN));
    }

    @Test
    void clearsEveryCacheAndDisablingCachingAlsoEvictsExistingEntries() {
        ConcurrentMapCacheManager cacheManager =
                new ConcurrentMapCacheManager("instruments", "counterparties");
        cacheManager.getCache("instruments").put("SAP.DE", "instrument");
        cacheManager.getCache("counterparties").put(1L, "counterparty");
        ReconConfigMBean config = new ReconConfigMBean(cacheManager);

        config.setCachingEnabled(false);

        assertThat(config.isCachingEnabled()).isFalse();
        assertThat(cacheManager.getCache("instruments").get("SAP.DE")).isNull();
        assertThat(cacheManager.getCache("counterparties").get(1L)).isNull();
    }
}
