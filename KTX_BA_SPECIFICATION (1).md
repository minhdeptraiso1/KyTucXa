# KÝ TÚC XÁ MANAGEMENT SYSTEM — BA SPECIFICATION

## 1. Mục đích tài liệu

Tài liệu này là đặc tả nghiệp vụ (Business Analysis Specification) cho hệ thống quản lý ký túc xá.

Mục tiêu của tài liệu:

- Là nguồn yêu cầu nghiệp vụ chính để phát triển hệ thống.
- Là căn cứ để Codex phân tích, thiết kế và triển khai Backend / Frontend.
- Giúp thống nhất Actor, Role, Use Case, Business Rule, Entity và luồng nghiệp vụ.
- Tránh tự ý mở rộng scope hoặc thay đổi nghiệp vụ khi triển khai.

> **Quy tắc quan trọng:** Nếu yêu cầu mới mâu thuẫn với tài liệu này, phải báo rõ phần bị ảnh hưởng và hỏi xác nhận trước khi thay đổi kiến trúc hoặc nghiệp vụ cốt lõi.

---

# 2. Tổng quan hệ thống

## 2.1. Tên hệ thống

**Hệ thống quản lý ký túc xá**

## 2.2. Mục tiêu

Hệ thống hỗ trợ quản lý tập trung:

1. Tài khoản và phân quyền.
2. Người ở / sinh viên.
3. Tòa nhà, tầng, phòng và giường.
4. Đăng ký KTX.
5. Xét duyệt đăng ký.
6. Phân phòng / phân giường.
7. Hợp đồng.
8. Điện, nước.
9. Hóa đơn và các khoản phí.
10. Thanh toán.
11. Yêu cầu / sự cố.
12. Chuyển phòng.
13. Trả phòng.
14. Vi phạm.
15. Thông báo.
16. Báo cáo và thống kê.
17. Audit log.

---

# 3. Actor và Role

Hệ thống CHỈ có 3 Role:

| Role | Mô tả |
|---|---|
| `USER` | Sinh viên / người thuê KTX, sử dụng dịch vụ KTX |
| `STAFF` | Nhân viên quản lý và vận hành toàn bộ nghiệp vụ KTX |
| `ADMIN` | Quản trị hệ thống, tài khoản, role, permission và cấu hình |

## 3.1. Nguyên tắc phân quyền

### USER

Chỉ được:

- Xem dữ liệu KTX công khai.
- Quản lý profile của chính mình.
- Đăng ký KTX.
- Theo dõi đăng ký.
- Xem phòng / giường đang ở.
- Xem hợp đồng của mình.
- Xem hóa đơn của mình.
- Thanh toán.
- Xem lịch sử thanh toán của mình.
- Gửi yêu cầu / báo sự cố.
- Xin chuyển phòng.
- Xin trả phòng.
- Xem thông báo của mình.
- Xem vi phạm của mình.

### STAFF

Được xử lý nghiệp vụ vận hành KTX:

- Quản lý USER.
- Quản lý tòa nhà / tầng / phòng / giường.
- Xử lý đăng ký KTX.
- Duyệt / từ chối đăng ký.
- Phân phòng / phân giường.
- Chuyển phòng / chuyển giường.
- Quản lý hợp đồng.
- Quản lý điện nước.
- Quản lý hóa đơn.
- Quản lý thanh toán.
- Xử lý yêu cầu / sự cố.
- Quản lý vi phạm.
- Xử lý trả phòng.
- Xem dashboard.
- Xem báo cáo.

### ADMIN

Chịu trách nhiệm quản trị hệ thống:

- Quản lý tài khoản.
- Tạo / khóa / mở khóa tài khoản.
- Quản lý Role.
- Quản lý Permission.
- Cấu hình hệ thống.
- Xem Audit Log.

> ADMIN không mặc định thực hiện nghiệp vụ vận hành KTX. Nếu cần, ADMIN có thể được cấp thêm permission riêng, nhưng không tự động coi ADMIN = STAFF.

---

# 4. Use Case tổng thể

## UC01 — Authentication & Account

- Đăng ký tài khoản USER.
- Đăng nhập.
- Đăng xuất.
- Đổi mật khẩu.
- Quên / reset mật khẩu.
- Quản lý profile.
- Khóa / mở khóa tài khoản.

## UC02 — User Management

STAFF:

- Xem danh sách USER.
- Tìm kiếm USER.
- Xem chi tiết USER.
- Cập nhật thông tin USER.
- Khóa tài khoản USER theo permission.

