-- Phase 5: utility meters, invoices and payments

CREATE TABLE utility_tariffs (
    id UUID PRIMARY KEY,
    utility_type VARCHAR(20) NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL CHECK (unit_price >= 0),
    effective_from DATE NOT NULL,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT chk_utility_tariff_type CHECK (utility_type IN ('ELECTRICITY', 'WATER')),
    CONSTRAINT chk_utility_tariff_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE INDEX idx_utility_tariff_lookup
    ON utility_tariffs (utility_type, active, effective_from DESC);

CREATE TABLE utility_meters (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms (id),
    meter_code VARCHAR(50) NOT NULL,
    utility_type VARCHAR(20) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT uq_utility_meter_code UNIQUE (meter_code),
    CONSTRAINT chk_utility_meter_type CHECK (utility_type IN ('ELECTRICITY', 'WATER')),
    CONSTRAINT chk_utility_meter_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX uq_utility_meter_room_type_active
    ON utility_meters (room_id, utility_type)
    WHERE status = 'ACTIVE' AND deleted_at IS NULL;

CREATE TABLE meter_readings (
    id UUID PRIMARY KEY,
    meter_id UUID NOT NULL REFERENCES utility_meters (id),
    billing_period DATE NOT NULL,
    previous_value NUMERIC(15, 3) NOT NULL CHECK (previous_value >= 0),
    current_value NUMERIC(15, 3) NOT NULL CHECK (current_value >= 0),
    consumption NUMERIC(15, 3) NOT NULL CHECK (consumption >= 0),
    reset_recorded BOOLEAN NOT NULL DEFAULT FALSE,
    note TEXT,
    read_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT uq_meter_reading_period UNIQUE (meter_id, billing_period),
    CONSTRAINT chk_meter_reading_period_first_day CHECK (EXTRACT(DAY FROM billing_period) = 1)
);

CREATE INDEX idx_meter_reading_period ON meter_readings (billing_period, meter_id);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    invoice_code VARCHAR(40) NOT NULL,
    user_id UUID NOT NULL REFERENCES users (id),
    contract_id UUID NOT NULL REFERENCES contracts (id),
    billing_period DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    discount NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    fine_amount NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (fine_amount >= 0),
    total_amount NUMERIC(15, 2) NOT NULL CHECK (total_amount >= 0),
    paid_amount NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
    status VARCHAR(30) NOT NULL DEFAULT 'ISSUED',
    issued_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT uq_invoice_code UNIQUE (invoice_code),
    CONSTRAINT uq_invoice_contract_period UNIQUE (contract_id, billing_period),
    CONSTRAINT chk_invoice_period_first_day CHECK (EXTRACT(DAY FROM billing_period) = 1),
    CONSTRAINT chk_invoice_status CHECK (status IN ('ISSUED', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT chk_invoice_amounts CHECK (paid_amount <= total_amount AND discount <= subtotal + fine_amount)
);

CREATE INDEX idx_invoice_user_history ON invoices (user_id, billing_period DESC);
CREATE INDEX idx_invoice_status_due ON invoices (status, due_date);

CREATE TABLE invoice_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    item_type VARCHAR(20) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity NUMERIC(15, 3) NOT NULL DEFAULT 1 CHECK (quantity >= 0),
    unit_price NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (unit_price >= 0),
    amount NUMERIC(15, 2) NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT chk_invoice_item_type CHECK (item_type IN ('ROOM', 'ELECTRICITY', 'WATER', 'SERVICE', 'FINE'))
);

CREATE INDEX idx_invoice_item_invoice ON invoice_items (invoice_id);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    payment_code VARCHAR(40) NOT NULL,
    invoice_id UUID NOT NULL REFERENCES invoices (id),
    user_id UUID NOT NULL REFERENCES users (id),
    amount NUMERIC(15, 2) NOT NULL CHECK (amount > 0),
    method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    external_reference VARCHAR(100),
    paid_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    CONSTRAINT uq_payment_code UNIQUE (payment_code),
    CONSTRAINT uq_payment_idempotency UNIQUE (idempotency_key),
    CONSTRAINT chk_payment_method CHECK (method IN ('CASH', 'VNPAY')),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_payment_invoice ON payments (invoice_id, created_at DESC);
CREATE INDEX idx_payment_user_history ON payments (user_id, created_at DESC);

