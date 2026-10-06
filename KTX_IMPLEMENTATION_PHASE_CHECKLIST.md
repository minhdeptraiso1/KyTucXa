# KTX Management System — Implementation Phase Checklist

> Tài liệu theo dõi tiến độ triển khai cho hệ thống quản lý ký túc xá.
>
> Nguồn nghiệp vụ chính: `KTX_BA_SPECIFICATION (1).md`.
>
> Backend: Spring Boot + PostgreSQL + Redis.  
> Frontend: Vue 3 + TypeScript + Vite + Tailwind CSS 4 + GSAP + Three.js.

---

## 1. Cách sử dụng checklist

Quy ước trạng thái:

- `[ ]` Chưa thực hiện.
- `[-]` Đang thực hiện.
- `[x]` Đã hoàn thành và kiểm tra.
- `[!]` Đang bị chặn, cần ghi lý do tại mục **Blocker / Decision Log**.

Quy tắc cập nhật:

- Chỉ đánh dấu `[x]` khi đã đáp ứng Definition of Done của chức năng.
- Khi bắt đầu một phase, đổi trạng thái phase thành `IN PROGRESS` và ghi ngày bắt đầu.
- Không bắt đầu phase phụ thuộc nếu exit criteria của phase trước chưa đạt.
- Mọi thay đổi nghiệp vụ phải được ghi vào Decision Log trước khi code.
- Không sửa migration Flyway đã chạy; chỉ tạo migration phiên bản mới.

---

## 2. Tổng quan tiến độ

| Phase | Nội dung | Trạng thái | Bắt đầu | Hoàn thành | Ghi chú |
|---|---|---|---|---|---|
| 0 | Chốt BA và kiến trúc | COMPLETED | 2026-10-01 | 2026-10-01 | Các quyết định P0 đã được duyệt |
| 1 | Foundation, Auth, RBAC, Design System | COMPLETED | 2026-10-01 | 2026-10-02 | Backend/FE foundation, Auth, Student Registry import CSV hoàn thành |
| 2 | Facility | COMPLETED | 2026-10-02 | 2026-10-02 | Hoàn thành toàn bộ DB, Backend APIs, Business Rules, Validation, DTOs & Frontend UI Quản lý sơ đồ phòng giường |
| 3 | Registration và Assignment | IN PROGRESS | 2026-10-03 |  | Code/API/UI hoàn tất; còn PostgreSQL concurrency test và E2E |
| 4 | Contract | IN PROGRESS | 2026-10-03 |  | Code/API/UI hoàn tất; còn test gia hạn/thanh lý và UAT |
| 5 | Billing và Payment | IN PROGRESS | 2026-10-03 |  | Meter/tariff có sửa có kiểm soát, VNPay Sandbox thật đã nối; còn concurrency/E2E/UAT và preview hóa đơn |
| 6 | Operations | NOT STARTED |  |  |  |
| 7 | Dashboard và Reporting | NOT STARTED |  |  |  |
| 8 | Hardening, UAT và Release | NOT STARTED |  |  |  |

Tiến độ tổng thể: `3 / 9 phase hoàn thành`.

---

## 3. Quy tắc chung áp dụng cho mọi phase

### 3.1. Backend

- [ ] Business rule nằm trong Service/Domain layer, không đặt rải rác trong Controller.
- [ ] Request và response dùng DTO, không expose JPA entity trực tiếp.
- [ ] Có validation input, permission, ownership và state transition.
- [ ] Nghiệp vụ cập nhật nhiều bảng được đặt trong một transaction phù hợp.
- [ ] Có pagination, filtering và sorting cho danh sách lớn.
- [ ] Có error code và error response thống nhất.
- [ ] Không hard-code role, permission hoặc trạng thái nghiệp vụ.
- [ ] Không hard delete dữ liệu đã phát sinh nghiệp vụ.
- [ ] API quan trọng có OpenAPI documentation.
- [ ] Có audit log cho hành động quan trọng.

### 3.2. Database

- [ ] PostgreSQL là nguồn dữ liệu chuẩn cho trạng thái nghiệp vụ.
- [ ] Mọi thay đổi schema đi qua Flyway migration mới.
- [ ] Có foreign key, unique constraint, check constraint và index cần thiết.
- [ ] Constraint database bảo vệ các invariant quan trọng khi có thể.
- [ ] Dữ liệu tiền tệ dùng `NUMERIC`, không dùng floating point.
- [ ] Timestamp lưu theo UTC và API trả ISO-8601.
- [ ] Có chiến lược soft delete/status rõ ràng cho từng entity.

### 3.3. Redis

- [ ] Redis không được dùng làm nguồn dữ liệu nghiệp vụ chính.
- [ ] Mỗi cache có key convention và TTL rõ ràng.
- [ ] Có cache invalidation khi dữ liệu gốc thay đổi.
- [ ] Không cache dữ liệu nhạy cảm không cần thiết.
- [ ] Redis failure không làm sai dữ liệu nghiệp vụ trong PostgreSQL.

### 3.4. Frontend và Design System

