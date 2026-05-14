-- ============================================================
-- V1__init_schema.sql
-- HireTrack API — Initial Database Schema
-- ============================================================
-- This migration creates the baseline schema that was previously
-- managed by Hibernate ddl-auto=update. From this point forward,
-- ALL schema changes go through versioned Flyway migrations.
-- ============================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Job applications table
CREATE TABLE IF NOT EXISTS job_applications (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    company          VARCHAR(255) NOT NULL,
    role             VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'APPLIED',
    notes            TEXT,
    applied_date     DATE         NOT NULL,
    user_id          BIGINT       NOT NULL,
    resume_file_name VARCHAR(255),
    resume_file_path VARCHAR(500),
    priority         VARCHAR(10)  DEFAULT 'MEDIUM',

    PRIMARY KEY (id),
    CONSTRAINT fk_job_app_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Performance Indexes ─────────────────────────────────
-- Index on user_id: every query filters by user (multi-tenant pattern)
CREATE INDEX idx_job_app_user_id ON job_applications(user_id);

-- Composite index for filtering by user + status (most common query)
CREATE INDEX idx_job_app_user_status ON job_applications(user_id, status);

-- Index for company search (LIKE queries)
CREATE INDEX idx_job_app_user_company ON job_applications(user_id, company);

-- Index for sorting by applied_date
CREATE INDEX idx_job_app_user_date ON job_applications(user_id, applied_date DESC);
