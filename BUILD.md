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

ADMIN tạo tài khoản nhân viên tại **Quản trị → Tài khoản & Phân quyền**. Đơn chờ thanh toán hết hạn sau 15 phút và tự hoàn chỗ về lịch. Với database đã có từ trước, chạy `database/migration_payment_deadline.sql`; nếu schema cũ không tương thích thì tạo lại bằng `database/pool_booking.sql` và nạp `database/seed_data.sql` (việc tạo lại sẽ xóa dữ liệu cũ).

Thanh toán VNPay Sandbox cần `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`, `VNPAY_RETURN_URL` và `FRONTEND_URL`. Cấu hình IPN công khai `https://<domain>/api/payments/vnpay/ipn` trong merchant portal; khi chạy local, dùng HTTPS tunnel tới backend port 8080. Các giá trị merchant được cấp khi đăng ký sandbox.

## Thư mục cần giữ

```text
backend/
frontend/
database/
docs/
```

`demo/` là project Spring Initializr cũ, không dùng cho MVP mới. `.m2/` là Maven cache, có thể xóa thủ công để giải phóng dung lượng.