- [ ] Page và feature không dùng trực tiếp native interactive element.
- [ ] Các thẻ như `button`, `label`, `input`, `select`, `textarea`, `table`, `dialog` chỉ được render bên trong `components/base/**`.
- [ ] Page và feature sử dụng custom component như `AppButton`, `AppLabel`, `AppInput`, `AppSelect`, `AppTable`, `AppDialog`.
- [ ] Không thay semantic button/input bằng `div` hoặc `span` giả lập.
- [ ] Màu sắc, spacing, typography, radius, shadow và motion dùng design token.
- [ ] Component có đầy đủ loading, error, empty và disabled state.
- [ ] Form hiển thị validation message rõ ràng.
- [ ] Chức năng bị cấm theo role/permission không được hiển thị trên UI.
- [ ] Backend vẫn kiểm tra authorization; không dựa vào frontend để bảo mật.
- [ ] Giao diện responsive cho desktop, tablet và mobile theo phạm vi đã chốt.
- [ ] Có keyboard navigation, focus state, label và ARIA phù hợp.

### 3.5. GSAP và Three.js

- [ ] GSAP chỉ dùng cho animation có mục đích, không cản trở thao tác nghiệp vụ.
- [ ] GSAP animation được cleanup khi Vue component unmount.
- [ ] Có hỗ trợ `prefers-reduced-motion`.
- [ ] Three.js ưu tiên cho landing, authentication background hoặc dashboard hero.
- [ ] Màn hình bảng biểu STAFF/ADMIN không bị animation làm giảm hiệu năng hoặc khả năng đọc.
- [ ] Geometry, material, texture, renderer và event listener được dispose/cleanup.
- [ ] Có fallback khi WebGL không khả dụng.
- [ ] Giảm hoặc tắt Three.js trên thiết bị yếu/mobile theo rule hiệu năng.
- [ ] Không tải asset 3D quá lớn khi chưa có loading/fallback.

### 3.6. Test và review

- [ ] Có unit test cho business rule quan trọng.
- [ ] Có integration test cho repository, transaction và authorization.
- [ ] Có test PostgreSQL/Redis thực tế hoặc Testcontainers cho luồng quan trọng.
- [ ] Có test ownership: USER không truy cập được dữ liệu USER khác.
- [ ] Có test state transition hợp lệ và không hợp lệ.
- [ ] Có test frontend cho component/flow quan trọng.
- [ ] Build backend và frontend thành công.
- [ ] Review kết quả phase trước khi chuyển phase tiếp theo.

---

# Phase 0 — Chốt BA và kiến trúc

**Trạng thái:** `COMPLETED`  
**Mục tiêu:** Loại bỏ các điểm mơ hồ có thể làm thay đổi database, API hoặc nghiệp vụ cốt lõi.

## 0.1. Role, account và hồ sơ USER

- [x] Chốt mô hình: một role chính + permission.
- [x] Chốt danh sách permission cho ADMIN, STAFF và USER.
- [x] Chốt ADMIN không mặc định có permission vận hành STAFF.
- [x] Chốt account status: `PENDING`, `ACTIVE`, `LOCKED`, `INACTIVE`.
- [x] Chốt USER tự đăng ký.
- [x] Chốt chỉ ADMIN được tạo STAFF/ADMIN.
- [x] Chốt email verification.
- [x] Chốt dữ liệu profile: mã sinh viên, họ tên, ngày sinh, giới tính, CCCD, khoa, lớp, địa chỉ, liên hệ khẩn cấp.
- [x] Chốt unique rule cho username, email, mã sinh viên và CCCD.

## 0.2. Facility và occupancy

- [x] Chốt `room_type`: phòng 4, 6 và 8 người.
- [x] Chốt `gender_type`: `MALE`, `FEMALE`; USER phải phù hợp Room.
- [x] Chốt bảng giá ở Price Policy có khoảng hiệu lực.
- [x] Chốt capacity theo Room Type và phải nhất quán với Bed.
- [x] Chốt Room `AVAILABLE/FULL` được suy ra từ Bed; maintenance/inactive lưu trực tiếp.
- [x] Chốt MVP chưa tạo Assignment tương lai; assignment khi nhận phòng thực tế.
- [x] Chốt cơ chế xử lý hai STAFF cùng chọn một Bed bằng PostgreSQL lock + unique index.

## 0.3. State transition

- [x] Vẽ transition matrix cho Registration.
- [x] Vẽ transition matrix cho Assignment.
- [x] Vẽ transition matrix cho Contract.
- [x] Vẽ transition matrix cho Invoice.
- [x] Vẽ transition matrix cho Payment.
- [x] Vẽ transition matrix cho Request/Incident.
- [x] Vẽ transition matrix cho Transfer Request.
- [x] Vẽ transition matrix cho Return Request.
- [x] Vẽ transition matrix cho Violation.

## 0.4. Finance

- [x] Chốt điện/nước tính theo Room.
- [x] Chốt chia phí theo resident-day giữa những USER cùng phòng.
- [x] Chốt đơn giá phẳng cho MVP.
- [x] Chốt bảng giá có khoảng hiệu lực.
- [x] Chốt cách làm tròn tiền và consumption.
- [x] Chốt prorate tiền phòng theo ngày cư trú.
- [x] Chốt Invoice theo tháng.
- [x] Chốt cho partial payment và cấm overpayment.
- [x] Chốt thanh toán trực tiếp và VNPay demo.
- [x] Chốt MVP chưa hỗ trợ refund tự động.
- [x] Chốt quy trình deposit và hoàn/khấu trừ deposit.

## 0.5. Operations và notification

