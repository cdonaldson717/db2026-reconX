package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.ReconciliationRule;
import com.dbtraining.reconx.model.TradeType;
import com.dbtraining.reconx.repository.ReconResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that delegates reconciliation to the engine and persists each result.
 */
@Service
public class ReconciliationService {

    private final ReconciliationEngine engine;
    private final ReconResultRepository repo;

    public ReconciliationService(ReconciliationEngine engine, ReconResultRepository repo) {
        this.engine = engine;
        this.repo = repo;
    }

    /**
     * Run reconciliation and persist every produced result row.
     *
     * @param internal the internal trade book to reconcile.
     * @param external the external/counterparty trade book to reconcile against.
     * @param rule the tolerance rule applied to each trade pair.
     * @return the in-memory reconciliation results returned by the engine.
     */
    public List<ReconResult> runRecon(List<TradeType> internal,
                                      List<TradeType> external,
                                      ReconciliationRule rule) {
        List<ReconResult> out = engine.reconcile(internal, external, rule);
        out.forEach(repo::save);
        return out;
    }
}
