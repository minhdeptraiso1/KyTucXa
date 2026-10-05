# State Transition Specification

Trạng thái: **ACCEPTED**

Mọi transition không được liệt kê bên dưới đều bị từ chối bằng business error. Service phải kiểm tra current state trong transaction.

## 1. Account

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | register | `PENDING` | Public/USER | Dữ liệu hợp lệ, không trùng unique field |
| `PENDING` | verify/activate | `ACTIVE` | USER/System | Verification hợp lệ |
| `PENDING` | deactivate | `INACTIVE` | ADMIN | Có lý do |
| `ACTIVE` | lock | `LOCKED` | ADMIN | Không tự khóa account ADMIN cuối cùng |
| `LOCKED` | unlock | `ACTIVE` | ADMIN | |
| `ACTIVE` | deactivate | `INACTIVE` | ADMIN | Kiểm tra dữ liệu nghiệp vụ liên quan |
| `INACTIVE` | reactivate | `ACTIVE` | ADMIN | Theo retention rule |

## 2. Registration

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | submit | `PENDING` | USER | Không có Registration/Assignment trái rule |
| `PENDING` | cancel | `CANCELLED` | Owner USER | Chưa được review |
| `PENDING` | approve | `APPROVED` | STAFF | Có Bed phù hợp tại thời điểm assign hoặc reservation rule |
| `PENDING` | reject | `REJECTED` | STAFF | Có rejection reason |

`APPROVED`, `REJECTED`, `CANCELLED` là terminal state trong MVP. Nếu cần sửa hồ sơ, tạo Registration mới.

## 3. Assignment

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | assign | `ACTIVE` | STAFF | USER và Bed hợp lệ, không có active conflict |
| `ACTIVE` | end | `ENDED` | STAFF/System | Return/transfer/contract end hợp lệ |
| `ACTIVE` | cancel | `CANCELLED` | STAFF | Chỉ dùng để sửa sai trước thời điểm bắt đầu; có audit reason |

Không cập nhật `bed_id` của Assignment ACTIVE. Chuyển giường/phòng phải end record cũ và tạo record mới.

## 4. Contract

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | create | `DRAFT` | STAFF | Có Assignment hợp lệ |
| `DRAFT` | activate | `ACTIVE` | STAFF | Ngày/giá/deposit hợp lệ, không có Contract ACTIVE khác |
| `DRAFT` | terminate | `TERMINATED` | STAFF | Hủy trước kích hoạt, có reason |
| `ACTIVE` | expire | `EXPIRED` | System/STAFF | Qua end date, không có extension |
| `ACTIVE` | terminate | `TERMINATED` | STAFF | Return hoặc chấm dứt hợp lệ |

Gia hạn không thay terminal Contract. Đề xuất tạo Contract version/addendum mới hoặc cập nhật end date kèm immutable history theo quyết định cuối cùng.

## 5. Invoice

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | issue | `UNPAID` | STAFF/System | Có ít nhất một detail, total > 0 |
| `UNPAID` | pay-partial | `PARTIAL` | USER/STAFF/System | Payment SUCCESS, paid amount < total |
| `UNPAID` | pay-full | `PAID` | USER/STAFF/System | Payment SUCCESS, paid amount = total |
| `UNPAID` | overdue | `OVERDUE` | System | Qua due date và còn nợ |
| `UNPAID` | cancel | `CANCELLED` | STAFF | Chưa có Payment SUCCESS |
| `PARTIAL` | pay-more | `PARTIAL` | USER/STAFF/System | Tổng paid vẫn < total |
| `PARTIAL` | pay-full | `PAID` | USER/STAFF/System | Tổng paid = total |
| `PARTIAL` | overdue | `OVERDUE` | System | Qua due date và còn nợ |
| `OVERDUE` | pay-partial | `OVERDUE` | USER/STAFF/System | Vẫn còn nợ |
| `OVERDUE` | pay-full | `PAID` | USER/STAFF/System | Tổng paid = total |

`PAID` và `CANCELLED` là terminal trong MVP. Điều chỉnh tài chính dùng adjustment/credit record, không sửa trực tiếp.

