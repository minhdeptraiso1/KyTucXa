-- Phase 5 demo data for local development/UAT.

INSERT INTO utility_tariffs (
    id, utility_type, unit_price, effective_from, active,
    created_at, updated_at, created_by, updated_by
)
VALUES
    ('00000000-0000-0000-0000-000000000501', 'ELECTRICITY', 3500, '2026-01-01', TRUE, now(), now(), 'system', 'system'),
    ('00000000-0000-0000-0000-000000000502', 'WATER', 15000, '2026-01-01', TRUE, now(), now(), 'system', 'system')
ON CONFLICT DO NOTHING;

INSERT INTO utility_meters (
    id, room_id, meter_code, utility_type, unit, status,
    created_at, updated_at, created_by, updated_by
)
SELECT '00000000-0000-0000-0000-000000000511', room.id, 'DIEN-A101', 'ELECTRICITY', 'kWh', 'ACTIVE',
       now(), now(), 'system', 'system'
FROM rooms room
WHERE room.room_number = 'A101' AND room.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM utility_meters meter WHERE meter.meter_code = 'DIEN-A101');

INSERT INTO utility_meters (
    id, room_id, meter_code, utility_type, unit, status,
    created_at, updated_at, created_by, updated_by
)
SELECT '00000000-0000-0000-0000-000000000512', room.id, 'NUOC-A101', 'WATER', 'm3', 'ACTIVE',
       now(), now(), 'system', 'system'
FROM rooms room
WHERE room.room_number = 'A101' AND room.deleted_at IS NULL
  AND NOT EXISTS (SELECT 1 FROM utility_meters meter WHERE meter.meter_code = 'NUOC-A101');

INSERT INTO meter_readings (
    id, meter_id, billing_period, previous_value, current_value, consumption,
    reset_recorded, note, read_at, created_at, updated_at, created_by, updated_by
)
SELECT '00000000-0000-0000-0000-000000000521', meter.id, '2026-10-01', 1200, 1328, 128,
       FALSE, 'Chỉ số điện demo tháng 10/2026', now(), now(), now(), 'system', 'system'
FROM utility_meters meter
WHERE meter.meter_code = 'DIEN-A101'
  AND NOT EXISTS (SELECT 1 FROM meter_readings reading WHERE reading.meter_id = meter.id AND reading.billing_period = '2026-10-01');

INSERT INTO meter_readings (
    id, meter_id, billing_period, previous_value, current_value, consumption,
    reset_recorded, note, read_at, created_at, updated_at, created_by, updated_by
)
SELECT '00000000-0000-0000-0000-000000000522', meter.id, '2026-10-01', 340, 356, 16,
       FALSE, 'Chỉ số nước demo tháng 10/2026', now(), now(), now(), 'system', 'system'
FROM utility_meters meter
WHERE meter.meter_code = 'NUOC-A101'
  AND NOT EXISTS (SELECT 1 FROM meter_readings reading WHERE reading.meter_id = meter.id AND reading.billing_period = '2026-10-01');

