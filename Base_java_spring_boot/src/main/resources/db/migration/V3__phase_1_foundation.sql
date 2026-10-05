ALTER TABLE users ADD COLUMN status VARCHAR(20);
UPDATE users SET status = CASE WHEN enabled THEN 'ACTIVE' ELSE 'LOCKED' END;
ALTER TABLE users ALTER COLUMN status SET NOT NULL;
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'ACTIVE';
ALTER TABLE users DROP COLUMN enabled;

ALTER TABLE users ADD CONSTRAINT chk_users_role
    CHECK (role IN ('ADMIN', 'STAFF', 'USER'));
ALTER TABLE users ADD CONSTRAINT chk_users_status
    CHECK (status IN ('PENDING', 'ACTIVE', 'LOCKED', 'INACTIVE'));

CREATE TABLE user_profiles
(
    user_id                 UUID PRIMARY KEY REFERENCES users (id),
    full_name               VARCHAR(150) NOT NULL,
    student_code            VARCHAR(50) UNIQUE,
    date_of_birth           DATE,
    gender                  VARCHAR(20),
    phone                   VARCHAR(20),
    identity_number         VARCHAR(50),
    faculty                 VARCHAR(150),
    class_name              VARCHAR(100),
    address                 VARCHAR(500),
    emergency_contact_name  VARCHAR(150),
    emergency_contact_phone VARCHAR(20),
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100),
    deleted_at              TIMESTAMP,
    deleted_by              VARCHAR(100)
);

CREATE UNIQUE INDEX uq_user_profiles_identity_number
    ON user_profiles (identity_number)
    WHERE identity_number IS NOT NULL AND deleted_at IS NULL;

CREATE TABLE password_reset_tokens
(
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users (id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at    TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_tokens_expires_at
    ON password_reset_tokens (expires_at);

ALTER TABLE token_sessions
    ADD CONSTRAINT fk_token_sessions_user
    FOREIGN KEY (user_id) REFERENCES users (id);
CREATE UNIQUE INDEX uq_token_sessions_refresh_token
    ON token_sessions (refresh_token);
CREATE INDEX idx_token_sessions_user_id
    ON token_sessions (user_id);

ALTER TABLE audit_logs ADD COLUMN entity_type VARCHAR(100);
ALTER TABLE audit_logs ADD COLUMN entity_id UUID;
ALTER TABLE audit_logs ADD COLUMN old_value TEXT;
ALTER TABLE audit_logs ADD COLUMN new_value TEXT;
ALTER TABLE audit_logs ADD COLUMN ip_address VARCHAR(64);
ALTER TABLE audit_logs ADD COLUMN user_agent VARCHAR(500);

CREATE INDEX idx_audit_logs_user_id_created_at
    ON audit_logs (user_id, created_at DESC);
CREATE INDEX idx_audit_logs_entity
    ON audit_logs (entity_type, entity_id);

INSERT INTO user_profiles (
    user_id, full_name, created_at, updated_at, created_by, updated_by
)
SELECT id, username, now(), now(), 'system', 'system'
FROM users
WHERE NOT EXISTS (
    SELECT 1 FROM user_profiles profile WHERE profile.user_id = users.id
);
