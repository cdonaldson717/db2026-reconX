package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.entity.Trade;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reads Hibernate Envers revision history for persisted trades.
 */
@Service
public class TradeHistoryService {

    private final EntityManager em;

    public TradeHistoryService(EntityManager em) {
        this.em = em;
    }

    /**
     * Return every recorded revision number for the given trade id.
     *
     * @param tradeId the trade primary key.
     * @return revision numbers in Envers order for this trade.
     */
    @Transactional(readOnly = true)
    public List<Number> revisionsFor(Long tradeId) {
        AuditReader reader = AuditReaderFactory.get(em);
        return reader.getRevisions(Trade.class, tradeId);
    }

    /**
     * Load the trade snapshot captured at a specific revision.
     *
     * @param tradeId the trade primary key.
     * @param revision the Envers revision number to inspect.
     * @return the trade state stored at that revision, or {@code null} if absent.
     */
    @Transactional(readOnly = true)
    public Trade snapshotAt(Long tradeId, Number revision) {
        AuditReader reader = AuditReaderFactory.get(em);
        return reader.find(Trade.class, tradeId, revision);
    }
}
