# REST API Conventions

Trạng thái: **ACCEPTED**

## 1. Base URL và resource naming

```text
/api/v1
```

- Dùng danh từ số nhiều, kebab-case khi có nhiều từ.
- Không đưa động từ CRUD vào URL.
- Action nghiệp vụ dùng sub-resource/action endpoint rõ nghĩa.

Ví dụ:

```http
GET    /api/v1/rooms
POST   /api/v1/registrations
POST   /api/v1/registrations/{id}/approve
POST   /api/v1/assignments/{id}/end
POST   /api/v1/transfer-requests/{id}/approve
```

## 2. HTTP method và status

| Trường hợp | Method/status |
|---|---|
| Đọc | `GET 200` |
| Tạo resource | `POST 201` |
| Thay toàn bộ | `PUT 200` |
| Cập nhật một phần | `PATCH 200` |
| Xóa mềm/vô hiệu hóa | `DELETE 204` hoặc action endpoint theo nghiệp vụ |
| Validation lỗi | `400` |
| Chưa xác thực | `401` |
| Không đủ quyền/ownership | `403` |
| Không tìm thấy | `404` |
| State conflict/duplicate | `409` |
| Rate limit | `429` |

## 3. Success response

Giữ wrapper tương thích base hiện tại:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {
    "requestId": "uuid",
    "timestamp": "2026-10-01T04:00:00Z"
  }
}
```

Endpoint `204 No Content` không trả body.

## 4. Error response

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ASSIGNMENT_BED_NOT_AVAILABLE",
    "message": "Giường không còn khả dụng",
    "fieldErrors": [],
    "details": {}
  },
  "meta": {
    "requestId": "uuid",
    "timestamp": "2026-10-01T04:00:00Z"
  }
}
```

- `code` là stable machine-readable string.
- `message` có thể localize, frontend không branch logic bằng message.
- Không trả stack trace, SQL, secret hoặc thông tin nội bộ.

## 5. Pagination, filter và sort

```http
GET /api/v1/rooms?page=0&size=20&sort=code,asc&status=AVAILABLE&buildingId={uuid}
```

- `page` zero-based.
- `size` mặc định 20, tối đa 100.
- Chỉ cho sort theo allow-list.
- Search string được trim và giới hạn độ dài.

Page response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "sort": ["code,asc"]
}
```

## 6. Authentication và authorization

- Bearer access token trong `Authorization` header.
- Refresh token được xử lý theo auth contract riêng; production ưu tiên HttpOnly Secure cookie nếu frontend/backend cùng kiểm soát domain.
- Client không gửi user ID để thay cho identity từ token.
- Endpoint dùng permission; Service tiếp tục kiểm tra ownership/state.

## 7. Idempotency và concurrency

- Payment creation yêu cầu `Idempotency-Key`.
- Có thể yêu cầu idempotency cho approve/assign nếu UI dễ retry.
- Resource mutable quan trọng trả `version`; update gửi version hoặc `If-Match` khi áp dụng optimistic locking.
- Conflict trả `409` với error code cụ thể.

## 8. Date, time và money

- Instant: ISO-8601 UTC, ví dụ `2026-10-01T04:30:00Z`.
- Local date: `YYYY-MM-DD`.
- Billing month: `YYYY-MM` hoặc object year/month thống nhất.
- Money truyền dạng JSON number với tối đa 2 decimal hoặc string decimal; quyết định cuối cùng phải thống nhất OpenAPI và TypeScript generator.
- Currency trả rõ `VND` khi response có số tiền tổng hợp.

## 9. Validation

- Backend là nguồn validation bắt buộc.
- Chuẩn hóa field error thành `field`, `code`, `message`.
- Enum không hợp lệ trả `400`, không trả `500`.
- UUID không hợp lệ trả `400`.
- Unknown JSON field: đề xuất reject trong endpoint tài chính/nhạy cảm.

## 10. OpenAPI và compatibility

- Mọi endpoint public hoặc dùng bởi frontend phải có OpenAPI schema.
- DTO response không đổi breaking trong cùng `/v1` nếu chưa có migration plan.
- Frontend client được generate hoặc type-check từ OpenAPI.
- Deprecation phải có thời hạn và replacement rõ ràng.

## 11. Endpoint groups dự kiến

```text
/auth
/users
/profiles
/roles
/permissions
/buildings
/floors
/room-types
/rooms
/rooms/{roomId}/images
/beds
/registrations
/assignments
/contracts
/meters
/meter-readings
/tariffs
/invoices
/payments
/payments/vnpay-demo
/requests
/transfer-requests
/return-requests
/violations
/notifications
/reports
/audit-logs
```

