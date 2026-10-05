-- ============================================================
-- V5: Student Registry & User Linking
-- ============================================================

CREATE TABLE student_registry
(
    id           UUID PRIMARY KEY,
    student_code VARCHAR(50)  NOT NULL,
    full_name    VARCHAR(150) NOT NULL,
    email        VARCHAR(255) NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    class_name   VARCHAR(100) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100),
    CONSTRAINT chk_student_registry_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX uq_student_registry_student_code
    ON student_registry (student_code)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_student_registry_email
    ON student_registry (email);

CREATE INDEX idx_student_registry_status
    ON student_registry (status);

-- Link student_registry to users (1 Student Registry -> 0..1 User)
ALTER TABLE users ADD COLUMN student_registry_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_student_registry
    FOREIGN KEY (student_registry_id) REFERENCES student_registry (id);

CREATE UNIQUE INDEX uq_users_student_registry_id
    ON users (student_registry_id)
    WHERE student_registry_id IS NOT NULL AND deleted_at IS NULL;
