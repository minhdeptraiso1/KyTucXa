# 🏢 Hướng Dẫn Kéo Code (Pull/Clone) Và Khởi Chạy Dự Án Ký Túc Xá

Tài liệu này hướng dẫn chi tiết từng bước cho thành viên mới khi clone/pull mã nguồn dự án **Quản lý Ký Túc Xá (KTX Management System)** về máy cá nhân và thiết lập môi trường để chạy đầy đủ cả Backend và Frontend.

---

## 📌 1. Tổng Quan Kiến Trúc Dự Án

Dự án bao gồm 2 phần độc lập nằm trong cùng monorepo:

| Thành phần | Thư mục | Công nghệ chính | Cổng mặc định (Port) |
| :--- | :--- | :--- | :--- |
| **Backend** | `Base_java_spring_boot/` | Java 21, Spring Boot 3.5.1, Spring Security (JWT), PostgreSQL 16, Redis 7, Flyway | `8080` (Context: `/api/v1`) |
| **Frontend** | `Fe/` | Vue 3 (Composition API), TypeScript, Vite 7, Tailwind CSS, Pinia, Three.js, GSAP | `5173` |
| **Database** | Docker | PostgreSQL 16 (`appdb`) | `5432` |
| **Cache** | Docker | Redis 7 | `6379` |

---

## 💻 2. Yêu Cầu Môi Trường (Prerequisites)

Trước khi bắt đầu, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:

