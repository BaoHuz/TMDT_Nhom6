/*
    AquaFlow / PoolBooking - SQL Server schema
    MVP: Hệ thống nền tảng đặt lịch và quản lý dịch vụ hồ bơi công cộng

    Phạm vi dữ liệu: 9 bảng cốt lõi
    1. tai_khoan
    2. khach_hang
    3. ho_boi
    4. khu_vuc
    5. dich_vu
    6. lich_dich_vu
    7. dat_lich
    8. thanh_toan
    9. check_in

    LƯU Ý:
    - Script này dùng cho môi trường phát triển/demo và có thể chạy lại.
    - Nếu các bảng đã tồn tại, script sẽ xóa chúng theo thứ tự an toàn rồi tạo lại.
    - Các quy tắc cần dữ liệu ở nhiều bảng (giờ mở cửa, sức chứa khu vực,
      quyền STAFF/ADMIN, chống overbooking) phải được kiểm tra thêm ở Service/transaction.
*/

IF DB_ID(N'PoolBooking') IS NULL
BEGIN
    CREATE DATABASE PoolBooking;
END;
GO

USE PoolBooking;
GO

/* =========================================================
   0. DROP TABLES (development/demo reset)
   ========================================================= */
IF OBJECT_ID(N'dbo.check_in', N'U') IS NOT NULL DROP TABLE dbo.check_in;
IF OBJECT_ID(N'dbo.thanh_toan', N'U') IS NOT NULL DROP TABLE dbo.thanh_toan;
IF OBJECT_ID(N'dbo.dat_lich', N'U') IS NOT NULL DROP TABLE dbo.dat_lich;
IF OBJECT_ID(N'dbo.lich_dich_vu', N'U') IS NOT NULL DROP TABLE dbo.lich_dich_vu;
IF OBJECT_ID(N'dbo.dich_vu', N'U') IS NOT NULL DROP TABLE dbo.dich_vu;
IF OBJECT_ID(N'dbo.khu_vuc', N'U') IS NOT NULL DROP TABLE dbo.khu_vuc;
IF OBJECT_ID(N'dbo.ho_boi', N'U') IS NOT NULL DROP TABLE dbo.ho_boi;
IF OBJECT_ID(N'dbo.khach_hang', N'U') IS NOT NULL DROP TABLE dbo.khach_hang;
IF OBJECT_ID(N'dbo.tai_khoan', N'U') IS NOT NULL DROP TABLE dbo.tai_khoan;
GO

/* =========================================================
   1. TÀI KHOẢN
   ========================================================= */
CREATE TABLE dbo.tai_khoan (
    ma_tk           VARCHAR(60)  NOT NULL,
    ten_dang_nhap   VARCHAR(60)  NOT NULL,
    email           VARCHAR(150) NOT NULL,
    mat_khau_bam    VARCHAR(255) NOT NULL,
    vai_tro         VARCHAR(20)  NOT NULL,
    trang_thai      VARCHAR(20)  NOT NULL CONSTRAINT DF_tai_khoan_trang_thai DEFAULT 'HOAT_DONG',
    tao_luc         DATETIME2    NOT NULL CONSTRAINT DF_tai_khoan_tao_luc DEFAULT SYSDATETIME(),

    CONSTRAINT PK_tai_khoan PRIMARY KEY (ma_tk),
    CONSTRAINT UQ_tai_khoan_ten_dang_nhap UNIQUE (ten_dang_nhap),
    CONSTRAINT UQ_tai_khoan_email UNIQUE (email),
    CONSTRAINT CK_tai_khoan_vai_tro
        CHECK (vai_tro IN ('CUSTOMER', 'STAFF', 'ADMIN')),
    CONSTRAINT CK_tai_khoan_trang_thai
        CHECK (trang_thai IN ('HOAT_DONG', 'KHOA'))
);
GO

/* =========================================================
   2. KHÁCH HÀNG
   ========================================================= */
CREATE TABLE dbo.khach_hang (
    ma_kh       VARCHAR(60)   NOT NULL,
    ma_tk       VARCHAR(60)   NULL,
    ho_ten      NVARCHAR(150) NOT NULL,
    dien_thoai  VARCHAR(20)   NULL,
    email       VARCHAR(150)  NULL,
    trang_thai  VARCHAR(20)   NOT NULL CONSTRAINT DF_khach_hang_trang_thai DEFAULT 'HOAT_DONG',

    CONSTRAINT PK_khach_hang PRIMARY KEY (ma_kh),
    CONSTRAINT FK_khach_hang_tai_khoan
        FOREIGN KEY (ma_tk) REFERENCES dbo.tai_khoan(ma_tk),
    CONSTRAINT CK_khach_hang_trang_thai
        CHECK (trang_thai IN ('HOAT_DONG', 'KHOA'))
);
GO

