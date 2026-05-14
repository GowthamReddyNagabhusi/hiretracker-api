package com.hiretrack.hiretrack_api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set values via reflection (normally injected by Spring)
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-minimum-32-characters-for-hmac-sha256-testing");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 900000L); // 15 min
    }

    @Test
    @DisplayName("should generate a valid JWT token")
    void generateToken_returnsValidToken() {
        String token = jwtUtil.generateToken("test@example.com");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("should extract email from token")
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("test@example.com");
        String email = jwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("should validate a correct token")
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("test@example.com");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("should reject a tampered token")
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("test@example.com");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("should reject an expired token")
    void isTokenValid_expiredToken_returnsFalse() {
        // Set expiration to 0 milliseconds (immediately expired)
        ReflectionTestUtils.setField(jwtUtil, "expiration", 0L);
        String token = jwtUtil.generateToken("test@example.com");

        // Token is already expired
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("should reject null/empty tokens")
    void isTokenValid_nullToken_returnsFalse() {
        assertThat(jwtUtil.isTokenValid(null)).isFalse();
        assertThat(jwtUtil.isTokenValid("")).isFalse();
        assertThat(jwtUtil.isTokenValid("not-a-jwt")).isFalse();
    }
}
