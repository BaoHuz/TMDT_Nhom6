USE PoolBooking;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.thanh_toan', N'U') IS NULL
        THROW 51000, 'Table dbo.thanh_toan does not exist.', 1;

    IF COL_LENGTH(N'dbo.thanh_toan', N'so_tien_yeu_cau_hoan') IS NULL
        ALTER TABLE dbo.thanh_toan ADD so_tien_yeu_cau_hoan DECIMAL(18,2) NULL;
    IF COL_LENGTH(N'dbo.thanh_toan', N'thoi_gian_yeu_cau_hoan') IS NULL
        ALTER TABLE dbo.thanh_toan ADD thoi_gian_yeu_cau_hoan DATETIME2 NULL;
    IF COL_LENGTH(N'dbo.thanh_toan', N'ma_yeu_cau_hoan') IS NULL
        ALTER TABLE dbo.thanh_toan ADD ma_yeu_cau_hoan VARCHAR(60) NULL;
    IF COL_LENGTH(N'dbo.thanh_toan', N'vnpay_ma_giao_dich_goc') IS NULL
        ALTER TABLE dbo.thanh_toan ADD vnpay_ma_giao_dich_goc VARCHAR(100) NULL;
    IF COL_LENGTH(N'dbo.thanh_toan', N'vnpay_thoi_diem_giao_dich_goc') IS NULL
        ALTER TABLE dbo.thanh_toan ADD vnpay_thoi_diem_giao_dich_goc VARCHAR(14) NULL;
    IF COL_LENGTH(N'dbo.thanh_toan', N'ghi_chu_hoan') IS NULL
        ALTER TABLE dbo.thanh_toan ADD ghi_chu_hoan NVARCHAR(500) NULL;
    IF COL_LENGTH(N'dbo.dat_lich', N'thoi_diem_bat_dau_ap_dung') IS NULL
        ALTER TABLE dbo.dat_lich ADD thoi_diem_bat_dau_ap_dung DATETIME2 NULL;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.thanh_toan')
          AND name = N'CK_tt_trang_thai_hoan'
    )
        ALTER TABLE dbo.thanh_toan DROP CONSTRAINT CK_tt_trang_thai_hoan;

    ALTER TABLE dbo.thanh_toan
        ADD CONSTRAINT CK_tt_trang_thai_hoan
        CHECK (
            trang_thai_hoan IS NULL
            OR trang_thai_hoan IN ('CHO_HOAN', 'DANG_XU_LY', 'KHONG_HOAN', 'HOAN_MOT_PHAN', 'HOAN_TOAN')
        );

    IF EXISTS (
        SELECT 1 FROM sys.check_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.thanh_toan')
          AND name = N'CK_tt_so_tien_yeu_cau_hoan'
    )
        ALTER TABLE dbo.thanh_toan DROP CONSTRAINT CK_tt_so_tien_yeu_cau_hoan;

    ALTER TABLE dbo.thanh_toan
        ADD CONSTRAINT CK_tt_so_tien_yeu_cau_hoan
        CHECK (
            so_tien_yeu_cau_hoan IS NULL
            OR (so_tien_yeu_cau_hoan >= 0 AND so_tien_yeu_cau_hoan <= so_tien)
        );

    UPDATE dbo.thanh_toan
    SET so_tien_yeu_cau_hoan = COALESCE(so_tien_yeu_cau_hoan, so_tien_hoan),
        thoi_gian_yeu_cau_hoan = COALESCE(thoi_gian_yeu_cau_hoan, thoi_gian_hoan),
        ma_yeu_cau_hoan = COALESCE(
            ma_yeu_cau_hoan,
            ma_giao_dich_hoan,
            CONCAT('REF-REQ-', REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''))
        ),
        so_tien_hoan = NULL,
        thoi_gian_hoan = NULL,
        ma_giao_dich_hoan = NULL
    WHERE trang_thai_hoan = 'CHO_HOAN'
      AND COALESCE(so_tien_yeu_cau_hoan, so_tien_hoan, 0) > 0;

    IF NOT EXISTS (
        SELECT 1 FROM sys.indexes
        WHERE object_id = OBJECT_ID(N'dbo.thanh_toan')
          AND name = N'UX_thanh_toan_ma_yeu_cau_hoan'
    )
        CREATE UNIQUE INDEX UX_thanh_toan_ma_yeu_cau_hoan
            ON dbo.thanh_toan(ma_yeu_cau_hoan)
            WHERE ma_yeu_cau_hoan IS NOT NULL;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