CREATE UNIQUE INDEX UX_khach_hang_ma_tk
    ON dbo.khach_hang(ma_tk)
    WHERE ma_tk IS NOT NULL;
GO

/* =========================================================
   3. HỒ BƠI
   ========================================================= */
CREATE TABLE dbo.ho_boi (
    ma_ho_boi      VARCHAR(60)   NOT NULL,
    ten_ho_boi     NVARCHAR(150) NOT NULL,
    dia_chi        NVARCHAR(300) NOT NULL,
    gio_mo_cua     TIME          NOT NULL,
    gio_dong_cua   TIME          NOT NULL,
    trang_thai     VARCHAR(20)   NOT NULL CONSTRAINT DF_ho_boi_trang_thai DEFAULT 'HOAT_DONG',

    CONSTRAINT PK_ho_boi PRIMARY KEY (ma_ho_boi),
    CONSTRAINT CK_ho_boi_gio_hoat_dong CHECK (gio_dong_cua > gio_mo_cua),
    CONSTRAINT CK_ho_boi_trang_thai
        CHECK (trang_thai IN ('HOAT_DONG', 'TAM_DONG', 'NGUNG_HOAT_DONG'))
);
GO

/* =========================================================
   4. KHU VỰC
   ========================================================= */
CREATE TABLE dbo.khu_vuc (
    ma_khu_vuc  VARCHAR(60)   NOT NULL,
    ma_ho_boi   VARCHAR(60)   NOT NULL,
    ten_khu_vuc NVARCHAR(120) NOT NULL,
    suc_chua    INT           NOT NULL,
    trang_thai  VARCHAR(20)   NOT NULL CONSTRAINT DF_khu_vuc_trang_thai DEFAULT 'HOAT_DONG',

    CONSTRAINT PK_khu_vuc PRIMARY KEY (ma_khu_vuc),
    CONSTRAINT FK_khu_vuc_ho_boi
        FOREIGN KEY (ma_ho_boi) REFERENCES dbo.ho_boi(ma_ho_boi),
    CONSTRAINT UQ_khu_vuc_ten UNIQUE (ma_ho_boi, ten_khu_vuc),
    CONSTRAINT CK_khu_vuc_suc_chua CHECK (suc_chua > 0),
    CONSTRAINT CK_khu_vuc_trang_thai
        CHECK (trang_thai IN ('HOAT_DONG', 'TAM_DONG', 'NGUNG_HOAT_DONG'))
);
GO

/* =========================================================
   5. DỊCH VỤ
   ========================================================= */
CREATE TABLE dbo.dich_vu (
    ma_dv            VARCHAR(60)   NOT NULL,
    ten_dv           NVARCHAR(150) NOT NULL,
    loai_dv          VARCHAR(30)   NOT NULL,
    don_gia          DECIMAL(18,2) NOT NULL,
    thoi_luong_phut  INT           NOT NULL,
    trang_thai       VARCHAR(20)   NOT NULL CONSTRAINT DF_dich_vu_trang_thai DEFAULT 'DANG_BAN',

    CONSTRAINT PK_dich_vu PRIMARY KEY (ma_dv),
    CONSTRAINT CK_dich_vu_don_gia CHECK (don_gia >= 0),
    CONSTRAINT CK_dich_vu_thoi_luong CHECK (thoi_luong_phut > 0),
    CONSTRAINT CK_dich_vu_trang_thai
        CHECK (trang_thai IN ('DANG_BAN', 'TAM_DUNG', 'NGUNG_BAN'))
);
GO

/* =========================================================
   6. LỊCH DỊCH VỤ / CA BƠI
   ========================================================= */
