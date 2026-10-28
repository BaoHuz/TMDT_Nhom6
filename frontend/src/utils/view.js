export const apiData=response=>response.data.data;
export const messageOf=error=>error.response?.data?.message||'Không thể kết nối máy chủ.';
export const money=value=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(Number(value||0));
export const dateText=value=>value?new Intl.DateTimeFormat('vi-VN').format(new Date(`${value}T00:00:00`)):'';
export const statusText=value=>({MO:'Đang mở',HET_CHO:'Hết chỗ',DONG:'Đã đóng',HOAT_DONG:'Hoạt động',TAM_DONG:'Tạm đóng',DANG_BAN:'Đang bán',TAM_DUNG:'Tạm dừng',NGUNG_BAN:'Ngừng bán',CHO_THANH_TOAN:'Chờ thanh toán',CHO_XU_LY:'Đang xử lý thanh toán',DA_XAC_NHAN:'Đã xác nhận',DA_HUY:'Đã hủy',DA_CHECK_IN:'Đã check-in',HET_HAN:'Hết hạn thanh toán',THANH_CONG:'Thành công',THAT_BAI:'Thất bại',CHUA_THANH_TOAN:'Chưa thanh toán',CHO_HOAN:'Chờ xử lý hoàn tiền',DANG_XU_LY:'Đang xử lý/đối soát VNPay',HOAN_MOT_PHAN:'Đã hoàn một phần',HOAN_TOAN:'Đã hoàn tiền',KHONG_HOAN:'Không hoàn tiền',KHONG_AP_DUNG:'Không áp dụng'})[value]||value;
