# Decisions and Assumptions

Tài liệu này bổ sung cho `KTX_BA_SPECIFICATION (1).md`. Nếu có mâu thuẫn, BA gốc vẫn là nguồn ưu tiên cho đến khi quyết định tại đây được chuyển thành `ACCEPTED`.

## Danh sách quyết định

| ID | Trạng thái | Chủ đề | Phương án đề xuất |
|---|---|---|---|
| DEC-001 | ACCEPTED | Role và permission | Mỗi account có đúng một role chính; permission được gán cho role qua RBAC |
| DEC-002 | ACCEPTED | Hồ sơ người ở | Tách thông tin đăng nhập `users` khỏi nghiệp vụ `user_profiles` |
| DEC-003 | ACCEPTED | Account status | `PENDING`, `ACTIVE`, `LOCKED`, `INACTIVE` |
| DEC-004 | ACCEPTED | Tạo account | USER tự đăng ký; chỉ ADMIN có quyền tạo STAFF/ADMIN |
| DEC-005 | ACCEPTED | Facility pricing | Room tham chiếu Room Type; giá được version bằng Price Policy có ngày hiệu lực |
| DEC-006 | ACCEPTED | Room status | `MAINTENANCE/INACTIVE` lưu trực tiếp; `AVAILABLE/FULL` được suy ra từ Bed |
| DEC-007 | ACCEPTED | Assignment concurrency | PostgreSQL transaction + row lock + database constraint; Redis không giữ invariant |
| DEC-008 | ACCEPTED | Điện nước | Meter gắn Room; chi phí được phân bổ theo số ngày cư trú trong billing period |
| DEC-009 | ACCEPTED | Tariff | Đơn giá phẳng theo đơn vị, có `effective_from/effective_to`; chưa hỗ trợ lũy tiến ở MVP |
| DEC-010 | ACCEPTED | Billing | Hóa đơn theo tháng; tiền phòng prorate theo số ngày có Assignment ACTIVE |
| DEC-011 | ACCEPTED | Payment | Thanh toán trực tiếp và VNPay demo; cho partial payment, cấm overpayment |
| DEC-012 | ACCEPTED | Deposit | Contract lưu snapshot deposit; hoàn/khấu trừ khi return và có transaction record |
| DEC-013 | ACCEPTED | Attachment/Room image | MVP lưu local filesystem qua storage abstraction; DB chỉ lưu metadata/path |
| DEC-014 | ACCEPTED | Notification | In-app + WebSocket; email cho reset password, invoice và kết quả quan trọng |
| DEC-015 | ACCEPTED | Thời gian | Database lưu UTC; API ISO-8601; billing/report hiển thị theo `Asia/Bangkok` |
| DEC-016 | ACCEPTED | Tiền tệ | VND, lưu `NUMERIC(19,2)`, làm tròn HALF_UP ở boundary tính tiền |
| DEC-017 | ACCEPTED | API version | REST base path `/api/v1` |
| DEC-018 | ACCEPTED | Kiến trúc | Modular monolith; module giao tiếp qua service/application boundary |
| DEC-019 | ACCEPTED | Room capacity type | MVP có phòng 4, 6 và 8 người |
| DEC-020 | ACCEPTED | Room gender | MVP có phòng `MALE` và `FEMALE` |
| DEC-021 | ACCEPTED | Transfer eligibility | USER phải có Assignment và Contract ACTIVE; STAFF khảo sát thực tế trước khi chuyển |
| DEC-022 | ACCEPTED | Responsive targets | Điện thoại từ 360px, tablet và laptop/desktop; hỗ trợ browser hiện đại |
| DEC-023 | ACCEPTED | Financial history | USER xem được toàn bộ Invoice và Payment history của chính mình |

## Chi tiết đề xuất

### Identity và profile

`users` chỉ chứa dữ liệu xác thực và trạng thái account:

- `id`
- `username`
- `email`
- `password_hash`
- `role_id`
- `status`
- audit/soft-delete fields

`user_profiles` chứa dữ liệu nghiệp vụ:

- `user_id`
- `student_code`
- `full_name`
- `date_of_birth`
- `gender`
- `identity_number`
- `phone`
- `faculty`
- `class_name`
- `address`
- `emergency_contact_name`
- `emergency_contact_phone`

Đề xuất unique: username, email và student code. Identity number chỉ unique khi có giá trị.

### Facility

- `room_types` là danh mục loại phòng, sức chứa mặc định và mô tả.
- `room_price_policies` version giá theo khoảng hiệu lực.
- Room có thể override capacity nhưng không nhỏ hơn số Bed đang active/occupied.
- Bed là đơn vị occupancy thực tế.
- Không cho phép assignment nếu Room hoặc Bed là `MAINTENANCE/INACTIVE`.
- Room Type MVP dùng capacity chuẩn 4, 6 hoặc 8 người.
- Room Gender Type MVP là `MALE` hoặc `FEMALE`.
- Một Room có nhiều `room_images`; một ảnh được chọn làm cover.

### Finance

- Một Meter thuộc một Room và một loại `ELECTRICITY/WATER`.
- Một Room tối đa một Meter active cho mỗi loại tại một thời điểm.
- Consumption của kỳ = current reading − previous reading.
- Chi phí utility của Room được phân bổ theo resident-day trong kỳ:

```text
user_share = room_utility_amount × user_resident_days / total_resident_days
```

- Sai số làm tròn được cộng vào người có số resident-day lớn nhất để tổng detail luôn bằng tổng Room utility.
- Invoice phát hành theo USER; Invoice Detail lưu source/reference và snapshot công thức/đơn giá.
- MVP hỗ trợ thanh toán trực tiếp và VNPay demo; chưa hỗ trợ refund tự động hoặc overpayment.
- USER có trang lịch sử Invoice và Payment, có filter theo kỳ, trạng thái và thời gian.

### Transfer và return

- Transfer Request là entity riêng, không dùng chung hoàn toàn với Incident Request.
- Return Request là entity riêng và lưu debt check, inspection result, deposit settlement.
- Chuyển phòng và trả phòng là transaction nhiều bước, rollback toàn bộ khi lỗi.
- USER chỉ gửi transfer khi có Assignment và Contract ACTIVE.
- STAFF phải khảo sát/kiểm tra thực tế, giới tính phòng, capacity và Bed đích trước khi hoàn tất.

## Kết quả xác nhận

- Dùng đầy đủ RBAC permission ngay từ MVP.
- USER MVP được hiểu là sinh viên/người ở KTX có hồ sơ nghiệp vụ.
- Utility phân bổ theo resident-day.
- MVP dùng giá phẳng, chưa có lũy tiến.
- Cho partial payment và cấm overpayment.
- Tích hợp VNPay ở chế độ demo/sandbox và thanh toán trực tiếp.
- Bản demo lưu ảnh/file local. `StorageService` giữ khả năng chuyển sang MinIO/S3 sau này mà không đổi business service.
- Notification theo thiết kế đã duyệt.

