package com.dbtraining.reconx.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long";

    @Test
    void generateCreatesSignedTokenWithRequiredClaims() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60, "reconx-test");
        Instant beforeGeneration = Instant.now().minusSeconds(1);

        Claims claims = provider.parse(provider.generate("trader@db.com", "TRADER"));

        assertThat(claims.getSubject()).isEqualTo("trader@db.com");
        assertThat(claims.getIssuer()).isEqualTo("reconx-test");
        assertThat(claims.get("role", String.class)).isEqualTo("TRADER");
        assertThat(claims.getIssuedAt().toInstant()).isAfterOrEqualTo(beforeGeneration);
        assertThat(claims.getExpiration().toInstant())
                .isAfterOrEqualTo(claims.getIssuedAt().toInstant().plusSeconds(3_599));
        assertThat(provider.expirationSeconds()).isEqualTo(3_600);
    }

    @Test
    void parseRejectsTokensSignedByAnotherSecretOrIssuer() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60, "reconx-test");
        String wrongSignature = new JwtTokenProvider(
                "another-test-secret-that-is-at-least-32-bytes", 60, "reconx-test")
                .generate("trader@db.com", "TRADER");
        String wrongIssuer = new JwtTokenProvider(SECRET, 60, "another-issuer")
                .generate("trader@db.com", "TRADER");

        assertThatThrownBy(() -> provider.parse(wrongSignature)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> provider.parse(wrongIssuer)).isInstanceOf(JwtException.class);
    }
}