CREATE TABLE dbo.lich_dich_vu (
    ma_lich       VARCHAR(60) NOT NULL,
    ma_dv         VARCHAR(60) NOT NULL,
    ma_khu_vuc    VARCHAR(60) NOT NULL,
    ngay          DATE        NOT NULL,
    gio_bat_dau   TIME        NOT NULL,
    gio_ket_thuc  TIME        NOT NULL,
    suc_chua      INT         NOT NULL,
    so_cho_con    INT         NOT NULL,
    trang_thai    VARCHAR(20) NOT NULL CONSTRAINT DF_lich_dich_vu_trang_thai DEFAULT 'NHAP',
    row_version   BIGINT      NOT NULL CONSTRAINT DF_lich_dich_vu_row_version DEFAULT 0,

    CONSTRAINT PK_lich_dich_vu PRIMARY KEY (ma_lich),
    CONSTRAINT FK_lich_dv FOREIGN KEY (ma_dv) REFERENCES dbo.dich_vu(ma_dv),
    CONSTRAINT FK_lich_kv FOREIGN KEY (ma_khu_vuc) REFERENCES dbo.khu_vuc(ma_khu_vuc),
    CONSTRAINT CK_lich_gio CHECK (gio_ket_thuc > gio_bat_dau),
    CONSTRAINT CK_lich_suc_chua CHECK (suc_chua > 0),
    CONSTRAINT CK_lich_so_cho_con CHECK (so_cho_con BETWEEN 0 AND suc_chua),
    CONSTRAINT CK_lich_trang_thai
        CHECK (trang_thai IN ('NHAP', 'MO', 'HET_CHO', 'DONG'))
);
GO

/* =========================================================
   7. ĐẶT LỊCH
   - ma_ve_qr để NULL khi CHO_THANH_TOAN; chỉ sinh sau thanh toán thành công.
   - Các trường hủy/hoàn tiền được bổ sung theo TH4.
   ========================================================= */
CREATE TABLE dbo.dat_lich (
    ma_dat          VARCHAR(60)   NOT NULL,
    ma_kh           VARCHAR(60)   NOT NULL,
    ma_lich         VARCHAR(60)   NOT NULL,
    ma_ve_qr        VARCHAR(100)  NULL,
    ngay_dat        DATETIME2     NOT NULL CONSTRAINT DF_dat_lich_ngay_dat DEFAULT SYSDATETIME(),
    han_thanh_toan  DATETIME2     NOT NULL CONSTRAINT DF_dat_lich_han_thanh_toan DEFAULT DATEADD(MINUTE, 15, SYSDATETIME()),
    so_nguoi        INT           NOT NULL,
    don_gia_tai_dat DECIMAL(18,2) NOT NULL,
    tong_tien       DECIMAL(18,2) NOT NULL,
    trang_thai      VARCHAR(30)   NOT NULL CONSTRAINT DF_dat_lich_trang_thai DEFAULT 'CHO_THANH_TOAN',

    -- Bổ sung nghiệp vụ hủy / hoàn tiền
    ly_do_huy       NVARCHAR(255) NULL,
    thoi_gian_huy   DATETIME2     NULL,
    ty_le_hoan      DECIMAL(5,2)  NULL,
    so_tien_hoan    DECIMAL(18,2) NULL,

    CONSTRAINT PK_dat_lich PRIMARY KEY (ma_dat),
    CONSTRAINT FK_dat_kh FOREIGN KEY (ma_kh) REFERENCES dbo.khach_hang(ma_kh),
    CONSTRAINT FK_dat_lich FOREIGN KEY (ma_lich) REFERENCES dbo.lich_dich_vu(ma_lich),
    CONSTRAINT CK_dat_so_nguoi CHECK (so_nguoi > 0),
    CONSTRAINT CK_dat_don_gia CHECK (don_gia_tai_dat >= 0),
    CONSTRAINT CK_dat_tong_tien CHECK (tong_tien >= 0),
    CONSTRAINT CK_dat_tinh_tong_tien CHECK (tong_tien = don_gia_tai_dat * so_nguoi),
    CONSTRAINT CK_dat_ty_le_hoan CHECK (ty_le_hoan IS NULL OR ty_le_hoan IN (0, 50, 100)),
    CONSTRAINT CK_dat_so_tien_hoan CHECK (so_tien_hoan IS NULL OR so_tien_hoan >= 0),
    CONSTRAINT CK_dat_trang_thai
        CHECK (trang_thai IN (
            'CHO_THANH_TOAN',
            'DA_XAC_NHAN',
            'DA_HUY',
            'DA_CHECK_IN',
            'HOAN_TAT',
            'HET_HAN'
        ))
);
GO

-- SQL Server UNIQUE constraint chỉ cho một NULL, trong khi nhiều đơn CHO_THANH_TOAN
-- chưa có QR. Dùng filtered unique index để chỉ bắt UNIQUE khi QR đã được sinh.
CREATE UNIQUE INDEX UX_dat_lich_ma_ve_qr
    ON dbo.dat_lich(ma_ve_qr)
    WHERE ma_ve_qr IS NOT NULL;
GO

/* =========================================================
   8. THANH TOÁN
   - Cho phép nhiều lần thử thanh toán cho một đơn.
   - ma_giao_dich chỉ UNIQUE khi có giá trị.
   - Trường hoàn tiền gắn với giao dịch gốc, không tạo bảng mới.
   ========================================================= */
