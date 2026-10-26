# Hướng dẫn build PoolBooking

## Yêu cầu

- JDK 21
- Maven 3.9+ hoặc Maven Wrapper
- Node.js 18+ và npm
- SQL Server 2019+ / Express

## Database

Chạy lần lượt `database/pool_booking.sql` và `database/seed_data.sql` bằng SQL Server Management Studio. Nếu mật khẩu `sa` không phải `123`, đặt biến môi trường `DB_PASSWORD` trước khi chạy backend.

## Backend

Windows:

```powershell
cd backend
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd spring-boot:run
```

Hoặc nếu đã cài Maven:

```powershell
mvn clean package -DskipTests
java -jar target/pool-booking-1.0.0.jar
```

Backend chạy tại `http://localhost:8080`.

## Frontend

Mở terminal khác:

```powershell
cd frontend
npm install
npm run build
npm run dev
```

Frontend chạy tại `http://localhost:5173`.

## Tài khoản demo

```text
CUSTOMER: customer@pool.local / password
STAFF:    staff@pool.local    / password
ADMIN:    admin@pool.local    / password
```

Luồng demo: CUSTOMER đăng nhập → xem lịch → đặt vé → thanh toán mô phỏng → nhận QR → STAFF check-in → ADMIN xem dashboard.

## Thư mục cần giữ

```text
backend/
frontend/
database/
docs/
```

`demo/` là project Spring Initializr cũ, không dùng cho MVP mới. `.m2/` là Maven cache, có thể xóa thủ công để giải phóng dung lượng.
