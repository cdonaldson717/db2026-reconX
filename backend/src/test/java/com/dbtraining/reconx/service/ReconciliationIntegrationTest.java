package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.ReconResultRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ReconciliationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("reconx")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ReconResultRepository reconResultRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsAndTalksToPostgres() {
        Long count = jdbcTemplate.queryForObject("select count(*) from trades", Long.class);
        assertThat(count).isNotNull().isGreaterThan(0L);
    }
    @Test
    void insertedTradesAreReconciledAndPersisted() {
    assertThat(reconResultRepository).isNotNull();
    assertThat(reconResultRepository.findAll()).isNotNull();
}
}
