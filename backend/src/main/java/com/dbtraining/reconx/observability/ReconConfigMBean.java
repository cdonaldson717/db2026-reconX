package com.dbtraining.reconx.observability;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/** Runtime reconciliation and cache controls exposed through JMX. */
@Component
@ManagedResource(
        objectName = "reconx:type=ReconConfig",
        description = "Runtime tuning for the reconciliation engine")
public class ReconConfigMBean {

    private final CacheManager cacheManager;
    private volatile double priceTolerance = 0.01;
    private volatile boolean cachingEnabled = true;

    public ReconConfigMBean(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @ManagedAttribute(description = "Price tolerance for break detection (0.0 - 1.0)")
    public double getPriceTolerance() {
        return priceTolerance;
    }

    @ManagedAttribute(description = "Price tolerance for break detection (0.0 - 1.0)")
    public void setPriceTolerance(double priceTolerance) {
        if (!Double.isFinite(priceTolerance) || priceTolerance < 0.0 || priceTolerance > 1.0) {
            throw new IllegalArgumentException("price tolerance must be a finite value from 0.0 to 1.0");
        }
        this.priceTolerance = priceTolerance;
    }

    @ManagedAttribute(description = "Whether method-level caching is enabled")
    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    @ManagedAttribute(description = "Whether method-level caching is enabled")
    public void setCachingEnabled(boolean cachingEnabled) {
        this.cachingEnabled = cachingEnabled;
        if (!cachingEnabled) {
            clearCache();
        }
    }

    @ManagedOperation(description = "Evict all entries from every application cache")
    public void clearCache() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
