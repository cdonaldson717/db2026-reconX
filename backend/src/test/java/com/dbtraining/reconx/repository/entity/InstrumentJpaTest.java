package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:instrument-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InstrumentJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void roundTripsArbitraryJsonMetadata() {
        Instrument instrument = new Instrument();
        instrument.setSymbol("AAPL");
        instrument.setName("Apple Inc.");
        instrument.setAssetClass(AssetClass.EQUITY);
        instrument.setCurrency("USD");
        instrument.setMetadata(Map.of(
                "isin", "US0378331005",
                "cusip", "037833100",
                "lotSize", 100));

        entityManager.persist(instrument);
        entityManager.flush();
        Long id = instrument.getId();
        entityManager.clear();

        Instrument reloaded = entityManager.find(Instrument.class, id);
        assertThat(reloaded.getAssetClass()).isEqualTo(AssetClass.EQUITY);
        assertThat(reloaded.getMetadata()).containsEntry("isin", "US0378331005")
                .containsEntry("cusip", "037833100")
                .containsEntry("lotSize", 100);
    }
}