ADMIN:

- Quản lý toàn bộ account.
- Quản lý role.
- Quản lý permission.

## UC03 — Facility Management

- Quản lý tòa nhà.
- Quản lý tầng.
- Quản lý phòng.
- Quản lý giường.
- Xem trạng thái phòng.
- Xem trạng thái giường.

Cấu trúc:

```text
Building
    ↓
Floor
    ↓
Room
    ↓
Bed
```

## UC04 — KTX Registration

USER:

- Xem thông tin phòng.
- Xem phòng có chỗ.
- Gửi đăng ký KTX.
- Xem trạng thái đăng ký.
- Hủy đăng ký khi còn hợp lệ.

STAFF:

- Xem danh sách đăng ký.
- Xem chi tiết hồ sơ.
- Duyệt đăng ký.
- Từ chối đăng ký.
- Ghi nhận lý do từ chối.

## UC05 — Room / Bed Assignment

STAFF:

- Phân USER vào phòng.
- Phân USER vào giường.
- Chuyển giường.
- Chuyển phòng.
- Xem lịch sử phân phòng.
- Xem phòng còn chỗ.

## UC06 — Contract Management

STAFF:

- Tạo hợp đồng.
- Xem hợp đồng.
- Gia hạn hợp đồng.
- Cập nhật thông tin hợp đồng theo rule.
- Thanh lý hợp đồng.
- Theo dõi trạng thái hợp đồng.

USER:

- Xem hợp đồng của mình.

## UC07 — Electricity / Water Management

STAFF:

- Quản lý meter.
- Nhập chỉ số điện.
- Nhập chỉ số nước.
- Xem lịch sử chỉ số.
- Tính mức tiêu thụ.
- Áp dụng đơn giá.
- Tạo khoản phí điện / nước.

## UC08 — Invoice Management

STAFF:

- Tạo hóa đơn.
- Xem hóa đơn.
- Theo dõi hóa đơn.
- Quản lý chi tiết hóa đơn.
- Theo dõi công nợ.

USER:

- Xem hóa đơn của mình.

Cấu trúc:

```text
Invoice
    ↓
InvoiceDetail
    ├── ROOM_FEE
    ├── ELECTRICITY
    ├── WATER
    ├── SERVICE
    └── FINE
```

## UC09 — Payment Management

USER:

- Chọn hóa đơn.
- Thanh toán.
- Xem lịch sử thanh toán.

STAFF:

- Xem giao dịch.
- Xác nhận / đối soát thanh toán nếu phương thức yêu cầu.
- Theo dõi thanh toán thất bại.

## UC10 — Request / Incident Management

USER:

- Gửi yêu cầu.
- Báo sự cố.
- Đính kèm mô tả / hình ảnh.
- Theo dõi trạng thái.

STAFF:

- Tiếp nhận.
- Xử lý.
- Cập nhật trạng thái.
- Hoàn thành.
- Từ chối nếu không hợp lệ.

## UC11 — Room Transfer

USER:

- Gửi yêu cầu chuyển phòng.
- Chọn phòng mong muốn nếu được phép.
- Theo dõi trạng thái.

STAFF:

- Kiểm tra điều kiện.
- Kiểm tra phòng / giường trống.
- Duyệt / từ chối.
- Cập nhật assignment.
- Cập nhật hợp đồng nếu cần.
- Lưu lịch sử.

## UC12 — Room Return

USER:

- Gửi yêu cầu trả phòng.
- Theo dõi trạng thái.

STAFF:

- Kiểm tra công nợ.
- Kiểm tra tài sản / tình trạng phòng nếu nghiệp vụ yêu cầu.
- Xác nhận trả phòng.
- Thanh lý hợp đồng.
- Giải phóng giường.

## UC13 — Violation Management

STAFF:

- Tạo biên bản vi phạm.
- Chọn loại vi phạm.
- Nhập mô tả.
- Nhập mức phạt.
- Theo dõi trạng thái.

USER:

- Xem vi phạm của mình.
- Xem khoản phạt liên quan.

## UC14 — Notification

- Thông báo đăng ký được duyệt / từ chối.
- Thông báo hóa đơn.
- Thông báo thanh toán.
- Thông báo yêu cầu / sự cố.
- Thông báo chuyển phòng.
- Thông báo trả phòng.
- Thông báo vi phạm.

## UC15 — Dashboard & Reporting

STAFF:

