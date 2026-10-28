/*
    AquaFlow - migration quản lý hình ảnh hồ bơi
    Chỉ lưu metadata/đường dẫn; tệp vật lý nằm tại backend/uploads/pools/{maHoBoi}/.
*/

USE PoolBooking;
GO

IF OBJECT_ID(N'dbo.hinh_anh_ho_boi', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.hinh_anh_ho_boi (
        ma_anh             VARCHAR(60)   NOT NULL,
        ma_ho_boi          VARCHAR(60)   NOT NULL,
        duong_dan          NVARCHAR(500) NOT NULL,
        thu_tu             INT           NOT NULL CONSTRAINT DF_hinh_anh_ho_boi_thu_tu DEFAULT 0,
        la_anh_dai_dien    BIT           NOT NULL CONSTRAINT DF_hinh_anh_ho_boi_dai_dien DEFAULT 0,
        ngay_tao           DATETIME2     NOT NULL CONSTRAINT DF_hinh_anh_ho_boi_ngay_tao DEFAULT SYSDATETIME(),

        CONSTRAINT PK_hinh_anh_ho_boi PRIMARY KEY (ma_anh),
        CONSTRAINT FK_hinh_anh_ho_boi_ho_boi
            FOREIGN KEY (ma_ho_boi) REFERENCES dbo.ho_boi(ma_ho_boi) ON DELETE CASCADE,
        CONSTRAINT UQ_hinh_anh_ho_boi_duong_dan UNIQUE (duong_dan),
        CONSTRAINT CK_hinh_anh_ho_boi_thu_tu CHECK (thu_tu >= 0)
    );

    CREATE INDEX IX_hinh_anh_ho_boi_ho_boi_thu_tu
        ON dbo.hinh_anh_ho_boi(ma_ho_boi, thu_tu, ngay_tao);

    CREATE UNIQUE INDEX UX_hinh_anh_ho_boi_anh_dai_dien
        ON dbo.hinh_anh_ho_boi(ma_ho_boi)
        WHERE la_anh_dai_dien = 1;
END;
GO
