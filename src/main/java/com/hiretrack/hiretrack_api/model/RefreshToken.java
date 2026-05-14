package com.hiretrack.hiretrack_api.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Refresh token entity for JWT token rotation.
 *
 * Flow:
 * 1. User logs in → gets access token (15 min) + refresh token (7 days)
 * 2. Access token expires → client sends refresh token to /api/auth/refresh
 * 3. Server validates refresh token → issues new access token + new refresh token
 * 4. Old refresh token is invalidated (one-time use — prevents replay attacks)
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public RefreshToken() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
}
