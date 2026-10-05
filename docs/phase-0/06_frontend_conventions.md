# Frontend Conventions

Trạng thái: **ACCEPTED**

## 1. Stack

- Vue 3 Composition API.
- TypeScript strict mode.
- Vite.
- Vue Router.
- Pinia.
- Axios hoặc typed fetch wrapper; chỉ chọn một.
- GSAP + ScrollTrigger cho motion.
- Three.js cho trải nghiệm 3D có kiểm soát.
- UI library có thể dùng nội bộ, nhưng page/feature chỉ gọi custom component của dự án.

## 2. Cấu trúc đề xuất

```text
src/
├── app/
│   ├── router/
│   ├── stores/
│   └── providers/
├── assets/
├── components/
│   ├── base/
│   ├── composite/
│   └── motion/
├── composables/
├── features/
│   ├── auth/
│   ├── facility/
│   ├── registration/
│   ├── occupancy/
│   ├── contract/
│   ├── billing/
│   └── operations/
├── layouts/
├── services/
│   ├── api/
│   └── generated/
├── styles/
│   ├── tokens.css
│   ├── reset.css
│   └── globals.css
├── types/
└── views/
```

## 3. Quy tắc tuyệt đối về native elements

Trong `views/**`, `features/**`, `layouts/**` và `components/composite/**`:

- Không dùng trực tiếp `button`.
- Không dùng trực tiếp `label`.
- Không dùng trực tiếp `input`, `select`, `textarea`.
- Không dùng trực tiếp `table` và các thẻ con.
- Không dùng trực tiếp `dialog`.
- Không tạo interactive element bằng `div`/`span` với click handler.

Các native element này chỉ được render trong `components/base/**`, nơi accessibility và hành vi semantic được triển khai một lần.

Ngoại lệ: structural semantic elements như `main`, `section`, `header`, `nav`, `article`, `div`, `span`, `p`, heading được phép dùng trong page khi chưa có component tương ứng. Nếu yêu cầu cuối cùng là cấm mọi native tag trong template nghiệp vụ, tạo thêm layout primitives như `AppStack`, `AppGrid`, `AppText`, `AppHeading`, `AppSection`.

## 4. Base component contract

Danh sách tối thiểu:

| Component | Native semantic nội bộ | Yêu cầu chính |
|---|---|---|
| `AppButton` | `button` | type, disabled, loading, icon, keyboard, focus |
| `AppLabel` | `label` | `for`, required indicator |
| `AppInput` | `input` | modelValue, validation, aria-describedby |
| `AppTextarea` | `textarea` | modelValue, counter, validation |
| `AppSelect` | `select` hoặc accessible listbox | keyboard navigation |
| `AppCheckbox` | `input[type=checkbox]` | checked/indeterminate |
| `AppRadioGroup` | radio semantics | keyboard group |
| `AppFormField` | wrapper | label, hint, error, IDs |
| `AppDialog` | `dialog` hoặc accessible primitive | focus trap, Escape, restore focus |
| `AppTable` | `table` | caption, headers, loading, empty, responsive |
| `AppPagination` | navigation/button | current page semantics |
| `AppFileUpload` | `input[type=file]` | type/size/error/progress |

Base component không chứa business rule.

## 5. Lint enforcement

- Bật Vue ESLint + TypeScript strict rules.
- Cấu hình rule cấm restricted elements trong mọi thư mục ngoài `components/base/**`.
- CI fail nếu page/feature dùng native interactive element trực tiếp.
- CI fail khi có TypeScript error, lint error hoặc test failure.

Pseudo-rule:

```text
no-restricted-syntax/native-elements:
  button, label, input, select, textarea, table, dialog
allowed:
  src/components/base/**
```

## 6. Design tokens

Ba lớp token:

1. Primitive: palette, font family, raw spacing.
2. Semantic: background, surface, text, border, success, warning, danger.
3. Component: button height, input border, dialog radius, table row height.

Không hard-code màu/spacing rải rác trong feature. Token hỗ trợ light/dark nếu dark mode thuộc scope.

## 7. State management

- Server state ưu tiên giữ gần feature/API query layer, không nhồi mọi dữ liệu vào Pinia.
- Pinia dùng cho auth session, user context, permission, global UI state.
- Không lưu access/refresh token vào state persisted không an toàn nếu đã chọn cookie strategy.
- Permission helper duy nhất, ví dụ `can('registration:approve')`.

## 8. Form và validation

- Backend validation là bắt buộc.
- Frontend validation phản hồi sớm và map được field error từ API.
- Submit button có loading và chặn double submit.
- Action tài chính/approve/reject/terminate yêu cầu confirm phù hợp.
- Không reset dữ liệu form khi API lỗi.

## 9. GSAP convention

- Tạo composable `useGsapContext` để scope animation theo component.
- Dùng `gsap.context()` và `context.revert()` khi unmount.
- ScrollTrigger phải `kill()` khi scope bị hủy.
- Không animate thuộc tính gây layout thrashing khi có thể dùng transform/opacity.
- Animation không trì hoãn hiển thị thông tin quan trọng.
- `prefers-reduced-motion: reduce` phải tắt animation không cần thiết.
- Table/form nghiệp vụ chỉ dùng transition ngắn, không dùng parallax hoặc scroll hijacking.

## 10. Three.js convention

- Bọc lifecycle trong `ThreeScene`/`useThreeScene`.
- Dừng render loop khi tab/page không visible nếu phù hợp.
- Dispose geometry, material, texture, render target và renderer.
- Remove resize/pointer listener khi unmount.
- Giới hạn device pixel ratio, đề xuất `min(window.devicePixelRatio, 2)`.
- Có WebGL capability check và static/CSS fallback.
- Lazy-load Three.js scene; không đưa toàn bộ Three.js vào initial bundle của portal nghiệp vụ.
- Không dùng Three.js làm cách duy nhất để truyền tải thông tin.

## 11. Performance budget đề xuất

- Route nghiệp vụ không tải Three.js nếu không sử dụng.
- Initial JS gzip của auth/business shell có budget được đo và chốt trong Phase 1.
- 3D asset nén và lazy-load.
- Không để animation kéo dài blocking interaction.
- Theo dõi FPS và memory trên thiết bị mục tiêu trước khi duyệt scene.

Thiết bị mục tiêu:

- Điện thoại từ viewport 360px.
- Máy tính bảng.
- Laptop/desktop.
- Hai phiên bản mới nhất của Chrome, Edge, Firefox và Safari tại thời điểm release.

## 12. Accessibility

- WCAG 2.1 AA làm mục tiêu.
- Mọi control có accessible name.
- Focus visible và thứ tự focus hợp lý.
- Dialog giữ focus và trả focus về trigger.
- Status/error động dùng live region phù hợp.
- Màu sắc không phải tín hiệu duy nhất.
- GSAP/Three.js tuân thủ reduced motion.

## 13. Role layouts

- Public: landing, room catalogue, auth.
- USER: registration, current room, contract, invoice/payment, requests, transfer/return, violation, notification.
- STAFF: operations workspace và dashboard.
- ADMIN: account, role/permission, config, audit và system dashboard.

Route metadata khai báo authentication/permission, nhưng backend vẫn enforce toàn bộ quyền.

USER invoice/payment pages phải hiển thị đầy đủ lịch sử, hỗ trợ filter theo billing period, trạng thái và thời gian thanh toán.

