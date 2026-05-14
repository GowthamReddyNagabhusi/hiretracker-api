-- ============================================================
-- V2__add_refresh_tokens.sql
-- Adds refresh token table for JWT token rotation
-- ============================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    token       VARCHAR(255) NOT NULL,
    expiry_date DATETIME(6)  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    user_id     BIGINT       NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for token lookup (primary query path)
CREATE INDEX idx_refresh_token_token ON refresh_tokens(token, revoked);

-- Index for user-based bulk operations (revoke all, cleanup)
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);

-- Index for cleanup job
CREATE INDEX idx_refresh_token_expiry ON refresh_tokens(expiry_date);
