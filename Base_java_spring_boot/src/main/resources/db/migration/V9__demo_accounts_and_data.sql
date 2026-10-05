-- Demo data for local development and UAT.
-- All demo accounts use password: 123456

-- Student registry: one linked demo account and two records for registration/import testing.
INSERT INTO student_registry (
    id, student_code, full_name, email, phone, class_name, status,
    created_at, updated_at, created_by, updated_by
)
VALUES
    ('00000000-0000-0000-0000-000000000201', 'SVDEMO001', 'Nguyễn Văn Sinh', 'student@ktx.local', '0901000001', 'CNTT01', 'ACTIVE', now(), now(), 'system', 'system'),
    ('00000000-0000-0000-0000-000000000202', 'SVDEMO002', 'Trần Thị Minh', 'student2@ktx.local', '0901000002', 'CNTT02', 'ACTIVE', now(), now(), 'system', 'system'),
    ('00000000-0000-0000-0000-000000000203', 'SVDEMO003', 'Lê Minh Anh', 'student3@ktx.local', '0901000003', 'KTPM01', 'INACTIVE', now(), now(), 'system', 'system')
ON CONFLICT DO NOTHING;

-- Reuse the BCrypt hash already used by the development admin account for password 123456.
INSERT INTO users (
    id, username, email, password, role, status, student_registry_id,
    created_at, updated_at, created_by, updated_by
)
SELECT
    '00000000-0000-0000-0000-000000000101', 'staff', 'staff@ktx.local',
    '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC',
    'STAFF', 'ACTIVE', NULL, now(), now(), 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'staff' OR email = 'staff@ktx.local');

INSERT INTO users (
    id, username, email, password, role, status, student_registry_id,
    created_at, updated_at, created_by, updated_by
)
SELECT
    '00000000-0000-0000-0000-000000000102', 'svdemo001', 'student@ktx.local',
    '$2a$10$SLFWIgDoFq49uquyFJDoReLBUcVmElHLISAfXp/TgY3EwN07VCRsC',
    'USER', 'ACTIVE', registry.id, now(), now(), 'system', 'system'
FROM student_registry registry
WHERE registry.student_code = 'SVDEMO001' AND registry.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM users WHERE username = 'svdemo001' OR email = 'student@ktx.local');

INSERT INTO user_profiles (
    user_id, full_name, phone, faculty, class_name,
    created_at, updated_at, created_by, updated_by
)
SELECT user_account.id, 'Nguyễn Minh Nhân Viên', '0909000001', 'Ban quản lý KTX', 'STAFF',
       now(), now(), 'system', 'system'
FROM users user_account
WHERE user_account.username = 'staff'
  AND NOT EXISTS (SELECT 1 FROM user_profiles profile WHERE profile.user_id = user_account.id);

INSERT INTO user_profiles (
    user_id, full_name, student_code, phone, faculty, class_name,
    created_at, updated_at, created_by, updated_by
)
SELECT user_account.id, registry.full_name, registry.student_code, registry.phone,
       'Công nghệ thông tin', registry.class_name, now(), now(), 'system', 'system'
FROM users user_account
JOIN student_registry registry ON registry.id = user_account.student_registry_id
WHERE user_account.username = 'svdemo001'
  AND NOT EXISTS (SELECT 1 FROM user_profiles profile WHERE profile.user_id = user_account.id);

-- Minimal facility data so registration, assignment and contract flows can be tested immediately.
INSERT INTO floors (id, building_id, floor_number, name, status, created_at, updated_at, created_by, updated_by)
SELECT '00000000-0000-0000-0000-000000000301', building.id, 1, 'Tầng 1', 'ACTIVE', now(), now(), 'system', 'system'
FROM buildings building
WHERE building.code = 'A' AND building.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM floors floor WHERE floor.building_id = building.id AND floor.floor_number = 1 AND floor.deleted_at IS NULL);

INSERT INTO floors (id, building_id, floor_number, name, status, created_at, updated_at, created_by, updated_by)
SELECT '00000000-0000-0000-0000-000000000302', building.id, 1, 'Tầng 1', 'ACTIVE', now(), now(), 'system', 'system'
FROM buildings building
WHERE building.code = 'B' AND building.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM floors floor WHERE floor.building_id = building.id AND floor.floor_number = 1 AND floor.deleted_at IS NULL);

