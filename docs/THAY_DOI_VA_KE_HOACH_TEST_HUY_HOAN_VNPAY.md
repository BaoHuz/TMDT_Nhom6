# Thay đổi code và kế hoạch kiểm thử hủy lịch, hoàn tiền VNPay

**Đồ án:** Hệ thống nền tảng đặt lịch và quản lý dịch vụ hồ bơi công cộng
**Phạm vi:** Hủy lịch đặt bơi, tính tiền hoàn, gửi yêu cầu hoàn tiền qua VNPay Sandbox

## 1. Các thay đổi đã thực hiện

- Tách trạng thái hoàn tiền khỏi trạng thái thanh toán. Trạng thái hoàn gồm:
  KHONG_AP_DUNG, CHO_HOAN, DANG_XU_LY, HOAN_THANH, HOAN_THAT_BAI.
- Lưu thời điểm bắt đầu ca dùng làm căn cứ tính tỷ lệ hoàn vào đặt lịch.
- Tính mốc 24 giờ và 6 giờ theo thời lượng chính xác, không làm tròn xuống giờ trước khi chọn tỷ lệ.
- Lưu trạng thái lịch, trả chỗ và yêu cầu hoàn trong transaction CSDL.
- Chỉ gửi yêu cầu refund sau khi transaction CSDL đã commit.
- Tạo và lưu mã yêu cầu refund để các lần xử lý không tự tạo mã mới.
- Lưu mã giao dịch VNPay nhận từ IPN thanh toán.
- Thêm API refund VNPay có chữ ký; kiểm tra chữ ký phản hồi trước khi nhận kết quả.
- Đơn ADMIN hủy ca do sự cố cũng tạo yêu cầu hoàn 100% cho đặt lịch đã thanh toán, chưa check-in.
- API chi tiết đặt lịch và giao diện khách hàng hiển thị trạng thái hoàn riêng.
- Thêm migration CSDL và bài test Sandbox có điều kiện bật bằng biến môi trường.

Các file chính:

- backend/src/main/java/com/poolbooking/service/BookingService.java
- backend/src/main/java/com/poolbooking/service/AdminService.java
- backend/src/main/java/com/poolbooking/service/VnPayGateway.java
- backend/src/main/java/com/poolbooking/service/VnPayRefundListener.java
- backend/src/main/java/com/poolbooking/enums/TrangThaiHoanTien.java
- backend/src/test/java/com/poolbooking/VnPayRefundSandboxIT.java
- database/migration_cancellation_refund_status.sql

## 2. Trạng thái hiện tại và giới hạn

- mvn -q -DskipTests package đã biên dịch backend thành công.
- Test VnPayRefundSandboxIT đã được nhận diện, nhưng bị bỏ qua do chưa cấu hình thông tin/giao dịch Sandbox.
- Khi chạy toàn bộ test bằng Java 26, Mockito/Byte Buddy báo lỗi tạo mock. Dự án khai báo Java 21 trong backend/pom.xml; cần chạy lại với JDK 21 để đánh giá đúng bộ test hiện có.
- Code hiện lưu trạng thái DANG_XU_LY sau khi VNPay tiếp nhận yêu cầu. Chưa có tác vụ QueryDR/đối soát định kỳ để tự cập nhật trạng thái cuối thành HOAN_THANH hoặc HOAN_THAT_BAI. Vì vậy cần kiểm tra kết quả tại Sandbox; phản hồi nhận yêu cầu không đồng nghĩa tiền đã về tài khoản khách.

## 3. Chuẩn bị trước khi test

### 3.1. Chọn JDK 21

Kiểm tra phiên bản Java và Maven:

~~~powershell
java -version
mvn -version
~~~

Cả hai cần sử dụng JDK 21. Nếu máy có nhiều JDK, đặt JAVA_HOME trỏ tới JDK 21 và mở lại terminal trước khi chạy Maven.

### 3.2. Cập nhật CSDL

Với CSDL đang dùng, sao lưu trước rồi chạy một lần:

database/migration_cancellation_refund_status.sql

Với CSDL tạo mới, dùng database/pool_booking.sql. Backend đặt spring.jpa.hibernate.ddl-auto=validate, nên Hibernate sẽ báo lỗi nếu chưa có các cột/constraint mới.

### 3.3. Cấu hình VNPay Sandbox

Đặt thông tin bằng biến môi trường, không ghi TMN Code hoặc hash secret vào Git:

~~~text
VNPAY_TMN_CODE
VNPAY_HASH_SECRET
VNPAY_PAY_URL
VNPAY_RETURN_URL
VNPAY_API_URL
~~~

VNPAY_API_URL mặc định là:

~~~text
https://sandbox.vnpayment.vn/merchant_webapi/api/transaction
~~~

Nếu chạy local, cần URL IPN có thể truy cập từ internet qua HTTPS tunnel. Return URL dùng để hiển thị kết quả cho khách; IPN mới là thông báo server dùng để cập nhật thanh toán.

Khởi động SQL Server, backend trên cổng 8080 và frontend trên cổng 5173. Chuẩn bị tài khoản CUSTOMER và ADMIN để kiểm thử cả hai luồng.

## 4. Ma trận kiểm thử hủy lịch

