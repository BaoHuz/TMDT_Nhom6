CREATE DATABASE PoolBooking;
GO
USE PoolBooking;
GO
CREATE TABLE tai_khoan(ma_tk varchar(60) primary key,ten_dang_nhap varchar(60) not null unique,mat_khau_bam varchar(255) not null,ho_ten nvarchar(150) not null,email varchar(150),dien_thoai varchar(20),vai_tro varchar(20) not null default 'CUSTOMER',trang_thai varchar(20) not null default 'HOAT_DONG');
CREATE TABLE khu_vuc(ma_khu_vuc varchar(60) primary key,ten_khu_vuc nvarchar(120) not null,suc_chua int not null check(suc_chua>0),trang_thai varchar(20) not null default 'HOAT_DONG');
CREATE TABLE dich_vu(ma_dv varchar(60) primary key,ten_dv nvarchar(150) not null,don_gia decimal(18,2) not null check(don_gia>=0),thoi_luong_phut int not null, trang_thai varchar(20) not null default 'DANG_BAN');
CREATE TABLE lich_dich_vu(ma_lich varchar(60) primary key,ma_dv varchar(60) not null references dich_vu(ma_dv),ma_khu_vuc varchar(60) not null references khu_vuc(ma_khu_vuc),ngay date not null,gio_bat_dau time not null,gio_ket_thuc time not null,suc_chua int not null,so_cho_con int not null,trang_thai varchar(20) not null,row_version bigint not null default 0,check(so_cho_con between 0 and suc_chua));
CREATE TABLE dat_lich(ma_dat varchar(60) primary key,ma_tk varchar(60) not null references tai_khoan(ma_tk),ma_lich varchar(60) not null references lich_dich_vu(ma_lich),so_nguoi int not null,don_gia_tai_dat decimal(18,2) not null,tong_tien decimal(18,2) not null,ma_ve_qr varchar(100) unique,ngay_dat datetime2 not null default sysdatetime(),trang_thai varchar(30) not null);
CREATE TABLE thanh_toan(ma_thanh_toan varchar(60) primary key,ma_dat varchar(60) not null references dat_lich(ma_dat),so_tien decimal(18,2) not null,phuong_thuc varchar(30) not null,ma_giao_dich varchar(100) unique,ngay_thanh_toan datetime2 not null,trang_thai varchar(20) not null);
CREATE TABLE check_in(ma_check_in varchar(60) primary key,ma_dat varchar(60) not null unique references dat_lich(ma_dat),ma_tk_nhan_vien varchar(60) not null references tai_khoan(ma_tk),thoi_gian datetime2 not null);
