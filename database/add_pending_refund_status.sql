USE PoolBooking;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.thanh_toan', N'U') IS NULL
        THROW 51000, 'Table dbo.thanh_toan does not exist.', 1;

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
            OR trang_thai_hoan IN ('CHO_HOAN', 'KHONG_HOAN', 'HOAN_MOT_PHAN', 'HOAN_TOAN')
        );

    UPDATE dbo.thanh_toan
    SET thoi_gian_hoan = COALESCE(thoi_gian_hoan, SYSDATETIME()),
        ma_giao_dich_hoan = COALESCE(
            ma_giao_dich_hoan,
            CONCAT('REF-REQ-', REPLACE(CONVERT(VARCHAR(36), NEWID()), '-', ''))
        )
    WHERE trang_thai_hoan = 'CHO_HOAN'
      AND so_tien_hoan > 0
      AND (thoi_gian_hoan IS NULL OR ma_giao_dich_hoan IS NULL);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