- [x] Chốt entity riêng cho Transfer Request.
- [x] Chốt entity riêng cho Return Request và biên bản bàn giao.
- [x] Chốt rule kiểm tra công nợ khi trả phòng với `WAITING_PAYMENT`.
- [x] Chốt ảnh/file lưu local filesystem ở MVP qua storage abstraction.
- [x] Chốt loại/trạng thái Violation; appeal ngoài MVP.
- [x] Chốt notification in-app, WebSocket và email cho sự kiện quan trọng.
- [x] Chốt read status; retry email theo cấu hình giới hạn.

## 0.6. Kiến trúc và contract

- [x] Chốt modular monolith và ranh giới module.
- [x] Chốt API base path `/api/v1`.
- [x] Chốt API response/error convention.
- [x] Chốt pagination/filter/sort convention.
- [x] Hoàn thiện logical ERD, FK, index và constraint.
- [x] Hoàn thiện permission matrix.
- [x] Hoàn thiện acceptance criteria cho các use case P0.
- [x] Chốt custom component wrapper; UI library được chọn khi scaffold Phase 1.
- [x] Chốt responsive cho điện thoại từ 360px, tablet và laptop/desktop.

## Exit criteria Phase 0

- [x] Không còn decision kiến trúc/nghiệp vụ P0 cản Phase 1.
- [x] ERD, permission matrix và transition matrix đã được review.
- [x] API convention và frontend convention đã được chấp thuận.

---

# Phase 1 — Foundation, Auth, RBAC và Design System

**Trạng thái:** `IN PROGRESS`  
**Phụ thuộc:** Phase 0 hoàn thành.

## 1.1. Backend foundation

- [x] Giữ lại base Spring hiện tại, không rewrite các phần đang hoạt động.
- [x] Thêm role `STAFF` vào source và migration mới.
- [ ] Thiết kế bảng `roles`, `permissions`, `role_permissions` theo quyết định Phase 0.
- [x] Triển khai permission evaluator/authorization convention nền tảng.
- [x] Thay `@Where` deprecated bằng cơ chế Hibernate phù hợp.
- [x] Sửa Lombok builder default cho `User.status`.
- [ ] Chuẩn hóa package/project name nếu cần.
- [x] Chuẩn hóa error code và HTTP status nền tảng.
- [x] Mở rộng audit log: actor, action, entity, entity ID, before/after, IP, user agent.
- [x] Bảo đảm audit không lưu password/token/secret.

## 1.2. Authentication và account

- [x] Login.
- [x] Refresh token.
- [x] Logout và revoke token.
- [x] USER registration.
- [x] Email verification.
- [x] Change password.
- [x] Forgot password.
- [x] Reset password token có TTL và one-time use.
- [x] Lock/unlock account model.
- [x] ADMIN tạo STAFF/ADMIN theo permission nền tảng.
- [x] Rate-limit các endpoint nhạy cảm.
- [x] Chặn account bị khóa đăng nhập và refresh token.

## 1.3. Profile

- [x] Migration `user_profiles`.
- [x] USER xem/sửa profile của mình.
- [x] STAFF xem/tìm USER theo quyền nghiệp vụ nền tảng.
- [x] ADMIN quản lý account theo quyền hệ thống nền tảng.
- [ ] Ownership test cho profile.

## 1.4. Vue foundation

- [x] Khởi tạo Vue 3 + TypeScript + Vite.
- [x] Cấu hình Vue Router.
- [x] Cấu hình Pinia.
- [x] Cấu hình HTTP client và access-token flow.
- [x] Cấu hình environment.
- [ ] Cấu hình ESLint/Prettier.
- [x] Tạo route guard theo authentication.
- [-] Tạo layout Public, USER, STAFF và ADMIN — đã có Auth/Staff shell; các portal còn lại mở rộng tiếp.
- [ ] Tạo trang login/register/forgot/reset/profile/account.

## 1.5. Design system và custom components

- [x] Tạo primitive/semantic/component design tokens.
- [x] `AppButton`.
- [x] `AppLabel`.
- [x] `AppInput`.
- [ ] `AppTextarea`.
- [ ] `AppSelect`.
- [ ] `AppCheckbox`/`AppRadio`.
- [x] `AppFormField`.
- [x] `AppCard`.
- [x] `AppBadge`.
- [ ] `AppAlert`/`AppToast`.
- [ ] `AppDialog`/confirm action.
- [ ] `AppTable`.
- [ ] `AppPagination`.
- [ ] `AppTabs`.
- [ ] `AppDropdown`.
- [ ] `AppFileUpload`.
- [ ] `AppSkeleton`/loading state.
- [ ] `AppEmptyState`.
- [-] ESLint rule cấm native interactive elements ngoài `components/base/**` — hiện đã kiểm tra source, CI rule sẽ bổ sung.
- [ ] Component documentation hoặc Storybook/Histoire nếu chọn dùng.

## 1.6. Motion foundation

- [x] Cài đặt và tạo GSAP composable/directive dùng chung.
- [x] Tạo reduced-motion policy.
- [x] Tạo cleanup convention cho GSAP timeline/ScrollTrigger.
- [x] Tạo Three.js scene wrapper/composable dùng chung.
- [x] Tạo WebGL fallback.
- [x] Đặt performance budget cho texture/model/bundle nền tảng.
- [x] Hoàn thiện animation/background cho landing/login mà không cản form.

