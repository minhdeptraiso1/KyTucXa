# P0 Acceptance Criteria

Trạng thái: **ACCEPTED**

Các tiêu chí này mô tả kết quả nghiệp vụ, không phụ thuộc chi tiết UI/API cuối cùng.

## 1. Authentication và account

### AC-AUTH-01 — Account bị khóa

- Given account ở trạng thái `LOCKED`.
- When người dùng login hoặc refresh token.
- Then hệ thống từ chối và không phát hành token mới.

### AC-AUTH-02 — Role và permission

- Given account có một role chính.
- When truy cập API.
- Then backend kiểm tra permission tương ứng.
- And ADMIN không có quyền vận hành STAFF nếu permission đó không được gán.

### AC-AUTH-03 — Ownership

- Given USER A đã đăng nhập.
- When USER A yêu cầu profile/contract/invoice/request của USER B.
- Then hệ thống trả `403` hoặc `404` theo security convention.
- And không làm lộ dữ liệu của USER B.

## 2. Facility

### AC-FAC-01 — Room/Bed không usable

- Given Room hoặc Bed là `MAINTENANCE/INACTIVE`.
- When STAFF cố assign USER.
- Then hệ thống từ chối.

### AC-FAC-02 — Capacity consistency

- Given Room có Bed active/occupied.
- When STAFF giảm capacity hoặc vô hiệu hóa Bed.
- Then hệ thống từ chối nếu thao tác làm dữ liệu mâu thuẫn.

### AC-FAC-03 — Hình ảnh phòng

- Given STAFF có permission quản lý Facility.
- When STAFF tải ảnh hợp lệ cho Room.
- Then hệ thống lưu file qua Storage Service và lưu metadata trong database.
- And USER xem được gallery/cover image của Room.
- And file không hợp lệ về loại hoặc dung lượng bị từ chối.

## 3. Registration và assignment

### AC-REG-01 — Registration duy nhất

- Given USER có Registration active hoặc Assignment ACTIVE theo rule đã duyệt.
- When USER submit Registration mới.
- Then hệ thống từ chối với business error rõ ràng.

### AC-REG-02 — Reject có lý do

- Given Registration `PENDING`.
- When STAFF reject mà không có reason.
- Then hệ thống từ chối validation.

### AC-ASG-01 — Một Bed, một USER

- Given một Bed AVAILABLE.
- When hai STAFF đồng thời assign hai USER khác nhau vào Bed đó.
- Then chỉ một transaction thành công.
- And transaction còn lại nhận conflict.
- And dữ liệu Bed/Assignment vẫn nhất quán.

### AC-ASG-02 — Một USER, một Assignment

- Given USER đã có Assignment ACTIVE.
- When STAFF tạo Assignment ACTIVE khác.
- Then hệ thống từ chối.

## 4. Contract

### AC-CON-01 — Contract cần Assignment

- Given USER không có Assignment hợp lệ.
- When STAFF tạo/kích hoạt Contract.
- Then hệ thống từ chối.

### AC-CON-02 — Một Contract ACTIVE

- Given USER có Contract ACTIVE.
- When STAFF kích hoạt Contract thứ hai.
- Then hệ thống từ chối conflict.

## 5. Invoice và payment

### AC-INV-01 — Total invariant

- Given Invoice có nhiều Invoice Detail.
- When Invoice được phát hành.
- Then subtotal/total bằng công thức đã duyệt.
- And không có amount âm.

### AC-INV-02 — Paid immutable

- Given Invoice `PAID`.
- When STAFF sửa trực tiếp detail hoặc total.
- Then hệ thống từ chối.

### AC-PAY-01 — Ownership

- Given Invoice thuộc USER B.
- When USER A cố thanh toán.
- Then hệ thống từ chối.

### AC-PAY-02 — Idempotency

- Given hai request Payment có cùng idempotency key và payload.
- When hệ thống nhận cả hai request.
- Then chỉ một Payment nghiệp vụ được tạo/áp dụng.

### AC-PAY-03 — Concurrent payment

