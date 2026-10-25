USE PoolBooking;
GO
-- Password for all demo accounts: password (BCrypt hash).
INSERT INTO tai_khoan VALUES
('TK-CUSTOMER','customer','{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Nguyen Van A','customer@pool.local','0900000001','CUSTOMER','HOAT_DONG'),
('TK-STAFF','staff','{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Le Thi B','staff@pool.local','0900000002','STAFF','HOAT_DONG'),
('TK-ADMIN','admin','{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy','Admin Pool','admin@pool.local','0900000003','ADMIN','HOAT_DONG');
INSERT INTO khu_vuc VALUES ('KV01',N'Hồ người lớn',50,'HOAT_DONG'),('KV02',N'Hồ trẻ em',25,'HOAT_DONG'),('KV03',N'Hồ tập luyện',40,'HOAT_DONG');
INSERT INTO dich_vu VALUES('DV01', N'Vé bơi người lớn', 50000, 90, 'DANG_BAN'),('DV02', N'Vé bơi trẻ em', 30000, 90, 'DANG_BAN'),('DV03', N'Vé tập luyện', 40000, 120, 'DANG_BAN');
INSERT INTO lich_dich_vu VALUES ('L001','DV01','KV01',CAST(GETDATE() AS date),'06:00','07:30',40,40,'MO',0),('L002','DV02','KV02',CAST(GETDATE() AS date),'08:00','09:30',20,20,'MO',0),('L003','DV03','KV03',CAST(GETDATE() AS date),'16:30','18:30',30,30,'MO',0),('L004','DV01','KV01',DATEADD(day,1,CAST(GETDATE() AS date)),'06:00','07:30',40,40,'MO',0),('L005','DV02','KV02',DATEADD(day,1,CAST(GETDATE() AS date)),'08:00','09:30',20,20,'MO',0),('L006','DV03','KV03',DATEADD(day,1,CAST(GETDATE() AS date)),'18:30','20:30',30,30,'MO',0);