- Tổng số USER đang ở.
- Tổng số phòng.
- Phòng còn trống.
- Giường còn trống.
- Tỷ lệ lấp đầy.
- Đăng ký chờ duyệt.
- Hóa đơn chưa thanh toán.
- Công nợ.
- Sự cố đang xử lý.
- Vi phạm.

ADMIN:

- Dashboard quản trị hệ thống.
- Account statistics.
- Role / permission overview.
- Audit log.

---

# 5. Actor → Use Case Matrix

| Use Case | USER | STAFF | ADMIN |
|---|:---:|:---:|:---:|
| Đăng ký tài khoản | ✓ | - | - |
| Đăng nhập | ✓ | ✓ | ✓ |
| Đăng xuất | ✓ | ✓ | ✓ |
| Quản lý profile | ✓ | ✓ | ✓ |
| Quản lý USER | - | ✓ | ✓* |
| Quản lý tòa nhà | - | ✓ | - |
| Quản lý tầng | - | ✓ | - |
| Quản lý phòng | Xem | ✓ | - |
| Quản lý giường | Xem | ✓ | - |
| Đăng ký KTX | ✓ | - | - |
| Duyệt đăng ký | - | ✓ | - |
| Phân phòng | - | ✓ | - |
| Chuyển phòng | ✓ yêu cầu | ✓ xử lý | - |
| Quản lý hợp đồng | Xem | ✓ | - |
| Điện nước | Xem liên quan | ✓ | - |
| Hóa đơn | Xem | ✓ | - |
| Thanh toán | ✓ | ✓ | - |
| Báo sự cố | ✓ | ✓ | - |
| Xử lý sự cố | - | ✓ | - |
| Vi phạm | Xem | ✓ | - |
| Trả phòng | ✓ yêu cầu | ✓ xử lý | - |
| Báo cáo | - | ✓ | ✓ |
| Quản lý account | - | - | ✓ |
| Quản lý role | - | - | ✓ |
| Quản lý permission | - | - | ✓ |
| Cấu hình hệ thống | - | - | ✓ |
| Audit Log | - | - | ✓ |

`*` ADMIN quản lý account ở cấp hệ thống; STAFF quản lý USER ở góc độ nghiệp vụ KTX.

---

# 6. Business Rules

## 6.1. Authentication

**BR-AUTH-01**

Tài khoản bị khóa không được đăng nhập.

**BR-AUTH-02**

User chỉ được truy cập tài nguyên mà role / permission cho phép.

**BR-AUTH-03**

Không được lưu password dạng plaintext.

**BR-AUTH-04**

Các API nghiệp vụ phải kiểm tra authentication và authorization ở Backend.

---

## 6.2. User

**BR-USER-01**

Mỗi account có một role chính trong phạm vi phiên bản hiện tại.

**BR-USER-02**

USER chỉ được xem / sửa dữ liệu cá nhân thuộc quyền của mình.

**BR-USER-03**

Không được xóa cứng USER đã phát sinh nghiệp vụ.

---

## 6.3. Room / Bed

**BR-FAC-01**

Một Building có nhiều Floor.

**BR-FAC-02**

Một Floor có nhiều Room.

**BR-FAC-03**

Một Room có nhiều Bed.

**BR-FAC-04**

Một Bed chỉ được gán cho tối đa một USER tại cùng một thời điểm.

**BR-FAC-05**

Không được phân USER vào Bed có trạng thái `OCCUPIED`.

**BR-FAC-06**

Bed `MAINTENANCE` hoặc `INACTIVE` không được sử dụng.

**BR-FAC-07**

Room `MAINTENANCE` hoặc `INACTIVE` không được phân người ở.

**BR-FAC-08**

Trạng thái ROOM có thể được tính từ trạng thái các BED, nhưng không được để dữ liệu mâu thuẫn giữa Room và Bed.

---

# 7. Registration Rules

**BR-REG-01**

Một USER không được có nhiều registration đang hoạt động / chờ duyệt trái rule.

**BR-REG-02**

Registration mới mặc định `PENDING`.

**BR-REG-03**

Chỉ STAFF có permission phù hợp được duyệt / từ chối registration.

**BR-REG-04**

Không được duyệt registration nếu không còn Bed phù hợp.

**BR-REG-05**

Registration `REJECTED` phải có lý do từ chối nếu nghiệp vụ yêu cầu.

**BR-REG-06**

USER chỉ được hủy registration khi registration đang ở trạng thái cho phép hủy.

---

# 8. Assignment Rules

