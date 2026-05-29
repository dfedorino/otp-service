CREATE TABLE IF NOT EXISTS users
(
    id           BIGSERIAL PRIMARY KEY,
    login        VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password     VARCHAR(255) NOT NULL,
    "role"       VARCHAR(5)   NOT NULL -- IN ('ADMIN', 'USER')
);

INSERT INTO users (login, phone_number, password, "role")
VALUES ('admin', '+79111234567', '$2a$04$i0o7w7IQsZQuuGL..Z6G9uGbG.PzrwKUAMkfA8pxs355ZgwQpfNIi', 'ADMIN');

CREATE TABLE IF NOT EXISTS otp_config
(
    id          BIGSERIAL PRIMARY KEY,
    code_length INT NOT NULL DEFAULT 6,
    ttl_seconds INT NOT NULL DEFAULT 300
);

INSERT INTO otp_config (code_length, ttl_seconds) VALUES (6, 10);

CREATE TABLE IF NOT EXISTS otp_codes
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    operation_id VARCHAR(255) NOT NULL,
    code         VARCHAR(20)  NOT NULL,
    status       VARCHAR(7)   NOT NULL, -- IN ('ACTIVE', 'EXPIRED', 'USED'),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_otp_codes_user_id_operation_id ON otp_codes(user_id, operation_id, code);
CREATE INDEX IF NOT EXISTS idx_otp_codes_expires_status ON otp_codes(expires_at, status);