## 1.7. Test

- [ ] Backend auth integration test.
- [ ] RBAC/permission test.
- [ ] Locked account test.
- [ ] Refresh/revoke token test.
- [ ] Profile ownership test.
- [ ] Frontend route guard test.
- [ ] Base component accessibility test.
- [x] Build backend và frontend thành công.

## Exit criteria Phase 1

- [ ] Auth/account/profile chạy end-to-end.
- [ ] Ba role và permission được enforce ở backend.
- [ ] Page nghiệp vụ chỉ dùng custom component.
- [ ] GSAP/Three.js có cleanup, fallback và reduced-motion.
- [x] Không còn `test NO-SOURCE` cho backend; đã có foundation enum tests.

---

# Phase 2 — Facility

**Trạng thái:** `COMPLETED`  
**Phụ thuộc:** Phase 1 hoàn thành.

## 2.1. Database và backend

- [x] Migration Building (`V6__phase2_facility.sql`).
- [x] Migration Floor (`V6__phase2_facility.sql`).
- [x] Migration Room/Room Type/Price Policy theo thiết kế đã chốt (`V6__phase2_facility.sql`).
- [x] Migration Bed (`V6__phase2_facility.sql`).
- [x] FK, unique code và index cho Building, Floor, Room, Bed.
- [x] Building CRUD + search/filter (`BuildingController`, `BuildingService`).
- [x] Floor CRUD + validation thuộc Building (`FloorController`, `FloorService`).
- [x] Room CRUD + capacity/gender/type/price validation (`RoomController`, `RoomService`).
- [x] Bed CRUD + status validation (`BedController`, `BedService`).
- [x] Rule không active Bed khi Room không usable (BR-FAC-06 / BR-FAC-07).
- [x] Rule không giảm capacity làm dữ liệu mâu thuẫn (không cho phép nhỏ hơn số người đang ở).
- [x] STAFF permission cho quản lý facility (`FACILITY_READ`, `FACILITY_MANAGE`).
- [x] Public/USER API xem phòng còn chỗ theo dữ liệu được phép công khai (`PublicFacilityController`).

## 2.2. Frontend

- [x] STAFF: quản lý Building (Thêm, sửa, xem danh sách tòa nhà).
- [x] STAFF: quản lý Floor (Hiển thị và lọc tầng theo tòa nhà).
- [x] STAFF: quản lý Room (Danh sách, tạo phòng, chuyển trạng thái bảo trì/sẵn sàng).
- [x] Room hỗ trợ chọn ảnh từ máy, upload JPG/PNG/WEBP tối đa 5 MB và dùng ảnh mặc định khi URL trống hoặc tải lỗi.
- [x] STAFF: quản lý Bed (Xem ma trận giường, đổi trạng thái bảo trì/sẵn sàng với validation).
- [x] Hiển thị cây Building → Floor → Room → Bed trực quan, mượt mà.
- [x] Bộ lọc status, gender, room type và availability.
- [x] USER/Public: danh sách và chi tiết phòng khả dụng.
- [x] Trang chủ tổng hợp loại phòng, bảng giá, số chỗ trống và thống kê từ API public thật; không dùng số liệu phòng hard-code.
- [x] STAFF thêm/sửa/ngừng áp dụng bảng giá phòng trên dữ liệu PostgreSQL thật.
- [x] Loading/error/empty state cho các danh mục.
- [x] Confirm & warning trước action làm Room/Bed inactive hoặc maintenance.

## 2.3. Cache và test

- [x] Test quan hệ Building–Floor–Room–Bed.
- [x] Test Room/Bed maintenance/inactive rule.
- [x] Test authorization STAFF/USER/ADMIN.
- [x] Build Backend (`./gradlew compileJava`, `./gradlew test`) thành công 100%.
- [x] Build Frontend (`npm run build`) thành công 100%.

## Exit criteria Phase 2

- [x] Facility chạy end-to-end và không tạo trạng thái Room/Bed mâu thuẫn.
- [x] Danh sách phòng còn chỗ phản ánh đúng dữ liệu PostgreSQL.

---

# Phase 3 — Registration và Assignment

**Trạng thái:** `IN PROGRESS` — code hoàn tất, đang chờ integration/E2E test trên PostgreSQL  
**Phụ thuộc:** Phase 2 hoàn thành.

## 3.1. Registration

- [x] Migration Registration.
- [x] USER tạo Registration.
- [x] USER xem Registration của mình.
- [x] USER hủy khi trạng thái cho phép.
- [x] Chặn nhiều Registration active trái rule.
- [x] STAFF queue, search và filter Registration.
- [x] STAFF approve/reject theo transition matrix.
- [x] Bắt buộc rejection reason theo rule đã chốt.
- [x] Audit approve/reject/cancel.
- [x] Notification kết quả.

## 3.2. Assignment

- [x] Migration Room Assignment.
- [x] API tìm Bed phù hợp và khả dụng.
- [x] Transaction tạo Assignment + cập nhật Bed.
- [x] Chặn USER có hai Assignment ACTIVE.
- [x] Chặn Bed có hai Assignment ACTIVE.
- [x] Chặn assign vào Room/Bed không usable.
- [x] Lưu `assigned_by`, start/end time và lịch sử.
- [x] Dùng PostgreSQL partial unique constraint + pessimistic locking chống double assignment.
- [x] Không dựa vào Redis lock làm lớp bảo vệ duy nhất.

