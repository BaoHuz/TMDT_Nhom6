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

Mặc định local dùng SQL Server `localhost:1433`, tài khoản `sa`, mật khẩu `123`. Có thể ghi đè an toàn bằng biến môi trường:

```powershell
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="mat_khau_sql_server"
$env:JWT_SECRET="chuoi-bi-mat-jwt-dai-va-ngau-nhien"
```

## Chạy backend

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

| Vai trò | Email đăng nhập |
|---|---|
| CUSTOMER | `customer@pool.local` |
| STAFF | `staff@pool.local` |
| ADMIN | `admin@pool.local` |

Quản trị viên có thể tạo thêm tài khoản STAFF tại **Quản trị → Tài khoản & Phân quyền**. Nhân viên đăng nhập bằng email hoặc tên đăng nhập; hệ thống chuyển tới quầy check-in. Đơn đặt chỗ chờ thanh toán có hạn 15 phút, sau đó tự hết hạn và chỗ được trả về ca bơi.

### VNPay Sandbox

Đăng ký merchant sandbox để nhận `TMN_CODE` và `HASH_SECRET`, sau đó đặt biến môi trường trong terminal chạy backend:

```powershell
$env:VNPAY_TMN_CODE="ma_merchant"
$env:VNPAY_HASH_SECRET="chuoi_bi_mat"
$env:VNPAY_RETURN_URL="https://ten-mien-cong-khai/api/payments/vnpay/return"
$env:FRONTEND_URL="http://localhost:5173"
```

VNPay cần gọi được IPN tại `https://ten-mien-cong-khai/api/payments/vnpay/ipn`; cấu hình URL này trong portal merchant. Khi chạy local, dùng HTTPS tunnel trỏ về backend port `8080` và đặt cùng URL tunnel làm `VNPAY_RETURN_URL`. Không commit secret vào repo.

Nếu dùng database đã tạo trước, chạy `database/migration_payment_deadline.sql` và `database/add_pending_refund_status.sql` để cập nhật schema. Migration hoàn tiền tách số tiền/thời gian/mã yêu cầu khỏi dữ liệu hoàn thực tế; các yêu cầu `CHO_HOAN` cũ được chuyển sang các cột yêu cầu và các cột hoàn thực tế được để trống. Database cũ có schema lệch thì tạo lại bằng `database/pool_booking.sql` rồi nạp `database/seed_data.sql` (script tạo schema sẽ xóa các bảng demo hiện tại).

Chính sách hủy khách hàng: từ 24 giờ trước ca hoàn 100%; từ 6 đến dưới 24 giờ hoàn 50%; dưới 6 giờ không hoàn; trong ca không hoàn; sau khi ca kết thúc hoặc đã check-in thì không hủy. Tỷ lệ được tính lại lúc xác nhận hủy từ thời gian hiện hành và giờ bắt đầu ca hiện được lưu trong lịch; thời điểm bắt đầu được chụp lại trên đơn để tra soát nếu lịch đổi sau đó. Mọi thao tác hủy, check-in và cập nhật chỗ khóa ca trước rồi mới khóa đơn. Hủy ca do ADMIN ghi nhận yêu cầu hoàn 100% cho giao dịch đã thanh toán. Admin có thể gửi yêu cầu hoàn cho giao dịch VNPay từ chi tiết đơn; ứng dụng chỉ đánh dấu đã hoàn khi VNPay trả xác nhận thành công. Giao dịch mô phỏng không thể hoàn qua cổng VNPay. Trạng thái `DANG_XU_LY` cần được đối soát, không bấm lại nếu không rõ kết quả. Giao dịch VNPay cũ thiếu mã giao dịch và ngày thanh toán gốc sẽ không hiển thị thao tác hoàn tự động. Trạng thái đơn, thanh toán và hoàn tiền được theo dõi độc lập. `CHO_HOAN` lưu số tiền/thời gian/mã yêu cầu trên các cột riêng; mã `REF-REQ-*` là mã nội bộ. Cột hoàn thực tế chỉ ghi nhận giao dịch được VNPay xác nhận.

Dashboard tính doanh thu ròng bằng tổng giao dịch thành công trừ cả yêu cầu hoàn đang chờ và khoản hoàn đã xác nhận; hiển thị riêng số tiền chờ xử lý và số tiền đã hoàn thực tế. Vì vậy doanh thu giảm ngay khi tạo yêu cầu hoàn, nhưng khoản chờ xử lý vẫn chưa đồng nghĩa tiền đã được chuyển cho khách. Để dùng hoàn tự động, cấu hình `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` và tùy chỉnh `VNPAY_API_URL` nếu dùng môi trường khác Sandbox. Chạy lại `database/add_pending_refund_status.sql` để bổ sung metadata giao dịch VNPay và trạng thái xử lý hoàn vào database cũ.

## API chính

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/schedules`
- `POST /api/bookings`
- `GET /api/bookings/my`
- `GET /api/bookings/{id}/cancel-preview`
- `POST /api/bookings/{id}/cancel`
- `POST /api/payments`
- `POST /api/check-in`
- `GET /api/admin/dashboard`
- `GET /api/admin/bookings`

## Luồng demo

Đăng nhập CUSTOMER → xem ca bơi → đặt vé → thanh toán mô phỏng → nhận QR → đăng nhập STAFF → check-in QR → đăng nhập ADMIN → xem dashboard.

## Lưu ý

Đây là MVP phục vụ demo đồ án. QR hiện được sinh dưới dạng mã chuỗi `QR-{maDat}`. Lịch bơi, đơn đặt, thanh toán, check-in và dashboard trên frontend đều gọi REST API; không còn dùng mảng dữ liệu viết cứng trong `main.jsx`.
