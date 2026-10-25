# Hướng dẫn build PoolBooking

## Yêu cầu

- JDK 21
- Maven 3.9+ hoặc Maven Wrapper
- Node.js 18+ và npm
- SQL Server 2019+ / Express

## Database

Chạy lần lượt `database/pool_booking.sql` và `database/seed_data.sql` bằng SQL Server Management Studio. Lưu ý: `pool_booking.sql` xóa và tạo lại các bảng, vì vậy chỉ chạy trên database demo/dev hoặc sau khi đã sao lưu dữ liệu cần giữ. Sau đó sửa username/password trong `backend/src/main/resources/application.properties`.

Nếu đang dùng database cũ, cột `dbo.dich_vu.ten_dv` phải là `NVARCHAR(150)` để lưu được tên tiếng Việt. Có thể chuyển cột mà không xóa dữ liệu bằng lệnh:

```sql
ALTER TABLE dbo.dich_vu
ALTER COLUMN ten_dv NVARCHAR(150) NOT NULL;
```

## Backend

Windows:

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
CUSTOMER: customer / password
STAFF:    staff    / password
ADMIN:    admin    / password
```

Nếu đã chạy seed data trước đó, cập nhật mật khẩu demo hiện có bằng câu lệnh sau trong SQL Server Management Studio:

```sql
USE PoolBooking;
UPDATE dbo.tai_khoan
SET mat_khau_bam = '{bcrypt}$2a$10$iHnuvcu3yObLcSDyzML3ouhYEDFsMlL9aEsJCUVm8JLVt0rsMfufW'
WHERE ten_dang_nhap IN ('customer', 'staff', 'admin');
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