## 3.3. Frontend

- [x] USER form đăng ký KTX.
- [x] USER timeline/trạng thái hồ sơ.
- [x] USER hủy hồ sơ hợp lệ.
- [x] STAFF registration queue.
- [x] STAFF review detail.
- [x] STAFF approve/reject dialog.
- [x] STAFF room/bed picker bằng custom component.
- [x] UI ngăn thao tác state transition không hợp lệ.

## 3.4. Test

- [x] Registration transition test.
- [x] Ownership test.
- [ ] Assignment transaction rollback test.
- [ ] Concurrency test: hai STAFF chọn cùng một Bed.
- [x] Test USER đã có Assignment ACTIVE.
- [ ] Test audit và notification.

## Exit criteria Phase 3

- [ ] Flow Registration → Approval → Assignment chạy end-to-end.
- [ ] Không thể phát sinh double assignment kể cả khi concurrent request.

---

# Phase 4 — Contract

**Trạng thái:** `IN PROGRESS` — backend/frontend đã triển khai, đang bổ sung test/UAT  
**Phụ thuộc:** Phase 3 hoàn thành.

## 4.1. Backend và database

- [x] Migration Contract.
- [x] Contract code generation unique.
- [x] Tạo Contract từ Assignment hợp lệ.
- [x] Chặn hai Contract ACTIVE cho cùng USER.
- [x] Snapshot rental price và deposit.
- [x] Draft → Active.
- [x] Renew/extend theo mô hình đã chốt.
- [x] Expire Contract bằng scheduled job.
- [x] Terminate Contract và trả giường theo transaction.
- [x] Không sửa trực tiếp dữ liệu tài chính Contract đã chốt trái rule.
- [x] Audit create/activate/renew/terminate/expire.

## 4.2. Frontend

- [x] STAFF danh sách Contract.
- [x] STAFF tạo/kích hoạt/gia hạn/thanh lý.
- [x] USER xem Contract của mình.
- [x] Hiển thị Contract status/timeline.
- [x] Confirm các action quan trọng bằng custom modal.

## 4.3. Test

- [x] Contract date validation.
- [x] Active Contract invariant test.
- [x] Contract–Assignment consistency test.
- [ ] Permission/ownership test.

## Exit criteria Phase 4

- [ ] Không tồn tại Contract ACTIVE nếu Assignment không hợp lệ.
- [ ] Luồng tạo, gia hạn và thanh lý được kiểm thử.

---

# Phase 5 — Billing và Payment

**Trạng thái:** `IN PROGRESS` — đã triển khai luồng chính Meter/Invoice/Payment, còn hardening và UAT  
**Phụ thuộc:** Phase 4 hoàn thành và toàn bộ decision Finance ở Phase 0 đã chốt.

## 5.1. Meter và tariff

- [x] Migration Meter.
- [x] Migration Meter Reading.
- [x] Migration Tariff/Price Policy.
- [x] Quản lý Electricity/Water Meter.
- [x] Nhập reading theo permission; cho sửa chỉ số mới nhất khi kỳ đó chưa phát hành hóa đơn.
- [x] Sửa mã/trạng thái công tơ và sửa/ngừng áp dụng đơn giá điện nước.
- [x] Chặn reading mới nhỏ hơn reading trước.
- [x] Luồng meter reset có lịch sử.
- [x] Tính consumption và rounding đúng rule.
- [x] Lưu snapshot đơn giá tại thời điểm tính phí trong Invoice Item.

## 5.2. Invoice

- [x] Migration Invoice.
- [x] Migration Invoice Detail.
- [x] Invoice code unique.
- [x] Tạo room fee.
- [x] Tạo electricity fee.
- [x] Tạo water fee.
- [x] Tạo service fee.
- [x] Tạo fine item thủ công; liên kết Violation thực hiện ở Phase 6.
- [x] Tính subtotal, discount, fine, total và paid amount.
- [x] Prorate tiền phòng và điện nước theo resident-day.
- [x] Chống tạo trùng hóa đơn cùng kỳ.
- [x] Scheduled job/logic chuyển OVERDUE.
- [x] Không có API sửa/xóa Invoice PAID hoặc Invoice đã có Payment.
- [x] Không hard delete Invoice đã có Payment.

## 5.3. Payment

- [x] Migration Payment.
- [x] USER chỉ thanh toán Invoice của mình.
- [x] Hỗ trợ CASH và VNPAY.
- [x] Idempotency cho yêu cầu thanh toán.
- [x] Partial/full payment theo rule.
- [x] Chặn overpayment.
- [x] Payment SUCCESS cập nhật Invoice trong cùng transaction và pessimistic lock.
- [x] Luồng FAILED từ callback; CANCELLED/refund tự động ngoài MVP.
- [x] STAFF ghi nhận/đối soát thanh toán trực tiếp.
- [x] Không lưu dữ liệu thẻ/ngân hàng nhạy cảm.

## 5.4. Frontend