**BR-ASG-01**

Assignment phải tham chiếu USER và BED hợp lệ.

**BR-ASG-02**

Một USER chỉ có tối đa một assignment đang ACTIVE.

**BR-ASG-03**

Một BED chỉ có tối đa một assignment ACTIVE tại một thời điểm.

**BR-ASG-04**

Mọi chuyển phòng / chuyển giường phải lưu lịch sử.

**BR-ASG-05**

Khi assignment ACTIVE mới được tạo, BED phải chuyển sang `OCCUPIED`.

**BR-ASG-06**

Khi assignment kết thúc, BED phải được giải phóng về `AVAILABLE` nếu không có nghiệp vụ khác giữ Bed.

---

# 9. Contract Rules

**BR-CON-01**

Một USER chỉ có tối đa một Contract `ACTIVE`.

**BR-CON-02**

Contract phải có ngày bắt đầu và ngày kết thúc hợp lệ.

**BR-CON-03**

Không được tạo Contract ACTIVE nếu USER không có assignment hợp lệ.

**BR-CON-04**

Khi trả phòng hoàn tất, Contract phải được `TERMINATED` hoặc trạng thái phù hợp.

**BR-CON-05**

Không được xóa cứng Contract đã phát sinh giao dịch.

---

# 10. Invoice Rules

**BR-INV-01**

Invoice phải thuộc về một USER hợp lệ.

**BR-INV-02**

Invoice có thể có nhiều InvoiceDetail.

**BR-INV-03**

Tổng Invoice = tổng các InvoiceDetail hợp lệ.

**BR-INV-04**

Invoice `PAID` không được sửa trực tiếp các giá trị tài chính đã chốt.

**BR-INV-05**

Invoice quá hạn phải được đánh dấu `OVERDUE` theo rule hệ thống.

**BR-INV-06**

Không được xóa cứng Invoice đã có Payment.

---

# 11. Payment Rules

**BR-PAY-01**

Payment phải tham chiếu Invoice hợp lệ.

**BR-PAY-02**

USER chỉ được thanh toán Invoice của chính mình.

**BR-PAY-03**

Tổng Payment hợp lệ không được vượt quá số tiền phải thanh toán nếu hệ thống không hỗ trợ overpayment.

**BR-PAY-04**

Khi thanh toán đủ, Invoice chuyển `PAID`.

**BR-PAY-05**

Mọi giao dịch Payment phải lưu thời gian và trạng thái.

---

# 12. Electricity / Water Rules

**BR-METER-01**

Chỉ số mới không được nhỏ hơn chỉ số trước đó, trừ khi có nghiệp vụ reset meter được ghi nhận.

**BR-METER-02**

Mức tiêu thụ = chỉ số mới - chỉ số cũ.

**BR-METER-03**

Đơn giá phải được lưu / xác định rõ tại thời điểm tính phí.

**BR-METER-04**

Không được tạo khoản phí điện nước âm.

---

# 13. Request / Incident Rules

**BR-REQ-01**

USER chỉ được xem request của chính mình.

**BR-REQ-02**

Request mới mặc định `PENDING`.

**BR-REQ-03**

STAFF cập nhật request phải lưu người xử lý và thời gian.

**BR-REQ-04**

Request `RESOLVED` không được quay lại trạng thái xử lý nếu không có nghiệp vụ reopen.

---

# 14. Room Transfer Rules

**BR-TRANS-01**

USER phải có assignment ACTIVE mới được yêu cầu chuyển phòng.

**BR-TRANS-02**

Phòng / giường đích phải còn khả dụng.

**BR-TRANS-03**

Không được chuyển vào Bed đang `OCCUPIED`.

**BR-TRANS-04**

Chuyển phòng phải giải phóng Bed cũ.

**BR-TRANS-05**

Chuyển phòng phải tạo lịch sử assignment.

**BR-TRANS-06**

Nếu nghiệp vụ yêu cầu, Contract phải được cập nhật hoặc tạo phụ lục / phiên bản phù hợp.

---

# 15. Room Return Rules

**BR-RETURN-01**

USER phải có Contract / Assignment ACTIVE để yêu cầu trả phòng.

**BR-RETURN-02**

STAFF phải kiểm tra công nợ trước khi hoàn tất trả phòng.

**BR-RETURN-03**

Nếu còn công nợ, hệ thống phải ghi nhận trạng thái xử lý thay vì tự động xóa nghĩa vụ tài chính.

**BR-RETURN-04**

