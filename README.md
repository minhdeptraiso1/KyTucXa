# 🏢 KyTucXa - Hệ Thống Quản Lý Ký Túc Xá Sinh Viên

Hệ thống quản lý ký túc xá thông minh, tích hợp quản trị phòng ốc, cơ sở vật chất, hợp đồng lưu trú, đăng ký xét duyệt và hóa đơn dịch vụ điện nước.

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Dự Án

👉 Xem hướng dẫn chi tiết từng bước cho người mới (kéo code, cài đặt môi trường, cấu hình và chạy cả Backend & Frontend) tại:
**[📖 Hướng Dẫn Chạy Dự Án (readmepull.md)](./readmepull.md)**

---

## 🛠️ Công Nghệ Sử Dụng

- **Backend**: Spring Boot 3.5.1, Java 21, Spring Security (JWT), PostgreSQL 16, Redis 7, Flyway Migration, WebSocket, Swagger OpenAPI.
- **Frontend**: Vue 3 (Composition API), Vite 7, TypeScript, Tailwind CSS, Pinia, Three.js, GSAP.
- **Container**: Docker, Docker Compose.

---

## 📁 Cấu Trúc Dự Án

```
KyTucXa/
├── Base_java_spring_boot/   # Mã nguồn Backend (Spring Boot 3.5.1)
│   ├── src/                 # Controllers, Services, Models, Repositories, Migrations
│   ├── docker-compose.yaml  # Docker chạy PostgreSQL & Redis
│   ├── .env.example         # Template biến môi trường Backend
│   └── build.gradle         # Quản lý thư viện Gradle
├── Fe/                      # Mã nguồn Frontend (Vue 3 + Vite)
│   ├── src/                 # Components, Views, Layouts, Pinia Stores, Assets
│   ├── .env.example         # Template biến môi trường Frontend
│   └── package.json         # Quản lý dependencies npm
├── readmepull.md            # Tài liệu chi tiết hướng dẫn kéo code và chạy dự án
└── .gitignore               # Cấu hình bỏ qua các file rác, nhạy cảm và build artifacts
```

---

## 👥 Tài Khoản Trải Nghiệm Mặc Định

| Role | Username | Password |
| :--- | :--- | :--- |
| **Quản trị viên (ADMIN)** | `admin` | `123456` |
| **Cán bộ quản lý (STAFF)** | `staff` | `123456` |
| **Sinh viên (USER)** | `svdemo001` | `123456` |