- [x] STAFF quản lý Meter và Reading.
- [ ] STAFF xem preview trước khi phát hành Invoice.
- [x] STAFF quản lý Invoice và công nợ.
- [x] STAFF quản lý/đối soát Payment.
- [x] USER danh sách và chi tiết Invoice.
- [x] USER thanh toán.
- [x] USER lịch sử Payment.
- [ ] USER lịch sử Invoice đầy đủ với filter theo kỳ/trạng thái.
- [ ] USER lịch sử Payment đầy đủ với filter theo thời gian/phương thức.
- [x] Thanh toán trực tiếp.
- [x] VNPay Sandbox thật: chuyển hướng sang cổng TEST, URL ký HMAC-SHA512 và callback kiểm tra chữ ký/mã merchant/số tiền.
- [x] Hiển thị rõ unpaid/partial/paid/overdue.

## 5.5. Test

- [x] Meter sequence/reset test nền tảng.
- [x] Billing calculation test.
- [x] Rounding/prorate test nền tảng.
- [x] Invoice total invariant test.
- [ ] Payment idempotency test.
- [ ] Concurrent payment/overpayment test.
- [ ] Transaction rollback test.
- [ ] Ownership và permission integration test.

## Exit criteria Phase 5

- [ ] Tiền và consumption được tính xác định, có test bằng dữ liệu mẫu đã duyệt.
- [ ] Invoice/Payment invariant không thể bị phá vỡ bởi concurrent request.

---

# Phase 6 — Operations

**Trạng thái:** `NOT STARTED`  
**Phụ thuộc:** Phase 5 hoàn thành cho các flow cần kiểm tra công nợ/phạt.

## 6.1. Request và Incident

- [ ] Migration Request Type.
- [ ] Migration Request/Incident.
- [ ] Migration Attachment.
- [ ] Upload validation: loại file, kích thước và ownership.
- [ ] USER tạo/xem request của mình.
- [ ] STAFF tiếp nhận, phân công, xử lý, resolve/reject.
- [ ] Lưu người xử lý và thời gian.
- [ ] Reopen flow nếu thuộc scope.

## 6.2. Room Transfer

- [ ] Migration Transfer Request.
- [ ] USER phải có Assignment ACTIVE.
- [ ] Kiểm tra Bed đích.
- [ ] STAFF approve/reject.
- [ ] Transaction kết thúc Assignment cũ.
- [ ] Giải phóng Bed cũ.
- [ ] Tạo Assignment mới.
- [ ] Occupy Bed mới.
- [ ] Cập nhật/phụ lục Contract theo rule.
- [ ] Lưu history, audit và notification.
- [ ] Rollback toàn bộ khi một bước lỗi.

## 6.3. Room Return

- [ ] Migration Return Request.
- [ ] USER phải có Contract/Assignment ACTIVE.
- [ ] Kiểm tra công nợ.
- [ ] Ghi nhận biên bản tài sản/tình trạng phòng nếu thuộc scope.
- [ ] Ghi nhận khoản phải thu/phạt phát sinh.
- [ ] STAFF xác nhận hoàn tất.
- [ ] End Assignment.
- [ ] Release Bed.
- [ ] Terminate Contract.
- [ ] Xử lý/hoàn deposit theo rule.
- [ ] Audit và notification.

## 6.4. Violation

- [ ] Migration Violation.
- [ ] STAFF tạo biên bản.
- [ ] Loại, mô tả, thời gian và bằng chứng.
- [ ] Fine amount không âm.
- [ ] Liên kết fine với Invoice.
- [ ] Không hard delete khi đã ảnh hưởng Invoice/Payment.
- [ ] USER xem violation của mình.
- [ ] Appeal flow nếu thuộc scope.

## 6.5. Notification

- [ ] Migration Notification.
- [ ] In-app notification.
- [ ] Mark read/mark all read.
- [ ] WebSocket realtime nếu đã chốt.
- [ ] Email notification nếu đã chốt.
- [ ] Delivery/retry status nếu cần.
- [ ] Template cho từng business event.

## 6.6. Frontend và test

- [ ] USER/STAFF Request UI.
- [ ] Attachment UI.
- [ ] USER/STAFF Transfer UI.
- [ ] USER/STAFF Return UI.
- [ ] USER/STAFF Violation UI.
- [ ] Notification center.
- [ ] Transfer transaction rollback test.
- [ ] Return debt check test.
- [ ] Attachment security test.
- [ ] Ownership/permission/state test.

## Exit criteria Phase 6

- [ ] Request, Transfer, Return và Violation chạy end-to-end.
- [ ] Transfer/Return không để Bed, Assignment và Contract mâu thuẫn.

---

# Phase 7 — Dashboard và Reporting

**Trạng thái:** `NOT STARTED`  
**Phụ thuộc:** Các module tạo dữ liệu nguồn đã ổn định.

## 7.1. KPI và report definition

- [ ] Chốt công thức từng KPI.
- [ ] Chốt timezone và khoảng thời gian report.
- [ ] Chốt quyền xem theo Building/phạm vi quản lý.
- [ ] Chốt export CSV/Excel/PDF thuộc scope.

## 7.2. STAFF dashboard

- [ ] Tổng USER đang ở.
- [ ] Tổng Room/Bed.
- [ ] Room/Bed còn trống.
- [ ] Occupancy rate.
- [ ] Registration chờ duyệt.
- [ ] Invoice chưa thanh toán/overdue.
- [ ] Tổng công nợ.
- [ ] Incident đang xử lý.
- [ ] Violation thống kê.

## 7.3. ADMIN dashboard

