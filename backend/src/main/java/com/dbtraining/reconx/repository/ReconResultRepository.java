package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.dto.ReconResult;

import java.util.List;

/**
 * Repository abstraction for persisted reconciliation outcomes.
 */
public interface ReconResultRepository {

    /**
     * Persist one reconciliation result row.
     *
     * @param result the reconciliation result to persist.
     * @return the persisted result, potentially enriched by the storage layer.
     */
    ReconResult save(ReconResult result);

    /**
     * Load every persisted reconciliation result.
     *
     * @return all currently stored reconciliation results.
     */
    List<ReconResult> findAll();
}
