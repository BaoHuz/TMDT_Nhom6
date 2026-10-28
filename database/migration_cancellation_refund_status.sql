/* Run once on an existing PoolBooking database. */
IF COL_LENGTH('dbo.dat_lich', 'thoi_diem_bat_dau_tinh_hoan') IS NULL
    ALTER TABLE dbo.dat_lich ADD thoi_diem_bat_dau_tinh_hoan DATETIME2 NULL;
IF COL_LENGTH('dbo.thanh_toan', 'ma_giao_dich_vnpay') IS NULL
    ALTER TABLE dbo.thanh_toan ADD ma_giao_dich_vnpay VARCHAR(50) NULL;
GO

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_tt_trang_thai_hoan')
    ALTER TABLE dbo.thanh_toan DROP CONSTRAINT CK_tt_trang_thai_hoan;
GO
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_tt_trang_thai')
    ALTER TABLE dbo.thanh_toan DROP CONSTRAINT CK_tt_trang_thai;
GO
UPDATE dbo.thanh_toan SET trang_thai='THANH_CONG' WHERE trang_thai='HOAN_TIEN';
ALTER TABLE dbo.thanh_toan ADD CONSTRAINT CK_tt_trang_thai CHECK (trang_thai IN ('CHO_XU_LY','THANH_CONG','THAT_BAI'));
GO

/* Old labels were written before VNPAY confirmed the refund, so migrate to pending. */
UPDATE dbo.thanh_toan SET trang_thai_hoan = CASE trang_thai_hoan
    WHEN 'KHONG_HOAN' THEN 'KHONG_AP_DUNG'
    WHEN 'HOAN_TOAN' THEN 'DANG_XU_LY'
    WHEN 'HOAN_MOT_PHAN' THEN 'DANG_XU_LY'
    ELSE trang_thai_hoan END;
GO

ALTER TABLE dbo.thanh_toan ADD CONSTRAINT CK_tt_trang_thai_hoan
    CHECK (trang_thai_hoan IS NULL OR trang_thai_hoan IN ('KHONG_AP_DUNG','CHO_HOAN','DANG_XU_LY','HOAN_THANH','HOAN_THAT_BAI'));
GO
