package com.hiretrack.hiretrack_api.service;

import com.hiretrack.hiretrack_api.model.RefreshToken;
import com.hiretrack.hiretrack_api.model.User;
import com.hiretrack.hiretrack_api.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages refresh token lifecycle: creation, validation, rotation, and revocation.
 *
 * Security model:
 * - Each refresh token is single-use (rotated on every refresh)
 * - Tokens are revoked on logout and password change
 * - Expired/revoked tokens are cleaned up daily
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        token.setRevoked(false);

        RefreshToken saved = refreshTokenRepository.save(token);
        log.debug("Created refresh token for user: {}", user.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token);
    }

    /**
     * Validates and rotates refresh token.
     * Returns a new refresh token (old one is revoked).
     */
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        // Revoke old token (single-use)
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Check expiry
        if (oldToken.isExpired()) {
            log.warn("Attempted to use expired refresh token for user: {}", oldToken.getUser().getEmail());
            throw new RuntimeException("Refresh token has expired. Please login again.");
        }

        // Issue new token
        return createRefreshToken(oldToken.getUser());
    }

    /**
     * Revokes all refresh tokens for a user (logout from all devices).
     */
    @Transactional
    public void revokeAllTokens(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    /**
     * Cleanup job — runs daily at 3 AM to remove expired/revoked tokens.
     * Prevents the refresh_tokens table from growing unbounded.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.info("Cleaned up expired and revoked refresh tokens");
    }
}