CREATE TABLE dbo.thanh_toan (
    ma_tt             VARCHAR(60)   NOT NULL,
    ma_dat            VARCHAR(60)   NOT NULL,
    thoi_diem         DATETIME2     NOT NULL CONSTRAINT DF_thanh_toan_thoi_diem DEFAULT SYSDATETIME(),
    so_tien           DECIMAL(18,2) NOT NULL,
    phuong_thuc       VARCHAR(30)   NOT NULL CONSTRAINT DF_thanh_toan_phuong_thuc DEFAULT 'MO_PHONG',
    ma_giao_dich      VARCHAR(100)  NULL,
    trang_thai        VARCHAR(20)   NOT NULL,

    -- Bổ sung nghiệp vụ hoàn tiền theo TH4
    trang_thai_hoan   VARCHAR(20)   NULL,
    so_tien_hoan      DECIMAL(18,2) NULL,
    thoi_gian_hoan    DATETIME2     NULL,
    ma_giao_dich_hoan VARCHAR(50)   NULL,

    CONSTRAINT PK_thanh_toan PRIMARY KEY (ma_tt),
    CONSTRAINT FK_tt_dat FOREIGN KEY (ma_dat) REFERENCES dbo.dat_lich(ma_dat),
    CONSTRAINT CK_tt_so_tien CHECK (so_tien > 0),
    CONSTRAINT CK_tt_trang_thai
        CHECK (trang_thai IN ('CHO_XU_LY', 'THANH_CONG', 'THAT_BAI', 'HOAN_TIEN')),
    CONSTRAINT CK_tt_trang_thai_hoan
        CHECK (trang_thai_hoan IS NULL OR trang_thai_hoan IN ('KHONG_HOAN', 'HOAN_MOT_PHAN', 'HOAN_TOAN')),
    CONSTRAINT CK_tt_so_tien_hoan
        CHECK (so_tien_hoan IS NULL OR (so_tien_hoan >= 0 AND so_tien_hoan <= so_tien))
);
GO

CREATE UNIQUE INDEX UX_thanh_toan_ma_giao_dich
    ON dbo.thanh_toan(ma_giao_dich)
    WHERE ma_giao_dich IS NOT NULL;
GO

CREATE UNIQUE INDEX UX_thanh_toan_ma_giao_dich_hoan
    ON dbo.thanh_toan(ma_giao_dich_hoan)
    WHERE ma_giao_dich_hoan IS NOT NULL;
GO

/* =========================================================
   9. CHECK-IN
   ========================================================= */
CREATE TABLE dbo.check_in (
    ma_ci             VARCHAR(60)   NOT NULL,
    ma_dat            VARCHAR(60)   NOT NULL,
    ma_tk_nhan_vien   VARCHAR(60)   NOT NULL,
    thoi_diem         DATETIME2     NOT NULL CONSTRAINT DF_check_in_thoi_diem DEFAULT SYSDATETIME(),
    ghi_chu           NVARCHAR(500) NULL,

    CONSTRAINT PK_check_in PRIMARY KEY (ma_ci),
    CONSTRAINT UQ_check_in_ma_dat UNIQUE (ma_dat),
    CONSTRAINT FK_ci_dat FOREIGN KEY (ma_dat) REFERENCES dbo.dat_lich(ma_dat),
    CONSTRAINT FK_ci_tk FOREIGN KEY (ma_tk_nhan_vien) REFERENCES dbo.tai_khoan(ma_tk)
);
GO

/* =========================================================
   INDEXES PHỤC VỤ TRA CỨU / BÁO CÁO
   ========================================================= */
CREATE INDEX IX_lich_ngay_khu_vuc
    ON dbo.lich_dich_vu(ngay, ma_khu_vuc, trang_thai)
    INCLUDE (ma_dv, gio_bat_dau, gio_ket_thuc, so_cho_con);
GO

CREATE INDEX IX_lich_dich_vu_ngay
    ON dbo.lich_dich_vu(ma_dv, ngay, trang_thai);
GO

CREATE INDEX IX_dat_khach_hang
    ON dbo.dat_lich(ma_kh, ngay_dat DESC)
    INCLUDE (ma_lich, tong_tien, trang_thai);
GO

CREATE INDEX IX_dat_lich_ca
    ON dbo.dat_lich(ma_lich, trang_thai);
GO

CREATE INDEX IX_tt_ma_dat
    ON dbo.thanh_toan(ma_dat, thoi_diem DESC)
    INCLUDE (so_tien, trang_thai, trang_thai_hoan, so_tien_hoan);
GO

PRINT N'Đã tạo xong CSDL PoolBooking với 9 bảng MVP.';
GO