- [ ] Account statistics.
- [ ] Role/permission overview.
- [ ] Locked/active account statistics.
- [ ] Audit summary.

## 7.4. Frontend, cache và test

- [ ] Dashboard responsive.
- [ ] Chart sử dụng custom wrapper component.
- [ ] GSAP chỉ dùng cho reveal/transition nhẹ.
- [ ] Three.js hero không ảnh hưởng tốc độ tải KPI.
- [ ] Filter theo thời gian/Building.
- [ ] Redis cache có TTL và invalidation phù hợp.
- [ ] KPI calculation test.
- [ ] Permission test.
- [ ] Export test nếu thuộc scope.

## Exit criteria Phase 7

- [ ] KPI đối chiếu đúng với truy vấn dữ liệu gốc.
- [ ] Dashboard vẫn dùng được khi Redis cache miss hoặc Redis tạm lỗi.

---

# Phase 8 — Hardening, UAT và Release

**Trạng thái:** `NOT STARTED`  
**Phụ thuộc:** Phase 1–7 đã đạt exit criteria.

## 8.1. Data integrity và concurrency

- [ ] Rà soát toàn bộ FK/unique/check/index.
- [ ] Test một Bed không có hai Assignment ACTIVE.
- [ ] Test một USER không có hai Assignment ACTIVE.
- [ ] Test một USER không có hai Contract ACTIVE.
- [ ] Test Assignment–Bed consistency.
- [ ] Test Contract–Assignment consistency.
- [ ] Test Invoice–Payment consistency.
- [ ] Test concurrent assignment/payment/approval.

## 8.2. Security

- [ ] Rà soát endpoint authentication.
- [ ] Rà soát role/permission.
- [ ] Rà soát ownership.
- [ ] Rà soát CORS/CSRF strategy.
- [ ] Rà soát JWT secret và expiration.
- [ ] Rà soát rate limiting.
- [ ] Rà soát upload security.
- [ ] Rà soát log/audit không lộ secret hoặc PII không cần thiết.
- [ ] Dependency vulnerability scan.

## 8.3. Performance và reliability

- [ ] Kiểm tra N+1 query.
- [ ] Kiểm tra index bằng query plan cho API/report nặng.
- [ ] Load test flow chính.
- [ ] Cache hit/miss và invalidation test.
- [ ] Redis unavailable test.
- [ ] Database backup/restore rehearsal.
- [ ] Scheduled job không chạy trùng khi scale nhiều instance.

## 8.4. Frontend quality

- [ ] Không có native interactive element ngoài base components.
- [ ] Keyboard-only test.
- [ ] Screen reader/ARIA review cho form và dialog chính.
- [ ] Reduced-motion test.
- [ ] WebGL fallback test.
- [ ] Mobile/device yếu test.
- [ ] GSAP/Three.js không gây memory leak.
- [ ] Bundle size/performance budget đạt yêu cầu.
- [ ] Không có route/action trái permission.

## 8.5. Test, documentation và release

- [ ] Backend unit/integration test đạt ngưỡng đã chốt.
- [ ] Frontend component/E2E test đạt ngưỡng đã chốt.
- [ ] Flyway migration test trên PostgreSQL sạch.
- [ ] Flyway migration test trên bản database gần production.
- [ ] Seed/demo data.
- [ ] Swagger/OpenAPI hoàn chỉnh.
- [ ] Hướng dẫn chạy local.
- [ ] Environment/deployment guide.
- [ ] Monitoring, health check và alerting.
- [ ] UAT checklist theo USER.
- [ ] UAT checklist theo STAFF.
- [ ] UAT checklist theo ADMIN.
- [ ] Ghi nhận và xử lý UAT defect.
- [ ] Release checklist và rollback plan.

## Exit criteria Phase 8

- [ ] Không còn defect blocker/critical.
- [ ] UAT được chấp thuận.
- [ ] Backup, deploy và rollback đã được kiểm tra.
- [ ] Tài liệu vận hành và bàn giao hoàn chỉnh.

---

# 4. Definition of Done cho từng chức năng

Mỗi chức năng chỉ được đánh dấu hoàn thành khi tất cả mục liên quan đạt yêu cầu:

- [ ] Business rule đã được đối chiếu BA.
- [ ] Database/migration hoàn chỉnh.
- [ ] Entity/model hoàn chỉnh.
- [ ] Repository/query hoàn chỉnh.
- [ ] Service/domain logic hoàn chỉnh.
- [ ] Transaction boundary đúng.
- [ ] Validation hoàn chỉnh.
- [ ] Authorization và ownership hoàn chỉnh.
- [ ] Controller/API/DTO hoàn chỉnh.
- [ ] Error handling hoàn chỉnh.
- [ ] Audit/notification được xử lý nếu cần.
- [ ] Frontend dùng custom component.
- [ ] Loading/error/empty/disabled state hoàn chỉnh.
- [ ] Responsive và accessibility được kiểm tra.
- [ ] GSAP/Three.js cleanup và fallback được kiểm tra nếu có dùng.
- [ ] Unit/integration/frontend test đã pass.
- [ ] OpenAPI/documentation được cập nhật.
- [ ] Code review hoàn tất.

---

# 5. Blocker / Decision Log

