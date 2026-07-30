package com.dbtraining.reconx.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TICKET-ADV078 — exercises the complete authenticated trade lifecycle over
 * HTTP against a real PostgreSQL database.
 */
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect")
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TradeLifecycleIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private JdbcTemplate jdbc;

    private static String token;
    private static Long createdId;
    private static String reconJobId;
    private static Long breakId;

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @Order(1)
    void loginAsAdmin() {
        String body = """
                {"email":"admin@db.com","password":"admin123"}
                """;

        ResponseEntity<JsonNode> response = http.postForEntity(
                "/auth/login",
                new HttpEntity<>(body, jsonHeaders()),
                JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        token = response.getBody().path("token").asText();
        assertFalse(token.isBlank());
    }

    @Test
    @Order(2)
    void createTrade() {
        String body = """
                {
                  "tradeRef":"INT-20260315-0001",
                  "instrumentId":1,
                  "counterpartyId":1,
                  "assetClass":"EQUITY",
                  "side":"BUY",
                  "quantity":100.0,
                  "price":245.50,
                  "tradeDate":"2026-03-15"
                }
                """;

        ResponseEntity<JsonNode> response = http.exchange(
                "/v1/trades",
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
                JsonNode.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        createdId = response.getBody().path("id").asLong();
        assertTrue(createdId > 0);
    }

    @Test
    @Order(3)
    void getTradeBack() {
        ResponseEntity<JsonNode> response = http.exchange(
                "/v1/trades?status=PENDING",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().path("totalElements").asLong() >= 1);
    }

    @Test
    @Order(4)
    void patchStatus() {
        String body = """
                {"status":"MATCHED"}
                """;

        ResponseEntity<JsonNode> response = http.exchange(
                "/v1/trades/" + createdId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(body, authHeaders()),
                JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MATCHED", response.getBody().path("status").asText());
    }

    @Test
    @Order(5)
    void triggerRecon() {
        String body = """
                {"from":"2026-03-01","to":"2026-03-31"}
                """;

        ResponseEntity<JsonNode> response = http.exchange(
                "/v1/recon/run",
                HttpMethod.POST,
                new HttpEntity<>(body, authHeaders()),
                JsonNode.class);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        reconJobId = response.getBody().path("jobId").asText();
        assertFalse(reconJobId.isBlank());
    }

    @Test
    @Order(6)
    void resolveBreak() {
        jdbc.update("""
                INSERT INTO recon_breaks (trade_id, discrepancy_type, status)
                VALUES (?, 'PRICE_MISMATCH', 'OPEN')
                """, createdId);

        ResponseEntity<JsonNode> results = http.exchange(
                "/v1/recon/jobs/" + reconJobId + "/results",
                HttpMethod.GET,
                new HttpEntity<>(authHeaders()),
                JsonNode.class);

        assertEquals(HttpStatus.OK, results.getStatusCode());
        assertNotNull(results.getBody());
        assertTrue(results.getBody().isArray());
        assertFalse(results.getBody().isEmpty());
        breakId = results.getBody().get(0).path("id").asLong();

        String body = """
                {"note":"Confirmed via counterparty email on 2026-03-16."}
                """;
        ResponseEntity<JsonNode> response = http.exchange(
                "/v1/recon/results/" + breakId + "/resolve",
                HttpMethod.PUT,
                new HttpEntity<>(body, authHeaders()),
                JsonNode.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RESOLVED", response.getBody().path("status").asText());
    }
}
