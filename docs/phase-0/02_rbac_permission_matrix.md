# RBAC and Permission Matrix

Trạng thái: **ACCEPTED**

## 1. Mô hình

- Hệ thống chỉ có ba role: `USER`, `STAFF`, `ADMIN`.
- Mỗi account có đúng một role chính tại một thời điểm.
- Permission được gán cho role qua `role_permissions`.
- Không gán permission trực tiếp cho từng user trong MVP.
- ADMIN không tự động có permission vận hành STAFF.
- Ownership luôn được kiểm tra độc lập với permission.

## 2. Naming convention

```text
<resource>:<action>
```

Action chuẩn: `read`, `create`, `update`, `delete`, `approve`, `reject`, `assign`, `transfer`, `terminate`, `pay`, `export`, `manage`.

## 3. Permission matrix

| Permission | USER | STAFF | ADMIN | Ghi chú |
|---|:---:|:---:|:---:|---|
| `profile:read:self` | ✓ | ✓ | ✓ | Dữ liệu của chính mình |
| `profile:update:self` | ✓ | ✓ | ✓ | Không tự đổi role/status |
| `user:read` | - | ✓ | ✓ | STAFF đọc góc độ nghiệp vụ; ADMIN đọc account |
| `user:update` | - | ✓ | - | Chỉ profile nghiệp vụ, không đổi role |
| `account:manage` | - | - | ✓ | Tạo/khóa/mở khóa account |
| `role:manage` | - | - | ✓ | Không tạo role ngoài ba role chuẩn nếu chưa đổi BA |
| `permission:manage` | - | - | ✓ | Quản lý mapping role-permission |
| `facility:read` | ✓ | ✓ | - | USER chỉ thấy dữ liệu công khai |
| `facility:manage` | - | ✓ | - | Building/Floor/Room/Bed |
| `registration:create:self` | ✓ | - | - | |
| `registration:read:self` | ✓ | - | - | |
| `registration:cancel:self` | ✓ | - | - | Theo state transition |
| `registration:read` | - | ✓ | - | |
| `registration:approve` | - | ✓ | - | |
| `registration:reject` | - | ✓ | - | |
| `assignment:read:self` | ✓ | - | - | |
| `assignment:read` | - | ✓ | - | |
| `assignment:assign` | - | ✓ | - | |
| `assignment:transfer` | - | ✓ | - | |
| `contract:read:self` | ✓ | - | - | |
| `contract:read` | - | ✓ | - | |
| `contract:manage` | - | ✓ | - | Create/activate/renew/terminate |
| `meter:read:self` | ✓ | - | - | Chỉ dữ liệu liên quan Room đang ở |
| `meter:manage` | - | ✓ | - | |
| `invoice:read:self` | ✓ | - | - | |
| `invoice:read` | - | ✓ | - | |
| `invoice:manage` | - | ✓ | - | |
| `payment:create:self` | ✓ | - | - | Chỉ Invoice của mình |
| `payment:read:self` | ✓ | - | - | |
| `payment:read` | - | ✓ | - | |
| `payment:reconcile` | - | ✓ | - | |
| `request:create:self` | ✓ | - | - | |
| `request:read:self` | ✓ | - | - | |
| `request:manage` | - | ✓ | - | |
| `transfer:create:self` | ✓ | - | - | |
| `transfer:read:self` | ✓ | - | - | |
| `transfer:manage` | - | ✓ | - | |
| `return:create:self` | ✓ | - | - | |
| `return:read:self` | ✓ | - | - | |
| `return:manage` | - | ✓ | - | |
| `violation:read:self` | ✓ | - | - | |
| `violation:manage` | - | ✓ | - | |
| `notification:read:self` | ✓ | ✓ | ✓ | |
| `notification:manage` | - | ✓ | - | Gửi theo business event |
| `dashboard:staff` | - | ✓ | - | |
| `dashboard:admin` | - | - | ✓ | |
| `report:read` | - | ✓ | ✓ | ADMIN chỉ report hệ thống nếu không có quyền vận hành |
| `report:export` | - | ✓ | ✓ | |
| `audit:read` | - | - | ✓ | |
| `system-config:manage` | - | - | ✓ | |

## 4. Ownership rules

Permission có hậu tố `:self` luôn yêu cầu:

- `resource.user_id == authenticated_user.id`; hoặc
- quan hệ sở hữu được suy ra an toàn từ Invoice/Contract/Assignment hiện tại.

Không nhận `userId` từ client để quyết định ownership nếu có thể lấy từ access token.

## 5. Enforcement

- Controller kiểm tra coarse-grained permission bằng method security.
- Service kiểm tra ownership, state và domain invariant.
- Repository query của USER phải scope theo authenticated user.
- Frontend chỉ dùng permission để ẩn/khóa UI; backend vẫn là lớp bảo mật bắt buộc.
- Mọi hành động approve/reject/assign/transfer/terminate/pay phải audit.

## 6. Permission seed

- Seed ba role và permission bằng Flyway migration.
- Mapping permission được quản lý bằng code/migration ở MVP để tránh tự khóa hệ thống.
- UI quản lý permission chỉ bật sau khi có kiểm thử bảo vệ ADMIN cuối cùng.