| Mã | Kịch bản | Kết quả cần xác nhận |
|---|---|---|
| C01 | Đã thanh toán, hủy khi còn đúng 24 giờ | DA_HUY, hoàn 100%, tạo yêu cầu hoàn |
| C02 | Đã thanh toán, hủy khi còn 23 giờ 59 phút 59 giây | Hoàn 50% |
| C03 | Đã thanh toán, hủy khi còn đúng 6 giờ | Hoàn 50% |
| C04 | Đã thanh toán, hủy khi còn 5 giờ 59 phút 59 giây | Hoàn 0%, không gửi refund |
| C05 | Hủy đơn CHO_THANH_TOAN | Hủy đơn, trả chỗ, không tạo hoàn |
| C06 | Hủy đơn đã check-in hoặc hoàn tất | Từ chối; dữ liệu không đổi |
| C07 | Gửi yêu cầu hủy hai lần/double-click | Lần sau không tạo yêu cầu hoàn mới, không trả chỗ lần hai |
| C08 | Ca đổi giờ trước khi hủy | Tỷ lệ theo giờ bắt đầu hiện hành; giờ làm căn cứ được lưu |
| C09 | ADMIN hủy ca do sự cố | Đơn chưa check-in bị hủy; đơn đã thanh toán tạo hoàn 100% |
| C10 | Một bước lưu CSDL lỗi trong transaction | Rollback; không để lịch hủy mà trạng thái thanh toán/chỗ lệch |
| C11 | Khách và ADMIN thao tác hủy đồng thời | Không hoàn trùng, số chỗ không vượt sức chứa |
| C12 | Khách gọi hủy đơn của tài khoản khác | Bị từ chối quyền; đơn không thay đổi |

Với C01–C04, tạo ca có thời điểm bắt đầu tương ứng trước khi chạy test. Kiểm tra cả kết quả preview lẫn dữ liệu sau khi xác nhận hủy.

## 5. Ma trận kiểm thử VNPay

| Mã | Kịch bản | Kết quả cần xác nhận |
|---|---|---|
| V01 | Thanh toán Sandbox thành công, IPN hợp lệ | Thanh toán THANH_CONG, đặt lịch DA_XAC_NHAN, lưu mã giao dịch VNPay |
| V02 | Return URL báo thành công nhưng IPN chưa đến | Không xác nhận đơn chỉ dựa trên trình duyệt quay về |
| V03 | Hủy giao dịch VNPay đã thanh toán | Gửi POST refund, số tiền nhân 100, chữ ký hợp lệ |
| V04 | VNPay xác nhận tiếp nhận hoặc đang xử lý | Lưu DANG_XU_LY, không báo đã hoàn tiền xong |
| V05 | Sai chữ ký phản hồi hoặc VNPay từ chối | Không đánh dấu hoàn thành; giữ trạng thái để xử lý/đối soát |
| V06 | Timeout khi gọi refund | Lịch vẫn hủy; khoản hoàn không tự gửi lặp mù |
| V07 | VNPay phản hồi mã yêu cầu trùng 94 | Không tạo mã yêu cầu mới để thử lại tùy tiện |
| V08 | Tra cứu giao dịch ở Sandbox | Đối chiếu yêu cầu refund và trạng thái thực tế phía VNPay |

VNPay có API refund/querydr riêng. Mã phản hồi 00 xác nhận yêu cầu được tiếp nhận/xử lý ở tầng API; không nên tự diễn giải thành tiền đã được ghi có cho khách. Tham khảo [đặc tả chính thức VNPay](https://sandbox.vnpayment.vn/apis/docs/truy-van-hoan-tien/querydr%26refund.html).

## 6. Chạy test tự động

Từ thư mục backend, chạy bộ test:

~~~powershell
mvn test
~~~

Test VNPay Sandbox là test chủ động gửi yêu cầu hoàn trong môi trường Sandbox. Chỉ chạy khi đã có giao dịch Sandbox thành công phù hợp:

~~~powershell
mvn -Dtest=VnPayRefundSandboxIT test
~~~

Các biến bắt buộc cho test Sandbox:

~~~text
VNPAY_REFUND_TEST_ENABLED=true
VNPAY_TMN_CODE
VNPAY_HASH_SECRET
VNPAY_TEST_TXN_REF
VNPAY_TEST_AMOUNT_MINOR
VNPAY_TEST_PAYMENT_DATE
~~~

Biến tùy chọn:

~~~text
VNPAY_TEST_TRANSACTION_NO
VNPAY_API_URL
VNPAY_TEST_FULL_REFUND=false
~~~

- VNPAY_TEST_AMOUNT_MINOR là số tiền theo đơn vị VNPay, tức VND nhân 100.
- Mặc định test yêu cầu hoàn toàn phần. Hãy cung cấp đúng toàn bộ số tiền của giao dịch.
- Nếu đặt VNPAY_TEST_FULL_REFUND=false, số tiền cần nhỏ hơn số tiền thanh toán gốc để kiểm tra hoàn một phần.
- Bài test chỉ xác nhận API VNPay chấp nhận yêu cầu; cần tra cứu giao dịch để xác nhận quá trình xử lý tiếp theo.

## 7. Thứ tự đề xuất để nghiệm thu

1. Chạy migration trên CSDL thử nghiệm.
2. Chạy ứng dụng bằng JDK 21 và kiểm tra tạo đặt lịch/thanh toán/check-in vẫn hoạt động.
3. Chạy C01–C12, kiểm tra API response và các bản ghi trong dat_lich, thanh_toan, lich_dich_vu.
4. Tạo thanh toán VNPay Sandbox thành công, kiểm tra IPN lưu mã giao dịch.
5. Chạy hủy lịch đã thanh toán và quan sát yêu cầu refund được gửi sau commit.
6. Tra cứu giao dịch trên VNPay Sandbox, đối chiếu số tiền, mã giao dịch, trạng thái và thời gian.
7. Chạy test toàn bộ trên JDK 21; lưu log test và ảnh chụp giao dịch Sandbox làm minh chứng.
8. Bổ sung QueryDR hoặc quy trình ADMIN đối soát có kiểm soát trước khi tuyên bố tự động cập nhật trạng thái hoàn cuối cùng.
