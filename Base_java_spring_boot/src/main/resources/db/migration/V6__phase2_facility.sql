-- ============================================================
-- V6: Phase 2 — Facility Management
-- Building → Floor → Room → Bed + Price Policy
-- ============================================================

-- ── PRICE POLICY (tham chiếu bởi Room) ──
CREATE TABLE price_policies
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(200)    NOT NULL,
    room_type    VARCHAR(20)     NOT NULL,  -- STANDARD_8, STANDARD_6, PREMIUM_4
    price_per_month NUMERIC(15, 2) NOT NULL CHECK (price_per_month > 0),
    effective_from DATE           NOT NULL,
    effective_to   DATE,
    is_active    BOOLEAN         NOT NULL DEFAULT TRUE,
    description  TEXT,
    created_at   TIMESTAMP,
    updated_at   TIMESTAMP,
    created_by   VARCHAR(100),
    updated_by   VARCHAR(100),
    deleted_at   TIMESTAMP,
    deleted_by   VARCHAR(100),
    CONSTRAINT chk_price_policies_room_type CHECK (room_type IN ('STANDARD_8', 'STANDARD_6', 'PREMIUM_4')),
    CONSTRAINT chk_price_policies_dates CHECK (effective_to IS NULL OR effective_to > effective_from)
);

CREATE INDEX idx_price_policies_room_type ON price_policies (room_type);
CREATE INDEX idx_price_policies_active ON price_policies (is_active, effective_from);

-- ── BUILDINGS ──
CREATE TABLE buildings
(
    id          UUID PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL,
    name        VARCHAR(200) NOT NULL,
    address     VARCHAR(500),
    gender_type VARCHAR(20)  NOT NULL,  -- MALE, FEMALE, MIXED
    total_floors INT         NOT NULL DEFAULT 0,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100),
    CONSTRAINT chk_buildings_gender CHECK (gender_type IN ('MALE', 'FEMALE', 'MIXED')),
    CONSTRAINT chk_buildings_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE'))
);

CREATE UNIQUE INDEX uq_buildings_code
    ON buildings (code) WHERE deleted_at IS NULL;

CREATE INDEX idx_buildings_status ON buildings (status);

-- ── FLOORS ──
CREATE TABLE floors
(
    id          UUID PRIMARY KEY,
    building_id UUID         NOT NULL REFERENCES buildings (id),
    floor_number INT         NOT NULL,
    name        VARCHAR(100),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100),
    CONSTRAINT chk_floors_status CHECK (status IN ('ACTIVE', 'MAINTENANCE', 'INACTIVE'))
);

CREATE UNIQUE INDEX uq_floors_building_number
    ON floors (building_id, floor_number) WHERE deleted_at IS NULL;

CREATE INDEX idx_floors_building_id ON floors (building_id);

-- ── ROOMS ──
CREATE TABLE rooms
(
    id              UUID PRIMARY KEY,
    floor_id        UUID          NOT NULL REFERENCES floors (id),
    building_id     UUID          NOT NULL REFERENCES buildings (id),
    room_number     VARCHAR(20)   NOT NULL,
    room_type       VARCHAR(20)   NOT NULL,  -- STANDARD_8, STANDARD_6, PREMIUM_4
    capacity        INT           NOT NULL CHECK (capacity > 0),
    gender_type     VARCHAR(20)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE',
    current_occupancy INT         NOT NULL DEFAULT 0 CHECK (current_occupancy >= 0),
    area_sqm        NUMERIC(6, 2),
    price_policy_id UUID          REFERENCES price_policies (id),
    description     TEXT,
    notes           TEXT,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_at      TIMESTAMP,
    deleted_by      VARCHAR(100),
    CONSTRAINT chk_rooms_type     CHECK (room_type IN ('STANDARD_8', 'STANDARD_6', 'PREMIUM_4')),
    CONSTRAINT chk_rooms_gender   CHECK (gender_type IN ('MALE', 'FEMALE', 'MIXED')),
    CONSTRAINT chk_rooms_status   CHECK (status IN ('AVAILABLE', 'FULL', 'MAINTENANCE', 'INACTIVE')),
    CONSTRAINT chk_rooms_occupancy CHECK (current_occupancy <= capacity)
);

CREATE UNIQUE INDEX uq_rooms_floor_number
    ON rooms (floor_id, room_number) WHERE deleted_at IS NULL;

CREATE INDEX idx_rooms_building_id  ON rooms (building_id);
CREATE INDEX idx_rooms_floor_id     ON rooms (floor_id);
CREATE INDEX idx_rooms_status       ON rooms (status);
CREATE INDEX idx_rooms_gender_type  ON rooms (gender_type);
CREATE INDEX idx_rooms_room_type    ON rooms (room_type);

-- ── BEDS ──
CREATE TABLE beds
(
    id          UUID PRIMARY KEY,
    room_id     UUID          NOT NULL REFERENCES rooms (id),
    bed_number  VARCHAR(10)   NOT NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE',
    notes       TEXT,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(100),
    updated_by  VARCHAR(100),
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(100),
    CONSTRAINT chk_beds_status CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'INACTIVE'))
);

CREATE UNIQUE INDEX uq_beds_room_number
    ON beds (room_id, bed_number) WHERE deleted_at IS NULL;

CREATE INDEX idx_beds_room_id ON beds (room_id);
CREATE INDEX idx_beds_status  ON beds (status);

-- ── SEED DATA: Tòa nhà mẫu ──
INSERT INTO price_policies (id, name, room_type, price_per_month, effective_from, is_active, created_at, updated_at, created_by, updated_by)
VALUES
    (gen_random_uuid(), 'Giá phòng 8 người - 2026', 'STANDARD_8', 500000, '2026-01-01', true, now(), now(), 'system', 'system'),
    (gen_random_uuid(), 'Giá phòng 6 người - 2026', 'STANDARD_6', 700000, '2026-01-01', true, now(), now(), 'system', 'system'),
    (gen_random_uuid(), 'Giá phòng 4 người CLC - 2026', 'PREMIUM_4', 900000, '2026-01-01', true, now(), now(), 'system', 'system');

INSERT INTO buildings (id, code, name, address, gender_type, total_floors, status, description, created_at, updated_at, created_by, updated_by)
VALUES
    (gen_random_uuid(), 'A', 'Tòa A - Nam sinh viên', '144 Xuân Thủy, Cầu Giấy, Hà Nội', 'MALE', 10, 'ACTIVE', 'Khu nội trú dành riêng cho nam sinh viên', now(), now(), 'system', 'system'),
    (gen_random_uuid(), 'B', 'Tòa B - Nữ sinh viên', '144 Xuân Thủy, Cầu Giấy, Hà Nội', 'FEMALE', 10, 'ACTIVE', 'Khu nội trú dành riêng cho nữ sinh viên', now(), now(), 'system', 'system'),
    (gen_random_uuid(), 'C', 'Tòa C - Chất lượng cao', '144 Xuân Thủy, Cầu Giấy, Hà Nội', 'MIXED', 8, 'ACTIVE', 'Khu nội trú chất lượng cao dành cho cả nam và nữ', now(), now(), 'system', 'system');
