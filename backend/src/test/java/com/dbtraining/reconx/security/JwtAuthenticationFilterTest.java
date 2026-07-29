package com.dbtraining.reconx.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenPopulatesAuthenticationBeforeContinuingChain() throws Exception {
        CountingTokenProvider provider = new CountingTokenProvider();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + provider.generate("admin@db.com", "ADMIN"));
        AtomicBoolean chainInvoked = new AtomicBoolean();

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.isAuthenticated()).isTrue();
            assertThat(authentication.getName()).isEqualTo("admin@db.com");
            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
            assertThat(authentication.getCredentials()).isNull();
            assertThat(authentication.getDetails()).isNotNull();
            chainInvoked.set(true);
        });

        assertThat(provider.parseCount).isEqualTo(1);
        assertThat(chainInvoked).isTrue();
    }

    @Test
    void missingOrNonBearerHeaderContinuesWithoutAuthentication() throws Exception {
        CountingTokenProvider provider = new CountingTokenProvider();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);

        for (String header : List.of("", "Basic abc123", "bearer abc123")) {
            SecurityContextHolder.clearContext();
            MockHttpServletRequest request = new MockHttpServletRequest();
            if (!header.isEmpty()) {
                request.addHeader("Authorization", header);
            }

            filter.doFilter(request, new MockHttpServletResponse(), (req, res) ->
                    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull());
        }

        assertThat(provider.parseCount).isZero();
    }

    @Test
    void malformedOrExpiredBearerTokenClearsContextAndContinuesChain() throws Exception {
        CountingTokenProvider provider = new CountingTokenProvider();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);
        assertRejected(filter, "not.a.real.token");
        assertRejected(filter, "");

        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1, "reconx-test");
        assertRejected(filter, expiredProvider.generate("admin@db.com", "ADMIN"));

        assertThat(provider.parseCount).isEqualTo(3);
    }

    @Test
    void asyncRedispatchSkipsFilterAndContinuesChain() throws Exception {
        CountingTokenProvider provider = new CountingTokenProvider();
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(provider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setDispatcherType(DispatcherType.ASYNC);
        request.addHeader("Authorization", "Bearer " + provider.generate("admin@db.com", "ADMIN"));
        AtomicBoolean chainInvoked = new AtomicBoolean();

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> chainInvoked.set(true));

        assertThat(provider.parseCount).isZero();
        assertThat(chainInvoked).isTrue();
    }

    private static void assertRejected(JwtAuthenticationFilter filter, String token) throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("stale-user", null, List.of()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        AtomicBoolean chainInvoked = new AtomicBoolean();

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            chainInvoked.set(true);
        });

        assertThat(chainInvoked).isTrue();
    }

    private static final class CountingTokenProvider extends JwtTokenProvider {
        private int parseCount;

        private CountingTokenProvider() {
            super(SECRET, 60, "reconx-test");
        }

        @Override
        public Claims parse(String token) {
            parseCount++;
            return super.parse(token);
        }
    }
}