Khi trả phòng hoàn tất, Assignment phải kết thúc.

**BR-RETURN-05**

Bed phải được giải phóng.

**BR-RETURN-06**

Contract phải chuyển sang trạng thái kết thúc phù hợp.

---

# 16. Violation Rules

**BR-VIO-01**

Violation phải gắn với USER.

**BR-VIO-02**

Nếu có tiền phạt, số tiền phạt không được âm.

**BR-VIO-03**

Violation đã ghi nhận không được xóa cứng nếu đã ảnh hưởng đến Invoice / Payment.

---

# 17. Audit Rules

Các nghiệp vụ quan trọng phải lưu:

- Người thực hiện.
- Thời gian.
- Action.
- Entity.
- Entity ID.
- Giá trị trước / sau nếu cần.
- IP / metadata nếu hệ thống yêu cầu.

Các action quan trọng:

```text
CREATE
UPDATE
DELETE
APPROVE
REJECT
ASSIGN
TRANSFER
TERMINATE
PAY
LOGIN
LOGOUT
```

Không được dùng Audit Log để lưu password hoặc dữ liệu bí mật.

---

# 18. Main Business Flows

## 18.1. Đăng ký KTX

```text
USER
  ↓
Đăng nhập
  ↓
Xem phòng / loại phòng
  ↓
Chọn nhu cầu
  ↓
Gửi Registration
  ↓
PENDING
  ↓
STAFF kiểm tra
  ├── REJECTED
  │      ↓
  │   Thông báo lý do
  │
  └── APPROVED
         ↓
     Phân phòng / giường
         ↓
     Tạo Contract
         ↓
     USER nhận thông tin chỗ ở
```

---

# 19. Flow phân phòng

```text
Registration = APPROVED
        ↓
STAFF mở chức năng Assignment
        ↓
Chọn Building
        ↓
Chọn Floor
        ↓
Chọn Room
        ↓
Chọn Bed AVAILABLE
        ↓
Validate USER chưa có Assignment ACTIVE
        ↓
Tạo Assignment
        ↓
Bed = OCCUPIED
        ↓
Tạo Contract
```

---

# 20. Flow chuyển phòng

```text
USER
 ↓
Gửi Transfer Request
 ↓
PENDING
 ↓
STAFF kiểm tra
 ↓
Kiểm tra điều kiện
 ↓
Kiểm tra Bed đích
 ├── Không hợp lệ → REJECTED
 │
 └── Hợp lệ
       ↓
    APPROVED
       ↓
    Kết thúc Assignment cũ
       ↓
    Bed cũ = AVAILABLE
       ↓
    Tạo Assignment mới
       ↓
    Bed mới = OCCUPIED
       ↓
    Cập nhật Contract nếu cần
       ↓
    Ghi Audit Log
```

---

# 21. Flow trả phòng

```text
USER
 ↓
Gửi Return Request
 ↓
STAFF tiếp nhận
 ↓
Kiểm tra Contract
 ↓
Kiểm tra công nợ
 ↓
Kiểm tra tài sản / phòng nếu cần
 ↓
Xác nhận
 ↓
Terminate Assignment
 ↓
Bed = AVAILABLE
 ↓
Terminate Contract
 ↓
Hoàn tất trả phòng
```

---

# 22. Flow hóa đơn và thanh toán

```text
STAFF
 ↓
Tạo Invoice
 ↓
InvoiceDetail
 ├── Room Fee
 ├── Electricity
 ├── Water
 ├── Service
 └── Fine
 ↓
Invoice = UNPAID
 ↓
USER xem Invoice
 ↓
Thanh toán
 ↓
Payment
 ↓
Kiểm tra kết quả
 ├── FAILED
 │
 └── SUCCESS
       ↓
    Cập nhật Payment
       ↓
    Cập nhật Invoice
       ↓
    PAID nếu đã thanh toán đủ
```

---

# 23. Flow báo sự cố

```text
USER
 ↓
Tạo Request
 ↓
PENDING
 ↓
STAFF tiếp nhận
 ↓
PROCESSING
 ↓
Xử lý
 ↓
RESOLVED
 ↓
Thông báo USER
```

---

# 24. Entity / Database Model

## 24.1. Authentication

### users

Đề xuất các field chính:

```text
id
username
email
password_hash
full_name
phone
status
role_id
created_at
updated_at
```

### roles

```text
id
code
name
description
```

Các role mặc định:

```text
ADMIN
STAFF
USER
```