- Given Invoice còn số tiền X.
- When nhiều Payment đồng thời có tổng lớn hơn X.
- Then tổng Payment SUCCESS không vượt X.

### AC-PAY-04 — Thanh toán trực tiếp và VNPay demo

- Given Invoice còn số tiền phải trả.
- When STAFF xác nhận thanh toán trực tiếp hoặc USER hoàn tất VNPay demo callback hợp lệ.
- Then Payment và Invoice được cập nhật trong một transaction.
- And callback lặp lại không tạo thanh toán trùng.

### AC-PAY-05 — Lịch sử tài chính

- Given USER đã có Invoice và Payment.
- When USER mở lịch sử tài chính.
- Then USER xem được đầy đủ Invoice/Payment của chính mình và lọc theo kỳ, trạng thái hoặc thời gian.
- And không xem được lịch sử của USER khác.

## 6. Meter

### AC-METER-01 — Reading giảm

- Given reading trước là X.
- When STAFF nhập reading mới nhỏ hơn X mà không có reset event.
- Then hệ thống từ chối.

### AC-METER-02 — Snapshot tariff

- Given tariff thay đổi sau khi Invoice phát hành.
- When xem lại Invoice cũ.
- Then Invoice vẫn giữ amount/đơn giá đã chốt tại thời điểm phát hành.

## 7. Transfer

### AC-TRANS-01 — Atomic transfer

- Given USER có Assignment ACTIVE và Bed đích AVAILABLE.
- When STAFF approve transfer.
- Then Assignment cũ end, Bed cũ available, Assignment mới active và Bed mới occupied trong một transaction.
- And nếu bất kỳ bước nào lỗi, không bước nào được commit.

### AC-TRANS-02 — Điều kiện thực tế

- Given USER không có cả Assignment và Contract ACTIVE.
- When USER gửi Transfer Request.
- Then hệ thống từ chối.
- And STAFF chỉ được complete sau khi xác nhận khảo sát, Room/Bed đích phù hợp và còn khả dụng.

## 8. Return

### AC-RETURN-01 — Còn công nợ

- Given USER còn công nợ.
- When STAFF xử lý Return Request.
- Then request chuyển trạng thái chờ thanh toán hoặc trạng thái đã duyệt.
- And hệ thống không tự xóa nghĩa vụ tài chính.

### AC-RETURN-02 — Atomic return

- Given Return Request đủ điều kiện hoàn tất.
- When STAFF complete.
- Then Assignment end, Bed available và Contract terminated trong một transaction.

## 9. Frontend platform

### AC-FE-01 — Custom component enforcement

- Given source thuộc page/feature/layout/composite component.
- When CI chạy lint.
- Then CI fail nếu source dùng trực tiếp native interactive element bị cấm.

### AC-FE-02 — Accessibility semantics

- Given custom Button/Input/Label/Dialog/Table.
- When render.
- Then component nền vẫn dùng native semantic phù hợp và hỗ trợ keyboard/focus/ARIA.

### AC-FE-03 — Reduced motion

- Given người dùng bật `prefers-reduced-motion`.
- When mở trang có GSAP/Three.js.
- Then animation không thiết yếu được tắt hoặc giảm đáng kể.

### AC-FE-04 — Three.js fallback

- Given WebGL không khả dụng hoặc scene lỗi.
- When trang được mở.
- Then nội dung và thao tác chính vẫn hoạt động với fallback.

### AC-FE-05 — Cleanup

- Given người dùng điều hướng khỏi route có GSAP/Three.js.
- When component unmount.
- Then timeline, ScrollTrigger, render loop, resource GPU và event listener được cleanup.

## 10. Definition of Ready cho Phase 1

- [x] Tất cả decision ảnh hưởng Identity/RBAC đã `ACCEPTED`.
- [x] Permission matrix đã `ACCEPTED`.
- [x] Account states đã `ACCEPTED`.
- [x] ERD Identity và migration strategy đã `ACCEPTED`.
- [x] API conventions đã `ACCEPTED`.
- [x] Frontend conventions đã `ACCEPTED`.
- [x] Acceptance criteria AUTH/FE không còn điểm mơ hồ.