## 6. Payment

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | initiate | `PENDING` | USER/STAFF | Invoice payable, idempotency key hợp lệ |
| `PENDING` | confirm | `SUCCESS` | STAFF/System | Đối soát thành công |
| `PENDING` | fail | `FAILED` | STAFF/System | Có failure reason |
| `PENDING` | cancel | `CANCELLED` | USER/STAFF/System | Chưa SUCCESS |

`SUCCESS`, `FAILED`, `CANCELLED` là terminal. Retry tạo Payment mới với idempotency key mới.

## 7. Request/Incident

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | submit | `PENDING` | USER | Dữ liệu hợp lệ |
| `PENDING` | accept | `PROCESSING` | STAFF | Lưu assigned/handled by |
| `PENDING` | reject | `REJECTED` | STAFF | Có reason |
| `PROCESSING` | resolve | `RESOLVED` | STAFF | Có resolution note |
| `PROCESSING` | reject | `REJECTED` | STAFF | Phát hiện không hợp lệ, có reason |

MVP chưa hỗ trợ reopen. Nếu bổ sung: chỉ `RESOLVED → PROCESSING`, bắt buộc reason và audit.

## 8. Transfer Request

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | submit | `PENDING` | USER | Có Assignment và Contract ACTIVE |
| `PENDING` | cancel | `CANCELLED` | Owner USER | Chưa xử lý |
| `PENDING` | reject | `REJECTED` | STAFF | Có reason |
| `PENDING` | approve-and-execute | `COMPLETED` | STAFF | Đã khảo sát thực tế; Room/Bed đích phù hợp và khả dụng; transaction chuyển phòng thành công |

Không dùng state `APPROVED` tách rời trong MVP để tránh request đã duyệt nhưng Assignment chưa chuyển. Approve và execute là một transaction.

## 9. Return Request

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | submit | `PENDING` | USER | Có Assignment/Contract ACTIVE |
| `PENDING` | accept | `PROCESSING` | STAFF | Bắt đầu debt/inspection check |
| `PENDING` | cancel | `CANCELLED` | Owner USER | Chưa processing |
| `PENDING` | reject | `REJECTED` | STAFF | Có reason |
| `PROCESSING` | wait-debt | `WAITING_PAYMENT` | STAFF | Còn nghĩa vụ tài chính |
| `WAITING_PAYMENT` | resume | `PROCESSING` | STAFF/System | Công nợ đã xử lý |
| `PROCESSING` | complete | `COMPLETED` | STAFF | Transaction return thành công |

## 10. Violation

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| — | create | `RECORDED` | STAFF | Dữ liệu hợp lệ |
| `RECORDED` | confirm | `CONFIRMED` | STAFF | Hoàn tất review |
| `RECORDED` | void | `VOIDED` | STAFF | Có reason, chưa ảnh hưởng Invoice |
| `CONFIRMED` | invoice | `INVOICED` | STAFF/System | Fine > 0 và Invoice Detail được tạo |
| `CONFIRMED` | close | `CLOSED` | STAFF | Không có fine hoặc đã xử lý |
| `INVOICED` | close | `CLOSED` | System/STAFF | Khoản phạt đã hoàn tất nghĩa vụ |

## 11. Bed

| From | Action | To | Actor | Điều kiện |
|---|---|---|---|---|
| `AVAILABLE` | assign | `OCCUPIED` | STAFF | Assignment ACTIVE được tạo cùng transaction |
| `AVAILABLE` | maintain | `MAINTENANCE` | STAFF | Không có Assignment ACTIVE |
| `AVAILABLE` | deactivate | `INACTIVE` | STAFF | Không có Assignment ACTIVE |
| `OCCUPIED` | release | `AVAILABLE` | STAFF/System | Assignment ACTIVE đã end cùng transaction |
| `MAINTENANCE` | reopen | `AVAILABLE` | STAFF | Room usable |
| `MAINTENANCE` | deactivate | `INACTIVE` | STAFF | |
| `INACTIVE` | activate | `AVAILABLE` | STAFF | Room usable |