Nếu triển khai RBAC đầy đủ có thể thêm:

```text
permissions
role_permissions
```

---

# 25. Facility Entities

## buildings

```text
id
code
name
address
description
status
created_at
updated_at
```

## floors

```text
id
building_id
floor_number
name
status
created_at
updated_at
```

## rooms

```text
id
floor_id
code
name
capacity
gender_type
room_type
price
status
description
created_at
updated_at
```

## beds

```text
id
room_id
code
status
created_at
updated_at
```

Quan hệ:

```text
BUILDING 1 ─── N FLOOR
FLOOR    1 ─── N ROOM
ROOM     1 ─── N BED
```

---

# 26. Registration / Assignment / Contract

## registrations

```text
id
user_id
requested_room_type
requested_gender_type
preferred_start_date
preferred_end_date
reason
status
rejection_reason
reviewed_by
reviewed_at
created_at
updated_at
```

## room_assignments

```text
id
user_id
bed_id
start_date
end_date
status
assigned_by
created_at
updated_at
```

## contracts

```text
id
contract_code
user_id
assignment_id
start_date
end_date
rental_price
deposit
status
terminated_at
terminated_by
created_at
updated_at
```

Quan hệ:

```text
USER 1 ─── N REGISTRATION

USER 1 ─── N ROOM_ASSIGNMENT
BED  1 ─── N ROOM_ASSIGNMENT

USER 1 ─── N CONTRACT
ROOM_ASSIGNMENT 1 ─── N CONTRACT
```

Có thể giới hạn bằng Business Rule chỉ có một record ACTIVE tại một thời điểm.

---

# 27. Meter / Electricity / Water

## meters

```text
id
room_id
meter_type
meter_code
unit
status
created_at
updated_at
```

`meter_type`:

```text
ELECTRICITY
WATER
```

## meter_readings

```text
id
meter_id
reading_value
reading_date
previous_reading
consumption
recorded_by
created_at
```

---

# 28. Invoice / Payment

## invoices

```text
id
invoice_code
user_id
billing_month
due_date
subtotal
discount
fine_amount
total_amount
paid_amount
status
created_at
updated_at
```

## invoice_details

```text
id
invoice_id
item_type
description
quantity
unit_price
amount
reference_id
created_at
```

`item_type`:

```text
ROOM_FEE
ELECTRICITY
WATER
SERVICE
FINE
OTHER
```

## payments

```text
id
invoice_id
user_id
payment_code
amount
method
status
transaction_reference
paid_at
created_at
```

---

# 29. Request / Violation / Notification

## request_types

```text
id
code
name
description
```

## requests

```text
id
user_id
request_type_id
title
description
priority
status
assigned_to
resolved_by
resolved_at
created_at
updated_at
```

`status`:

```text
PENDING
PROCESSING
RESOLVED
REJECTED
```

## violations

```text
id
user_id
type
description
fine_amount
occurred_at
created_by
status
created_at
updated_at
```

## notifications

```text
id
user_id
title
content
type
is_read
created_at
```

---

# 30. Audit Log

## audit_logs

```text
id
user_id
action
entity_type
entity_id
old_value
new_value
ip_address
user_agent
created_at
```

Không lưu:

```text
password
password_hash
access_token
refresh_token
```

trong audit log.

---

# 31. ERD tổng quát

```text
                    ┌────────────┐
                    │   roles    │
                    └─────┬──────┘
                          │
                          N
                          │
                    ┌─────▼──────┐
                    │   users    │
                    └─────┬──────┘
                          │
        ┌─────────────────┼────────────────────┐
        │                 │                    │
        ▼                 ▼                    ▼
 registrations       contracts          room_assignments
                                             │
                                             │
                                             ▼
                                           beds
                                             │
                                             ▼
                                           rooms
                                             │
                                             ▼
                                          floors
                                             │
                                             ▼
                                         buildings

users
 │
 ├──── invoices ──── invoice_details
 │         │
 │         └──── payments
 │
 ├──── requests ─── request_types
 │
 ├──── violations
 │
 └──── notifications

rooms
 │
 └──── meters
          │
          └──── meter_readings
```

---

# 32. Trạng thái chuẩn

## Registration

```text
PENDING
APPROVED
REJECTED
CANCELLED
```

## Contract

```text
DRAFT
ACTIVE
EXPIRED
TERMINATED
```

## Room

```text
AVAILABLE
FULL
MAINTENANCE
INACTIVE
```

## Bed