INSERT INTO floors (id, building_id, floor_number, name, status, created_at, updated_at, created_by, updated_by)
SELECT '00000000-0000-0000-0000-000000000303', building.id, 1, 'Tầng 1', 'ACTIVE', now(), now(), 'system', 'system'
FROM buildings building
WHERE building.code = 'C' AND building.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM floors floor WHERE floor.building_id = building.id AND floor.floor_number = 1 AND floor.deleted_at IS NULL);

INSERT INTO rooms (
    id, floor_id, building_id, room_number, room_type, capacity, gender_type,
    status, current_occupancy, area_sqm, price_policy_id, description,
    created_at, updated_at, created_by, updated_by
)
SELECT '00000000-0000-0000-0000-000000000401', floor.id, building.id, 'A101', 'STANDARD_8', 8, 'MALE',
       'AVAILABLE', 0, 36, policy.id, 'Phòng demo tiêu chuẩn dành cho nam sinh viên',
       now(), now(), 'system', 'system'
FROM buildings building
JOIN floors floor ON floor.building_id = building.id AND floor.floor_number = 1 AND floor.deleted_at IS NULL
JOIN price_policies policy ON policy.room_type = 'STANDARD_8' AND policy.is_active = true AND policy.deleted_at IS NULL
WHERE building.code = 'A' AND building.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM rooms room WHERE room.floor_id = floor.id AND room.room_number = 'A101' AND room.deleted_at IS NULL)
ORDER BY policy.effective_from DESC LIMIT 1;

INSERT INTO rooms (
    id, floor_id, building_id, room_number, room_type, capacity, gender_type,
    status, current_occupancy, area_sqm, price_policy_id, description,
    created_at, updated_at, created_by, updated_by
)
SELECT '00000000-0000-0000-0000-000000000402', floor.id, building.id, 'B101', 'STANDARD_8', 8, 'FEMALE',
       'AVAILABLE', 0, 36, policy.id, 'Phòng demo tiêu chuẩn dành cho nữ sinh viên',
       now(), now(), 'system', 'system'
FROM buildings building
JOIN floors floor ON floor.building_id = building.id AND floor.floor_number = 1 AND floor.deleted_at IS NULL
JOIN price_policies policy ON policy.room_type = 'STANDARD_8' AND policy.is_active = true AND policy.deleted_at IS NULL
WHERE building.code = 'B' AND building.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM rooms room WHERE room.floor_id = floor.id AND room.room_number = 'B101' AND room.deleted_at IS NULL)
ORDER BY policy.effective_from DESC LIMIT 1;

INSERT INTO rooms (
    id, floor_id, building_id, room_number, room_type, capacity, gender_type,
    status, current_occupancy, area_sqm, price_policy_id, description,
    created_at, updated_at, created_by, updated_by
)
SELECT '00000000-0000-0000-0000-000000000403', floor.id, building.id, 'C101', 'PREMIUM_4', 4, 'MIXED',
       'AVAILABLE', 0, 32, policy.id, 'Phòng demo chất lượng cao',
       now(), now(), 'system', 'system'
FROM buildings building
JOIN floors floor ON floor.building_id = building.id AND floor.floor_number = 1 AND floor.deleted_at IS NULL
JOIN price_policies policy ON policy.room_type = 'PREMIUM_4' AND policy.is_active = true AND policy.deleted_at IS NULL
WHERE building.code = 'C' AND building.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM rooms room WHERE room.floor_id = floor.id AND room.room_number = 'C101' AND room.deleted_at IS NULL)
ORDER BY policy.effective_from DESC LIMIT 1;

INSERT INTO beds (id, room_id, bed_number, status, created_at, updated_at, created_by, updated_by)
SELECT gen_random_uuid(), room.id, lpad(series.value::text, 2, '0'), 'AVAILABLE', now(), now(), 'system', 'system'
FROM rooms room
CROSS JOIN generate_series(1, room.capacity) AS series(value)
WHERE room.room_number IN ('A101', 'B101', 'C101') AND room.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM beds bed
      WHERE bed.room_id = room.id AND bed.bed_number = lpad(series.value::text, 2, '0') AND bed.deleted_at IS NULL
  );
