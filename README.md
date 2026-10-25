# PoolBooking – Hệ thống đặt lịch và quản lý dịch vụ hồ bơi

Project MVP cho môn Thương mại điện tử, gồm frontend React và backend Spring Boot. Hệ thống hỗ trợ khách hàng xem ca bơi, đặt vé, thanh toán mô phỏng, nhận mã QR; nhân viên check-in và quản trị viên xem báo cáo.

## Công nghệ

- Backend: Java 21, Spring Boot, Spring Web, Spring Data JPA, Spring Security, JWT, BCrypt, Maven.
- Frontend: React, Vite, Axios, React Router, CSS.
- Database: Microsoft SQL Server.

## Cấu trúc

```text
PoolBooking/
├── backend/       # REST API và nghiệp vụ Spring Boot
├── frontend/      # Giao diện React/Vite
├── database/      # Script tạo bảng và dữ liệu mẫu
├── docs/          # Tài liệu project
├── BUILD.md       # Hướng dẫn build nhanh
└── README.md
```

## Tạo database

Mở SQL Server Management Studio và chạy lần lượt:

```text
database/pool_booking.sql
database/seed_data.sql
```

Sau đó sửa thông tin kết nối trong `backend/src/main/resources/application.properties`:

```properties
spring.datasource.username=sa
spring.datasource.password=YOUR_PASSWORD
```

## Chạy backend

```powershell
cd backend
mvnw.cmd clean package -DskipTests
mvnw.cmd spring-boot:run
```

Hoặc nếu đã cài Maven:

```powershell
mvn clean package -DskipTests
java -jar target/pool-booking-1.0.0.jar
```

Backend chạy tại `http://localhost:8080`.

## Chạy frontend

Mở terminal khác:

```powershell
cd frontend
npm install
npm run build
npm run dev
```

Frontend chạy tại `http://localhost:5173`.

## Tài khoản demo

Mật khẩu mặc định: `password`

| Vai trò | Tài khoản |
|---|---|
| CUSTOMER | `customer` |
| STAFF | `staff` |
| ADMIN | `admin` |

## API chính

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/schedules`
- `POST /api/bookings`
- `GET /api/bookings/my`
- `PUT /api/bookings/{id}/cancel`
- `POST /api/payments`
- `POST /api/check-in`
- `GET /api/admin/dashboard`
- `GET /api/admin/bookings`

## Luồng demo

Đăng nhập CUSTOMER → xem ca bơi → đặt vé → thanh toán mô phỏng → nhận QR → đăng nhập STAFF → check-in QR → đăng nhập ADMIN → xem dashboard.

## Lưu ý

Đây là MVP phục vụ demo đồ án. QR hiện được sinh dưới dạng mã chuỗi `QR-{maDat}`; dữ liệu frontend hiện có một phần giao diện mock và lớp Axios/Auth để kết nối API backend.
