package com.dbtraining.reconx.repository.entity;

import com.dbtraining.reconx.config.JpaConfig;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:trade-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class TradeJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAuditFieldsAndKeepsRelationshipsLazy() {
        Counterparty counterparty = new Counterparty();
        counterparty.setName("Test Counterparty");
        counterparty.setLeiCode("549300TESTLEICODE01");
        counterparty.setRegion("EMEA");
        entityManager.persist(counterparty);

        Instrument instrument = new Instrument();
        instrument.setSymbol("TEST");
        instrument.setName("Test Instrument");
        instrument.setAssetClass(AssetClass.EQUITY);
        instrument.setCurrency("USD");
        entityManager.persist(instrument);

        Trade trade = new Trade();
        trade.setTradeRef("TST-20260728-0001");
        trade.setCounterparty(counterparty);
        trade.setInstrument(instrument);
        trade.setAssetClass("EQUITY");
        trade.setSide("BUY");
        trade.setQuantity(new BigDecimal("10.0000"));
        trade.setPrice(new BigDecimal("25.5000"));
        trade.setTradeDate(LocalDate.of(2026, 7, 28));
        entityManager.persist(trade);
        entityManager.flush();

        Long id = trade.getId();
        entityManager.clear();

        Trade reloaded = entityManager.find(Trade.class, id);
        assertThat(reloaded.getStatus()).isEqualTo(TradeStatus.PENDING);
        assertThat(reloaded.getCreatedAt()).isNotNull();
        assertThat(reloaded.getModifiedAt()).isNotNull();
        assertThat(Hibernate.isInitialized(reloaded.getCounterparty())).isFalse();
        assertThat(Hibernate.isInitialized(reloaded.getInstrument())).isFalse();

        assertThat(reloaded.getCounterparty().getName()).isEqualTo("Test Counterparty");
        assertThat(Hibernate.isInitialized(reloaded.getCounterparty())).isTrue();
    }
}