1. **Git**: [Tải Git](https://git-scm.com/)
2. **Java 21 JDK**: [Tải Eclipse Temurin JDK 21](https://adoptium.net/) hoặc Amazon Corretto 21
   - Kiểm tra bằng lệnh: `java -version` (phải hiển thị phiên bản 21)
3. **Node.js**: Phiên bản LTS khuyến nghị `v18.x` hoặc `v20.x` trở lên (kèm `npm`)
   - Kiểm tra bằng lệnh: `node -v` và `npm -v`
4. **Docker Desktop** (khuyến nghị dùng để bật nhanh PostgreSQL và Redis):
   - [Tải Docker Desktop](https://www.docker.com/products/docker-desktop/)
   - Nếu không dùng Docker, bạn cần cài trực tiếp PostgreSQL 16 và Redis Server trên máy.

---

## 📥 3. Clone Hoặc Cập Nhật Code Mới Nhất

### Trường hợp 1: Tải mới dự án (Clone)
Mở Terminal / PowerShell và chạy:
```bash
git clone https://github.com/minhdeptraiso1/KyTucXa.git
cd KyTucXa
```

### Trường hợp 2: Đã có sẵn repo, muốn kéo cập nhật mới (Pull)
```bash
git checkout main
git pull origin main
```

---

## ⚙️ 4. Hướng Dẫn Cấu Hình Và Chạy Backend (Spring Boot)

### Bước 4.1: Khởi động Database (PostgreSQL) và Redis bằng Docker

Mở terminal tại thư mục `Base_java_spring_boot`:
```bash
cd Base_java_spring_boot
docker-compose up -d
```
> **Ghi chú**: Lệnh này sẽ tự động tạo container PostgreSQL (user: `postgres`, pass: `081003`, database: `appdb`) tại port `5432` và Redis tại port `6379`.
> Bạn có thể kiểm tra trạng thái container bằng lệnh: `docker-compose ps`.

### Bước 4.2: Tạo file cấu hình môi trường `.env`

Trong thư mục `Base_java_spring_boot`:
1. Nhân bản file `.env.example` và đổi tên thành `.env`:
   - **Windows PowerShell**:
     ```powershell
     Copy-Item .env.example .env
     ```
   - **Linux / macOS**:
     ```bash
     cp .env.example .env
     ```

2. Kiểm tra các thông số trong file `.env` vừa tạo để khớp với Docker Compose:
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/appdb
   DB_USERNAME=postgres
   DB_PASSWORD=081003
   REDIS_HOST=localhost
   REDIS_PORT=6379
   JWT_SECRET=your_jwt_secret_here_min_32_chars_long_ktx_uet
   JWT_ACCESS_EXPIRATION=900
   JWT_REFRESH_EXPIRATION=7d
   CONTEXT_PATH=/api/v1
   APP_FRONTEND_URL=http://localhost:5173
   ```
   *(Các thông số Email và VNPay là tùy chọn, có thể giữ mặc định khi chạy dev)*.

### Bước 4.3: Khởi chạy Backend

Flyway sẽ tự động chạy migration các bảng cơ sở dữ liệu và seed dữ liệu mẫu khi backend khởi động lần đầu.

- **Trên Windows**:
  ```powershell
  .\gradlew.bat bootRun
  ```

- **Trên Linux / macOS**:
  ```bash
  chmod +x gradlew
  ./gradlew bootRun
  ```

- **Hoặc mở thư mục `Base_java_spring_boot` bằng IntelliJ IDEA**:
  - Chọn file `BaseV1Application.java` và nhấn nút **Run ▶️**.

### Bước 4.4: Kiểm tra Backend hoạt động
- **API Health Check**: `http://localhost:8080/api/v1/actuator/health` (trả về `{"status":"UP"}`)
- **Swagger / OpenAPI Documentation**: `http://localhost:8080/api/v1/swagger` (hoặc `http://localhost:8080/api/v1/swagger-ui/index.html`)

---

## 🎨 5. Hướng Dẫn Cấu Hình Và Chạy Frontend (Vue 3 + Vite)

Mở một cửa sổ Terminal mới (không tắt terminal đang chạy Backend).

### Bước 5.1: Di chuyển vào thư mục Frontend
```bash
cd Fe
```

### Bước 5.2: Tạo file cấu hình `.env` cho Frontend
1. Nhân bản file `.env.example` thành `.env`:
   - **Windows PowerShell**:
     ```powershell
     Copy-Item .env.example .env
     ```
   - **Linux / macOS**:
     ```bash
     cp .env.example .env
     ```
2. Nội dung file `Fe/.env` đảm bảo trỏ đúng vào API Backend:
   ```properties
   VITE_API_BASE_URL=http://localhost:8080/api/v1
   ```

### Bước 5.3: Cài đặt thư viện dependencies
```bash
npm install
```

### Bước 5.4: Khởi chạy Frontend Development Server
```bash
npm run dev
```

Sau khi chạy thành công, giao diện web sẽ sẵn sàng tại:
👉 **`http://localhost:5173`**

---

## 🔑 6. Tài Khoản Đăng Nhập Dùng Thử (Demo Accounts)

Hệ thống đã chuẩn bị sẵn các tài khoản demo (được tạo tự động qua migration `V2` và `V9`):

| Quyền hạn (Role) | Tên đăng nhập (`username`) | Mật khẩu (`password`) | Mô tả chức năng |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (ADMIN)** | `admin` | `123456` | Quản lý toàn bộ hệ thống, phân quyền, cấu hình |
| **Cán bộ quản lý (STAFF)** | `staff` | `123456` | Quản lý phòng, cơ sở vật chất, duyệt đơn đăng ký, hợp đồng, hóa đơn điện nước |
| **Sinh viên (USER)** | `svdemo001` | `123456` | Xem thông tin phòng, đăng ký ở, gửi phản ánh hỏng hóc, thanh toán hóa đơn |

---

## 🛠️ 7. Xử Lý Các Vấn Đề Thường Gặp (Troubleshooting)

### 1. Lỗi cổng bị chiếm dụng (Port already in use)
- **Port 5432 (PostgreSQL)**: Nếu trên máy bạn đã có sẵn PostgreSQL service đang chạy, hãy dừng dịch vụ đó hoặc đổi port mapping trong `docker-compose.yaml` (ví dụ `5433:5432`) và cập nhật lại `DB_URL` trong file `.env`.
- **Port 8080 hoặc 5173**: Kiểm tra và tắt các tiến trình đang chiếm cổng bằng `netstat -ano | findstr :8080` (Windows).

### 2. Lỗi Gradle: `Unsupported class file major version` hoặc `Java version error`
- Nguyên nhân: Bạn đang dùng Java cũ (Java 8/11/17) thay vì Java 21.
- Khắc phục: Cài đặt JDK 21 và cấu hình biến môi trường `JAVA_HOME` trỏ về JDK 21.

### 3. Lỗi kết nối Database `Connection refused`
- Kiểm tra xem Docker container PostgreSQL đã chạy chưa: `docker-compose ps`.
- Nếu container chưa chạy, gõ: `docker-compose up -d`.

### 4. Lỗi npm install hoặc không nhận diện TypeScript
- Thử xóa thư mục cache và cài đặt lại:
  ```bash
  cd Fe
  rm -rf node_modules package-lock.json
  npm install
  ```

---

## 🚀 8. Các Lệnh Git Khi Đẩy Code (Dành Cho Nhà Phát Triển)

Khi bạn muốn đưa code lên GitHub:
```bash
# 1. Kiểm tra trạng thái các file thay đổi (file .env và node_modules sẽ tự động được bỏ qua nhờ .gitignore)
git status

# 2. Thêm file vào staging
git add .

# 3. Tạo commit
git commit -m "Mô tả tính năng hoặc thay đổi của bạn"

# 4. Đẩy code lên nhánh chính
git push origin main
```
