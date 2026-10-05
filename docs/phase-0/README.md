# Phase 0 — BA and Architecture Baseline

Trạng thái: **COMPLETED**  
Ngày bắt đầu: **2026-10-01**
Ngày hoàn thành: **2026-10-01**

## Mục tiêu

Phase 0 chuyển BA baseline thành bộ tài liệu đủ rõ để triển khai mà không phải tự suy diễn nghiệp vụ trong lúc code.

## Tài liệu

1. `01_decisions_and_assumptions.md` — quyết định và giả định đề xuất.
2. `02_rbac_permission_matrix.md` — role, permission và ownership.
3. `03_state_transitions.md` — state machine của các entity nghiệp vụ.
4. `04_erd_and_data_rules.md` — ERD logic, constraint và concurrency.
5. `05_api_conventions.md` — contract REST API.
6. `06_frontend_conventions.md` — Vue, custom components, GSAP và Three.js.
7. `07_acceptance_criteria_p0.md` — acceptance criteria cho các luồng P0.

## Quy ước trạng thái quyết định

- `ACCEPTED`: đã được người có thẩm quyền xác nhận.
- `PROPOSED`: phương án khuyến nghị, chưa được xác nhận.
- `REJECTED`: không sử dụng.
- `SUPERSEDED`: đã được thay bằng quyết định mới.

Không triển khai migration/domain model phụ thuộc một quyết định `PROPOSED` nếu việc thay đổi sau đó gây mất dữ liệu hoặc phải rewrite nghiệp vụ lớn.

## Điều kiện hoàn thành Phase 0

- [x] Tất cả quyết định P0 chuyển sang `ACCEPTED`.
- [x] Permission matrix được duyệt.
- [x] State transition được duyệt.
- [x] ERD và data integrity rules được duyệt.
- [x] API và frontend conventions được duyệt.
- [x] Acceptance criteria P0 không còn điểm mơ hồ cản Phase 1.

## Các xác nhận nghiệp vụ bổ sung

- Room Type MVP: phòng 4 người, 6 người và 8 người.
- Gender Type MVP: phòng nam và phòng nữ.
- Sinh viên có Assignment/Contract ACTIVE được gửi Room Transfer Request.
- STAFF chỉ hoàn tất chuyển phòng khi khảo sát thực tế và Bed đích phù hợp/còn trống.
- Payment MVP: thanh toán trực tiếp và VNPay demo.
- File/ảnh trong bản demo lưu ở local filesystem qua abstraction storage service.
- Room có một hoặc nhiều hình ảnh.
- Frontend responsive cho điện thoại, máy tính bảng và laptop.
- Sinh viên xem được toàn bộ lịch sử Invoice và Payment của chính mình.

