-- Phase 3: Registration and room/bed assignment

CREATE TABLE registrations
(
    id                    UUID PRIMARY KEY,
    user_id               UUID         NOT NULL REFERENCES users (id),
    requested_room_type   VARCHAR(20)  NOT NULL,
    requested_gender_type VARCHAR(20)  NOT NULL,
    preferred_start_date  DATE         NOT NULL,
    preferred_end_date    DATE,
    reason                TEXT,
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    rejection_reason      TEXT,
    reviewed_by           UUID REFERENCES users (id),
    reviewed_at           TIMESTAMP,
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    created_by            VARCHAR(100),
    updated_by            VARCHAR(100),
    deleted_at            TIMESTAMP,
    deleted_by            VARCHAR(100),
    CONSTRAINT chk_registration_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT chk_registration_room_type
        CHECK (requested_room_type IN ('STANDARD_8', 'STANDARD_6', 'PREMIUM_4')),
    CONSTRAINT chk_registration_gender
        CHECK (requested_gender_type IN ('MALE', 'FEMALE', 'MIXED')),
    CONSTRAINT chk_registration_dates
        CHECK (preferred_end_date IS NULL OR preferred_end_date > preferred_start_date),
    CONSTRAINT chk_registration_rejection
        CHECK (status <> 'REJECTED' OR rejection_reason IS NOT NULL)
);

CREATE UNIQUE INDEX uq_registration_user_pending
    ON registrations (user_id)
    WHERE status = 'PENDING' AND deleted_at IS NULL;

CREATE INDEX idx_registration_status_created
    ON registrations (status, created_at DESC);

CREATE TABLE room_assignments
(
    id              UUID PRIMARY KEY,
    registration_id UUID         NOT NULL REFERENCES registrations (id),
    user_id         UUID         NOT NULL REFERENCES users (id),
    bed_id          UUID         NOT NULL REFERENCES beds (id),
    start_date      DATE         NOT NULL,
    end_date        DATE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    assigned_by     UUID         NOT NULL REFERENCES users (id),
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(100),
    CONSTRAINT chk_assignment_status CHECK (status IN ('ACTIVE', 'ENDED', 'CANCELLED')),
    CONSTRAINT chk_assignment_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE UNIQUE INDEX uq_assignment_registration
    ON room_assignments (registration_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_assignment_active_user
    ON room_assignments (user_id)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

CREATE UNIQUE INDEX uq_assignment_active_bed
    ON room_assignments (bed_id)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

CREATE INDEX idx_assignment_user_history
    ON room_assignments (user_id, created_at DESC);

