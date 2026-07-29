package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.entity.Trade;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TradeHistoryServiceTest {

    @Test
    void revisionsFor_returnsAuditReaderRevisionList() {
        EntityManager em = mock(EntityManager.class);
        AuditReader reader = mock(AuditReader.class);
        TradeHistoryService service = new TradeHistoryService(em);
        List<Number> revisions = List.of(1, 2, 3, 4);

        try (MockedStatic<AuditReaderFactory> factory = mockStatic(AuditReaderFactory.class)) {
            factory.when(() -> AuditReaderFactory.get(em)).thenReturn(reader);
            when(reader.getRevisions(Trade.class, 42L)).thenReturn(revisions);

            List<Number> out = service.revisionsFor(42L);

            assertThat(out).isEqualTo(revisions);
            verify(reader).getRevisions(Trade.class, 42L);
        }
    }

    @Test
    void snapshotAt_returnsTradeSnapshotForRevision() {
        EntityManager em = mock(EntityManager.class);
        AuditReader reader = mock(AuditReader.class);
        TradeHistoryService service = new TradeHistoryService(em);
        Trade snapshot = new Trade();

        try (MockedStatic<AuditReaderFactory> factory = mockStatic(AuditReaderFactory.class)) {
            factory.when(() -> AuditReaderFactory.get(em)).thenReturn(reader);
            when(reader.find(Trade.class, 42L, 7)).thenReturn(snapshot);

            Trade out = service.snapshotAt(42L, 7);

            assertThat(out).isSameAs(snapshot);
            verify(reader).find(Trade.class, 42L, 7);
        }
    }

    @Test
    void methods_areMarkedTransactionalReadOnly() throws NoSuchMethodException {
        Transactional revisionsTx = TradeHistoryService.class
                .getMethod("revisionsFor", Long.class)
                .getAnnotation(Transactional.class);
        Transactional snapshotTx = TradeHistoryService.class
                .getMethod("snapshotAt", Long.class, Number.class)
                .getAnnotation(Transactional.class);

        assertThat(revisionsTx).isNotNull();
        assertThat(revisionsTx.readOnly()).isTrue();
        assertThat(snapshotTx).isNotNull();
        assertThat(snapshotTx.readOnly()).isTrue();
    }
}
