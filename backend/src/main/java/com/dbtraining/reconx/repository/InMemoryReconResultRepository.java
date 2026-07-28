package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.dto.ReconResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Runtime repository for reconciliation results until a database-backed result
 * entity is introduced.
 */
@Repository
public class InMemoryReconResultRepository implements ReconResultRepository {

    private final List<ReconResult> results = new CopyOnWriteArrayList<>();

    @Override
    public ReconResult save(ReconResult result) {
        results.add(result);
        return result;
    }

    @Override
    public List<ReconResult> findAll() {
        return List.copyOf(results);
    }
}
