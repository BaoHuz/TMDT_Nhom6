/*
    AquaFlow / PoolBooking - dữ liệu mẫu SQL Server
    Chạy sau database/pool_booking.sql

    Tài khoản demo:
    - customer / password
    - staff    / password
    - admin    / password

    BCrypt hash hợp lệ cho mật khẩu "password".
*/

USE PoolBooking;
GO

SET NOCOUNT ON;
GO

/* =========================================================
   1. TÀI KHOẢN
   ========================================================= */
INSERT INTO dbo.tai_khoan
    (ma_tk, ten_dang_nhap, email, mat_khau_bam, vai_tro, trang_thai)
VALUES
    ('TK-CUSTOMER', 'customer', 'customer@pool.local', '{bcrypt}$2a$10$CvfmI9FlxDnstnjoEsObBu2c3M4M7fZG8zEF5jXnU2IlDnkA1.KVS', 'CUSTOMER', 'HOAT_DONG'),
    ('TK-STAFF',    'staff',    'staff@pool.local',    '{bcrypt}$2a$10$CvfmI9FlxDnstnjoEsObBu2c3M4M7fZG8zEF5jXnU2IlDnkA1.KVS', 'STAFF',    'HOAT_DONG'),
    ('TK-ADMIN',    'admin',    'admin@pool.local',    '{bcrypt}$2a$10$CvfmI9FlxDnstnjoEsObBu2c3M4M7fZG8zEF5jXnU2IlDnkA1.KVS', 'ADMIN',    'HOAT_DONG');
GO

/* =========================================================
   2. KHÁCH HÀNG
   ========================================================= */
INSERT INTO dbo.khach_hang
    (ma_kh, ma_tk, ho_ten, dien_thoai, email, trang_thai)
VALUES
    ('KH001', 'TK-CUSTOMER', N'Nguyễn Văn A', '0900000001', 'customer@pool.local', 'HOAT_DONG');
GO

/* =========================================================
   3. HỒ BƠI
   ========================================================= */
INSERT INTO dbo.ho_boi
    (ma_ho_boi, ten_ho_boi, dia_chi, gio_mo_cua, gio_dong_cua, trang_thai)
VALUES
    ('HB01', N'Hồ Bơi Thảo Điền', N'Thảo Điền, TP. Hồ Chí Minh', '05:30', '21:00', 'HOAT_DONG'),
    ('HB02', N'Hồ Bơi Bình An',   N'Bình An, TP. Hồ Chí Minh',   '06:00', '20:30', 'HOAT_DONG');
GO

/* =========================================================
   4. KHU VỰC
   ========================================================= */
INSERT INTO dbo.khu_vuc
    (ma_khu_vuc, ma_ho_boi, ten_khu_vuc, suc_chua, trang_thai)
VALUES
    ('KV01', 'HB01', N'Hồ người lớn', 50, 'HOAT_DONG'),
    ('KV02', 'HB01', N'Hồ trẻ em',    25, 'HOAT_DONG'),
    ('KV03', 'HB02', N'Hồ tập luyện', 40, 'HOAT_DONG');
GO

/* =========================================================
   5. DỊCH VỤ
   ========================================================= */
INSERT INTO dbo.dich_vu
    (ma_dv, ten_dv, loai_dv, don_gia, thoi_luong_phut, trang_thai)
VALUES
    ('DV01', N'Vé bơi người lớn', 'VE_BOI',    50000,  90, 'DANG_BAN'),
    ('DV02', N'Vé bơi trẻ em',    'VE_BOI',    30000,  90, 'DANG_BAN'),
    ('DV03', N'Vé tập luyện',      'LUYEN_TAP', 40000, 120, 'DANG_BAN');
GO

/* =========================================================
   6. LỊCH DỊCH VỤ / CA BƠI
   Dữ liệu dùng ngày hiện tại và ngày kế tiếp để demo dễ dàng.
   ========================================================= */
DECLARE @hom_nay DATE = CAST(GETDATE() AS DATE);
DECLARE @ngay_mai DATE = DATEADD(DAY, 1, @hom_nay);

INSERT INTO dbo.lich_dich_vu
    (ma_lich, ma_dv, ma_khu_vuc, ngay, gio_bat_dau, gio_ket_thuc, suc_chua, so_cho_con, trang_thai)
VALUES
    ('L001', 'DV01', 'KV01', @hom_nay,  '06:00', '07:30', 40, 40, 'MO'),
    ('L002', 'DV02', 'KV02', @hom_nay,  '08:00', '09:30', 20, 20, 'MO'),
    ('L003', 'DV03', 'KV03', @hom_nay,  '16:30', '18:30', 30, 30, 'MO'),
    ('L004', 'DV01', 'KV01', @ngay_mai, '06:00', '07:30', 40, 40, 'MO'),
    ('L005', 'DV02', 'KV02', @ngay_mai, '08:00', '09:30', 20, 20, 'MO'),
    ('L006', 'DV03', 'KV03', @ngay_mai, '18:30', '20:30', 30, 30, 'MO');
GO

/*
    Không seed sẵn dat_lich / thanh_toan / check_in để luồng demo có thể tạo dữ liệu thật:
    tra cứu ca -> checkout -> thanh toán mô phỏng -> sinh QR -> check-in.
*/

PRINT N'Đã thêm dữ liệu mẫu cho AquaFlow / PoolBooking.';
GO