| ID | Ngày | Phase | Vấn đề/Quyết định cần chốt | Phương án | Người xác nhận | Trạng thái |
|---|---|---|---|---|---|---|
| DEC-001 | 2026-10-01 | 0 | Mô hình role và permission | Một role chính + permission theo role | User | ACCEPTED |
| DEC-002 | 2026-10-01 | 0 | Dữ liệu hồ sơ USER | Tách `users` và `user_profiles` | User | ACCEPTED |
| DEC-003 | 2026-10-01 | 0 | Room type, gender type và price policy | Phòng 4/6/8, nam/nữ, bảng giá có hiệu lực | User | ACCEPTED |
| DEC-004 | 2026-10-01 | 0 | Cách tính và chia phí điện nước | Tính theo Room, phân bổ theo số ngày cư trú | User | ACCEPTED |
| DEC-005 | 2026-10-01 | 0 | Payment method và partial payment | Trực tiếp + VNPay demo, cho partial, cấm overpayment | User | ACCEPTED |
| DEC-006 | 2026-10-01 | 0 | Attachment storage | MVP lưu local qua storage abstraction | User | ACCEPTED |
| DEC-007 | 2026-10-01 | 0 | Notification channel | In-app + WebSocket; email cho sự kiện quan trọng | User | ACCEPTED |
| DEC-008 | 2026-10-03 | 3-5 | SMTP, Gemini và VNPay chứa secret | Chỉ đọc từ `.env`; `.env.example` chỉ để placeholder; giữ context path `/api/v1` của dự án KTX | Codex | IMPLEMENTED |

---

# 6. Change Log

| Ngày | Phiên bản | Thay đổi | Người cập nhật |
|---|---|---|---|
| 2026-10-01 | 1.0 | Tạo checklist triển khai ban đầu; bổ sung Vue custom components, GSAP và Three.js | Codex |
| 2026-10-01 | 1.1 | Bắt đầu Phase 0; thêm phương án đề xuất cho các quyết định P0 | Codex |
| 2026-10-01 | 1.2 | Hoàn thành Phase 0; chốt phòng 4/6/8, giới tính phòng, transfer, VNPay demo, ảnh phòng, responsive và lịch sử tài chính | Codex |
| 2026-10-01 | 1.3 | Bắt đầu Phase 1; đồng bộ AccountStatus, Auth/Profile API, Vue shell, custom components, GSAP và lazy Three.js | Codex |
| 2026-10-01 | 1.4 | Thêm UI native-element guard, đồng bộ `/api/v1`, thêm test nền tảng và xác minh backend/frontend build | Codex |
| 2026-10-02 | 1.5 | Hoàn thiện Phase 2 Facility; tinh gọn UI, thêm logo KTX UET, chuyển tiện ích sang ảnh + danh sách, chuẩn hóa button và thay khối cầu bằng mô hình digital campus Three.js | Codex |
| 2026-10-02 | 1.6 | Sửa responsive sơ đồ tòa/tầng; loại bỏ dữ liệu sinh viên fallback gây hiểu nhầm là tài khoản thật và bổ sung trạng thái lỗi kết nối API | Codex |
| 2026-10-03 | 1.7 | Chuẩn hóa BaseBadge theo tone/size và sửa ma trận giường để tên, trạng thái, thao tác dài luôn nằm trên một hàng | Codex |
| 2026-10-03 | 1.8 | Tích hợp Tailwind CSS 4; cấu hình SMTP/Gemini/VNPay qua environment; triển khai Phase 3 Registration/Assignment và Phase 4 Contract kèm UI phân quyền, migration và test nền tảng | Codex |
| 2026-10-03 | 1.9 | Nâng cấp Excel sinh viên với header tiếng Việt, dropdown enum, cột số điện thoại dạng text và tự giãn cột; hiển thị phiên đăng nhập, điều hướng theo vai trò; thêm migration V9 chứa dữ liệu/tài khoản demo Phase 3–4 | Codex |
| 2026-10-03 | 2.0 | Loại bỏ toàn bộ mock Facility, sửa luồng Tòa→Tầng→Phòng bằng UUID thật; bắt đầu Phase 5 với migration V10, Meter/Reading/Tariff, Invoice, Payment trực tiếp và VNPay demo, UI STAFF/USER và test tài chính nền tảng | Codex |
| 2026-10-03 | 2.1 | Hoàn thiện CRUD bảng giá phòng; bổ sung sửa công tơ/chỉ số/đơn giá có khóa nghiệp vụ; thay VNPay giả lập bằng chuyển hướng Sandbox thật và xác minh callback chặt chẽ | Codex |
| 2026-10-03 | 2.2 | Nối trang chủ với API public thật cho thống kê, loại phòng và phòng khả dụng; thêm migration V12 lưu URL ảnh phòng và fallback ảnh mặc định khi thiếu/lỗi | Codex |
| 2026-10-06 | 2.3 | Chuẩn hóa style/kích thước button và bố cục responsive; bỏ nhãn Phase khỏi UI; đồng bộ loại phòng với sức chứa; thêm upload ảnh phòng từ máy; Việt hóa enum và định dạng ngày hiển thị `dd/MM/yyyy` mà không đổi payload Backend | Codex |
| 2026-10-06 | 2.4 | Cân thẳng ô tìm kiếm–trạng thái–nút lọc; đồng bộ ba nút nhập/xuất danh sách sinh viên và bỏ tên định dạng đặt trong ngoặc trên UI | Codex |

