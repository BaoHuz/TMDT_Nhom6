/* Run once against an existing PoolBooking database. Keeps existing rows. */
USE PoolBooking;
GO

IF COL_LENGTH('dbo.dat_lich', 'han_thanh_toan') IS NULL
BEGIN
    ALTER TABLE dbo.dat_lich
    ADD han_thanh_toan DATETIME2 NOT NULL
        CONSTRAINT DF_dat_lich_han_thanh_toan
        DEFAULT DATEADD(MINUTE, 15, SYSDATETIME());
END;
GO

IF OBJECT_ID('dbo.CK_tt_trang_thai', 'C') IS NOT NULL
    ALTER TABLE dbo.thanh_toan DROP CONSTRAINT CK_tt_trang_thai;
GO
ALTER TABLE dbo.thanh_toan WITH CHECK ADD CONSTRAINT CK_tt_trang_thai
    CHECK (trang_thai IN ('CHO_XU_LY', 'THANH_CONG', 'THAT_BAI', 'HOAN_TIEN'));
GO