```text
AVAILABLE
OCCUPIED
MAINTENANCE
INACTIVE
```

## Invoice

```text
UNPAID
PARTIAL
PAID
OVERDUE
CANCELLED
```

## Payment

```text
PENDING
SUCCESS
FAILED
CANCELLED
```

## Request

```text
PENDING
PROCESSING
RESOLVED
REJECTED
```

## Assignment

```text
ACTIVE
ENDED
CANCELLED
```

---

# 33. Core Data Integrity Rules

Không được để các tình trạng sau xảy ra:

```text
1. Một Bed có 2 USER ACTIVE cùng thời điểm.
2. Một USER có 2 Assignment ACTIVE.
3. Một USER có 2 Contract ACTIVE.
4. Assignment ACTIVE nhưng Bed = AVAILABLE.
5. Assignment ENDED nhưng Bed vẫn OCCUPIED.
6. Contract ACTIVE nhưng không có Assignment hợp lệ.
7. Invoice PAID nhưng paid_amount < total_amount.
8. Payment SUCCESS nhưng không thuộc Invoice hợp lệ.
9. Meter reading mới nhỏ hơn reading trước mà không có reset.
10. User truy cập dữ liệu của User khác.
```

---

# 34. Soft Delete

Đối với dữ liệu nghiệp vụ đã phát sinh giao dịch:

**Không xóa cứng tùy tiện.**

Ưu tiên:

```text
status = INACTIVE
```

hoặc:

```text
deleted_at
```

Các entity cần đặc biệt cẩn thận:

- User.
- Room.
- Bed.
- Contract.
- Invoice.
- Payment.
- Assignment.
- Violation.
- Request.

---

# 35. Scope phát triển

## Phase 1 — Foundation

- Authentication.
- Authorization.
- User.
- Role.
- Permission nếu dùng RBAC.
- Profile.

## Phase 2 — Facility

- Building.
- Floor.
- Room.
- Bed.
- Trạng thái phòng / giường.

## Phase 3 — Registration

- KTX Registration.
- Duyệt / từ chối.
- Phân phòng.
- Phân giường.

## Phase 4 — Contract

- Contract.
- Gia hạn.
- Thanh lý.
- Lịch sử assignment.

## Phase 5 — Finance

- Meter.
- Meter Reading.
- Electricity / Water.
- Invoice.
- Invoice Detail.
- Payment.

## Phase 6 — Operations

- Request / Incident.
- Room Transfer.
- Room Return.
- Violation.
- Notification.

## Phase 7 — Dashboard / Reporting

- Dashboard STAFF.
- Dashboard ADMIN.
- Reports.
- Statistics.
- Export nếu cần.

## Phase 8 — Audit / Hardening

- Audit Log.
- Validation.
- Authorization.
- Transaction.
- Concurrency handling.
- Data integrity.
- Error handling.
- Security review.

---

# 36. Quy tắc dành cho Codex khi triển khai

## 36.1. Đọc tài liệu trước

Trước khi code:

1. Đọc toàn bộ file BA này.
2. Kiểm tra source code hiện tại.
3. Kiểm tra database hiện tại.
4. Kiểm tra API hiện tại.
5. Kiểm tra authentication / authorization hiện tại.
6. Không tự ý xóa hoặc rewrite code đang hoạt động.

## 36.2. Không tự ý thay đổi nghiệp vụ

Không tự ý:

- Thêm Role ngoài `ADMIN`, `STAFF`, `USER`.
- Thêm module lớn ngoài scope.
- Thay đổi trạng thái nghiệp vụ.
- Thay đổi quan hệ Entity.
- Bỏ Business Rule.
- Thay đổi flow đăng ký / phân phòng / trả phòng.
- Thay đổi API contract đang được sử dụng.

Nếu thấy cần thay đổi:

```text
Phát hiện vấn đề
      ↓
Phân tích impact
      ↓
Đề xuất phương án
      ↓
Dừng
      ↓
Chờ người dùng xác nhận
```

## 36.3. Không làm CRUD mù

Mọi API phải tôn trọng:

- Business Rule.
- Role.
- Ownership.
- State transition.
- Transaction.
- Data integrity.

Ví dụ:

Không chỉ làm:

```http
POST /assignments
```

mà phải validate:

```text
USER tồn tại?
USER đã có Assignment ACTIVE chưa?
BED tồn tại?
BED AVAILABLE?
ROOM active?
CONTRACT có hợp lệ?
Có conflict thời gian?
```

## 36.4. Transaction

