-- Phase 4: Dormitory contracts

CREATE TABLE contracts
(
    id             UUID PRIMARY KEY,
    contract_code  VARCHAR(40)    NOT NULL,
    user_id        UUID           NOT NULL REFERENCES users (id),
    assignment_id  UUID           NOT NULL REFERENCES room_assignments (id),
    start_date     DATE           NOT NULL,
    end_date       DATE           NOT NULL,
    rental_price   NUMERIC(15, 2) NOT NULL CHECK (rental_price >= 0),
    deposit        NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (deposit >= 0),
    status         VARCHAR(20)    NOT NULL DEFAULT 'DRAFT',
    terminated_at  TIMESTAMP,
    terminated_by  UUID REFERENCES users (id),
    termination_reason TEXT,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     VARCHAR(100),
    updated_by     VARCHAR(100),
    deleted_at     TIMESTAMP,
    deleted_by     VARCHAR(100),
    CONSTRAINT uq_contract_code UNIQUE (contract_code),
    CONSTRAINT chk_contract_status CHECK (status IN ('DRAFT', 'ACTIVE', 'EXPIRED', 'TERMINATED')),
    CONSTRAINT chk_contract_dates CHECK (end_date > start_date),
    CONSTRAINT chk_contract_termination
        CHECK (status <> 'TERMINATED' OR terminated_at IS NOT NULL)
);

CREATE UNIQUE INDEX uq_contract_active_user
    ON contracts (user_id)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

CREATE INDEX idx_contract_user_history
    ON contracts (user_id, created_at DESC);

CREATE INDEX idx_contract_status_end_date
    ON contracts (status, end_date);

