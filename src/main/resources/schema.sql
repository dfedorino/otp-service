CREATE TYPE user_role AS ENUM ('ADMIN', 'USER');

CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    login    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- BCrypt hash
    role     user_role    NOT NULL
);

CREATE TABLE otp_config
(
    id          BIGSERIAL PRIMARY KEY,
    code_length INT NOT NULL DEFAULT 6,
    ttl_seconds INT NOT NULL DEFAULT 300
);

INSERT INTO otp_config (code_length, ttl_seconds) VALUES (6, 300);

CREATE TYPE otp_status AS ENUM ('ACTIVE', 'EXPIRED', 'USED');

CREATE TABLE otp_codes
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    operation_id VARCHAR(255) NOT NULL,
    code         VARCHAR(20)  NOT NULL,
    status       otp_status   NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_otp_codes_user_id ON otp_codes(user_id);
CREATE INDEX IF NOT EXISTS idx_otp_codes_expires ON otp_codes(expires_at);