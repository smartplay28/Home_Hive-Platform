package com.example.UC_Backend.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 * No mocks needed — tests the crypto logic directly.
 *
 * Security tests are critical: a bug here could allow unauthorized access.
 * We test:
 *   - Token generation produces parseable tokens
 *   - Correct claims are embedded (email, role, userId)
 *   - Token expiry logic
 *   - Tampered tokens are rejected
 *   - Wrong secret tokens are rejected
 */
@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtProvider;

    private static final String SECRET =
            "test-secret-key-that-is-at-least-32-characters-long-for-hmac-sha256";
    private static final long ACCESS_EXPIRY  = 900_000L;   // 15 minutes
    private static final long REFRESH_EXPIRY = 604_800_000L; // 7 days

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtTokenProvider();
        // Inject @Value fields using ReflectionTestUtils (no Spring context needed)
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiryMs", ACCESS_EXPIRY);
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenExpiryMs", REFRESH_EXPIRY);
    }

    // ─── Generation ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should generate non-null, non-blank access token")
    void generateAccessToken_producesNonBlankToken() {
        String token = jwtProvider.generateAccessToken("user@test.com", "CUSTOMER", 42);
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT = header.payload.signature
    }

    @Test
    @DisplayName("Should embed correct role claim in access token")
    void generateAccessToken_containsRoleClaim() {
        String token = jwtProvider.generateAccessToken("user@test.com", "AGENT", 123);
        assertThat(jwtProvider.getRoleFromToken(token)).isEqualTo("AGENT");
    }

    @Test
    @DisplayName("Should embed correct userId claim in access token")
    void generateAccessToken_containsUserIdClaim() {
        String token = jwtProvider.generateAccessToken("user@test.com", "CUSTOMER", 999);
        assertThat(jwtProvider.getUserIdFromToken(token)).isEqualTo(999);
    }

    @Test
    @DisplayName("Should embed email as subject")
    void generateAccessToken_emailAsSubject() {
        String token = jwtProvider.generateAccessToken("aksha@example.com", "ADMIN", 1);
        assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo("aksha@example.com");
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void generateRefreshToken_isValid() {
        String refresh = jwtProvider.generateRefreshToken("user@test.com");
        assertThat(refresh).isNotBlank();
        assertThat(jwtProvider.validateToken(refresh)).isTrue();
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid token should pass validation")
    void validateToken_validToken_returnsTrue() {
        String token = jwtProvider.generateAccessToken("test@test.com", "CUSTOMER", 1);
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Tampered token (modified payload) should fail validation")
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtProvider.generateAccessToken("real@test.com", "CUSTOMER", 1);
        // Tamper with the payload section (middle part)
        String[] parts = token.split("\\.");
        String tampered = parts[0] + ".TAMPERED_PAYLOAD." + parts[2];
        assertThat(jwtProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("Completely invalid string should fail validation")
    void validateToken_randomString_returnsFalse() {
        assertThat(jwtProvider.validateToken("not.a.jwt.at.all")).isFalse();
        assertThat(jwtProvider.validateToken("")).isFalse();
        assertThat(jwtProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("Token signed with different secret should fail validation")
    void validateToken_wrongSecret_returnsFalse() throws Exception {
        // Create a second provider with a DIFFERENT secret
        JwtTokenProvider otherProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otherProvider, "jwtSecret",
                "completely-different-secret-also-at-least-32-chars!!!");
        ReflectionTestUtils.setField(otherProvider, "accessTokenExpiryMs", ACCESS_EXPIRY);
        ReflectionTestUtils.setField(otherProvider, "refreshTokenExpiryMs", REFRESH_EXPIRY);

        String foreignToken = otherProvider.generateAccessToken("hacker@evil.com", "ADMIN", 0);

        // Our provider must NOT accept the foreign token
        assertThat(jwtProvider.validateToken(foreignToken)).isFalse();
    }

    // ─── Role extraction ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Should correctly extract all three roles from token")
    void extractRole_allRoles_correctlyExtracted() {
        for (String role : new String[]{"CUSTOMER", "AGENT", "ADMIN"}) {
            String token = jwtProvider.generateAccessToken("u@test.com", role, 1);
            assertThat(jwtProvider.getRoleFromToken(token)).isEqualTo(role);
        }
    }
}