Các nghiệp vụ làm thay đổi nhiều bảng phải được xử lý transaction.

Ví dụ:

### Phân phòng

```text
Create Assignment
+
Update Bed
+
Create / Update Contract
```

### Chuyển phòng

```text
End old Assignment
+
Release old Bed
+
Create new Assignment
+
Occupy new Bed
+
Update Contract
+
Audit Log
```

Nếu một bước lỗi, phải rollback các thay đổi liên quan.

---

# 37. Quy tắc API

Backend phải:

- Validate input.
- Validate permission.
- Validate ownership.
- Validate state transition.
- Trả HTTP status phù hợp.
- Không expose entity database trực tiếp nếu kiến trúc sử dụng DTO.
- Dùng DTO cho request / response.
- Có global exception handling.
- Có error response thống nhất.
- Có pagination cho danh sách lớn.
- Có filtering / searching khi cần.

---

# 38. Quy tắc Frontend

Frontend phải:

- Phản ánh đúng Role.
- Không hiển thị chức năng người dùng không có permission.
- Nhưng không được coi frontend là lớp bảo mật duy nhất.
- Backend vẫn phải authorization.
- Hiển thị rõ trạng thái nghiệp vụ.
- Confirm trước action nguy hiểm.
- Không cho user thao tác những state transition không hợp lệ.
- Đồng bộ trạng thái với API.

---

# 39. Definition of Done cho một chức năng

Một chức năng chỉ được coi là hoàn thành khi:

```text
Business Rule
      ↓
Database
      ↓
Entity / Model
      ↓
Repository
      ↓
Service
      ↓
Validation
      ↓
Authorization
      ↓
Controller / API
      ↓
DTO
      ↓
Exception Handling
      ↓
Frontend
      ↓
Loading / Error / Empty State
      ↓
Test
      ↓
Review
```

Không coi một chức năng là hoàn thành chỉ vì API trả được `200 OK`.

---

# 40. Nguyên tắc triển khai cuối cùng

1. Ưu tiên đúng nghiệp vụ hơn tốc độ code.
2. Không phá vỡ chức năng đang hoạt động.
3. Không tạo duplicate business logic giữa Controller và Service.
4. Business logic nằm ở Service / Domain layer phù hợp.
5. Transaction đặt ở boundary nghiệp vụ phù hợp.
6. Không hard-code Role / Permission rải rác trong code.
7. Không dùng magic string cho trạng thái nếu project đã có enum.
8. Không xóa dữ liệu nghiệp vụ quan trọng bằng hard delete.
9. Mọi thay đổi database phải có migration.
10. Mọi API quan trọng phải có validation và authorization.
11. Mọi nghiệp vụ nhiều bước phải xử lý transaction.
12. Ưu tiên lịch sử nghiệp vụ thay vì ghi đè dữ liệu cũ.
13. Khi phát hiện ambiguity, phải hỏi hoặc ghi rõ assumption.
14. Không tự ý mở rộng scope.
15. Trước mỗi Phase lớn phải review kết quả Phase trước.

---

# 41. Thứ tự ưu tiên nghiệp vụ

```text
P0 — CORE
├── Authentication
├── Authorization
├── User
├── Building
├── Floor
├── Room
├── Bed
├── Registration
├── Assignment
├── Contract
├── Invoice
└── Payment

P1 — BUSINESS
├── Electricity / Water
├── Room Transfer
├── Room Return
├── Request / Incident
├── Violation
└── Notification

P2 — ADVANCED
├── Dashboard
├── Reporting
├── Export
├── Online Payment
└── Audit / Advanced Security
```

---

# 42. Kết luận

Kiến trúc nghiệp vụ mục tiêu:

```text
                         KTX MANAGEMENT
                               │
             ┌─────────────────┼─────────────────┐
             │                 │                 │
           ADMIN             STAFF             USER
             │                 │                 │
       System Admin       KTX Operations      KTX User
             │                 │                 │
             └─────────────────┼─────────────────┘
                               │
                     ┌─────────▼─────────┐
                     │      CORE         │
                     │                   │
                     │ Building          │
                     │ Floor             │
                     │ Room              │
                     │ Bed               │
                     │ Registration      │
                     │ Assignment        │
                     │ Contract          │
                     │ Invoice           │
                     │ Payment           │
                     └───────────────────┘
```

Đây là **Baseline BA Specification**. Khi phát triển, mọi thiết kế API, database và UI phải đối chiếu tài liệu này.
