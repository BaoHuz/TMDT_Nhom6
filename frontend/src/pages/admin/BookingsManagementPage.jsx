import React, { useEffect, useState } from 'react';
import { Search, Filter, Eye, X, QrCode, AlertTriangle } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';
import { money, dateText, statusText } from '../../utils/view';

export default function BookingsManagementPage() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters
  const [searchCode, setSearchCode] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Detail Modal
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [refundSubmitting, setRefundSubmitting] = useState(false);
  const [refundFeedback, setRefundFeedback] = useState(null);

  useEffect(() => {
    adminService.bookings()
      .then(res => setBookings(res.data.data || []))
      .catch(() => setError('Không thể tải danh sách đơn đặt vé.'))
      .finally(() => setLoading(false));
  }, []);

  const filteredBookings = bookings.filter(b => {
    const matchCode = !searchCode.trim() || b.maDat?.toLowerCase().includes(searchCode.trim().toLowerCase());
    const matchStatus = statusFilter === 'ALL' || b.trangThai === statusFilter;
    return matchCode && matchStatus;
  });

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'DA_XAC_NHAN': return 'tag-green';
      case 'CHO_THANH_TOAN': return 'tag-yellow';
      case 'DA_CHECK_IN': return 'tag-blue';
      case 'DA_HUY': return 'tag-red';
      default: return 'tag-gray';
    }
  };

  const handleRefund = async () => {
    if (!selectedBooking || refundSubmitting) return;
    const confirmed = window.confirm(
      `Gửi yêu cầu hoàn ${money(selectedBooking.soTienYeuCauHoan)} cho giao dịch VNPay của đơn ${selectedBooking.maDat}?`
    );
    if (!confirmed) return;

    setRefundSubmitting(true);
    setRefundFeedback(null);
    try {
      const response = await adminService.refundBooking(selectedBooking.maDat);
      setRefundFeedback({
        type: 'success',
        message: response.data.message || 'VNPay đã xác nhận hoàn tiền.'
      });
    } catch (requestError) {
      setRefundFeedback({
        type: 'error',
        message: requestError.response?.data?.message || 'Không thể xác nhận kết quả hoàn tiền từ VNPay.'
      });
    } finally {
      try {
        const response = await adminService.bookings();
        const latestBookings = response.data.data || [];
        setBookings(latestBookings);
        setSelectedBooking(latestBookings.find(item => item.maDat === selectedBooking.maDat) || null);
      } catch {
        setRefundFeedback(current => ({
          type: 'error',
          message: `${current?.message || 'Đã gửi yêu cầu hoàn.'} Không tải lại được trạng thái mới nhất; hãy tải lại trang.`
        }));
      }
      setRefundSubmitting(false);
    }
  };

  return (
    <div className="admin-page-container">
      <PageTitle eyebrow="GIAO DỊCH VÀ ĐẶT LỊCH" title="Quản lý Đơn đặt vé">
        Tra cứu lịch sử đặt chỗ, trạng thái thanh toán và kiểm tra mã vé QR của toàn hệ thống.
      </PageTitle>

      {/* Filter bar */}
      <div className="filter-bar-card card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ flex: 1, minWidth: '240px' }}>
            <div className="search-input-wrapper">
              <Search size={16} className="input-icon" />
              <input
                type="text"
                className="form-input with-icon"
                placeholder="Tìm theo mã đơn (VD: DL-...)..."
                value={searchCode}
                onChange={e => setSearchCode(e.target.value)}
              />
            </div>
          </div>

          <div style={{ minWidth: '200px' }}>
            <select
              className="form-input"
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value)}
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="CHO_THANH_TOAN">Chờ thanh toán</option>
              <option value="DA_XAC_NHAN">Đã xác nhận</option>
              <option value="DA_CHECK_IN">Đã check-in</option>
              <option value="DA_HUY">Đã hủy</option>
            </select>
          </div>
        </div>
      </div>

      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} />
      ) : filteredBookings.length === 0 ? (
        <EmptyState message="Không tìm thấy đơn đặt vé nào." />
      ) : (
        <div className="card table-card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Mã đơn</th>
                  <th>Mã KH</th>
                  <th>Mã ca</th>
                  <th>Số người</th>
                  <th>Tổng tiền</th>
                  <th>Trạng thái</th>
                  <th>Mã QR</th>
                  <th style={{ textAlign: 'right' }}>Chi tiết</th>
                </tr>
              </thead>
              <tbody>
                {filteredBookings.map(b => (
                  <tr key={b.maDat}>
                    <td><code>{b.maDat}</code></td>
                    <td><small>{b.maKh}</small></td>
                    <td><code>{b.maLich}</code></td>
                    <td><b>{b.soNguoi}</b></td>
                    <td><b>{money(b.tongTien)}</b></td>
                    <td>
                      <span className={`status-tag ${getStatusBadgeClass(b.trangThai)}`}>
                        {statusText(b.trangThai)}
                      </span>
                    </td>
                    <td>
                      {b.maVeQr ? <code>{b.maVeQr}</code> : <span style={{ color: 'var(--text-muted)' }}>—</span>}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button className="btn btn-outline btn-sm" onClick={() => setSelectedBooking(b)}>
                        <Eye size={14} /> Xem
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Booking Detail Modal */}
      {selectedBooking && (
        <div className="modal-backdrop">
          <div className="modal-box">
            <div className="modal-header">
              <h3>Chi tiết Đơn hàng #{selectedBooking.maDat}</h3>
              <button className="btn-close" onClick={() => setSelectedBooking(null)}><X size={18} /></button>
            </div>
            <div className="modal-body">
              <div className="info-rows">
                <div className="info-row">
                  <span className="info-label">Mã khách hàng:</span>
                  <span className="info-val"><code>{selectedBooking.maKh}</code></span>
                </div>
                <div className="info-row">
                  <span className="info-label">Mã ca bơi:</span>
                  <span className="info-val"><code>{selectedBooking.maLich}</code></span>
                </div>
                <div className="info-row">
                  <span className="info-label">Số vé (người):</span>
                  <span className="info-val"><b>{selectedBooking.soNguoi}</b></span>
                </div>
                <div className="info-row">
                  <span className="info-label">Đơn giá tại đặt:</span>
                  <span className="info-val">{money(selectedBooking.donGiaTaiDat)}</span>
                </div>
                <div className="info-row total-highlight-row">
                  <span className="info-label">Tổng thanh toán:</span>
                  <span className="info-val total-price">{money(selectedBooking.tongTien)}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Trạng thái đơn:</span>
                  <span className="info-val">
                    <span className={`status-tag ${getStatusBadgeClass(selectedBooking.trangThai)}`}>
                      {statusText(selectedBooking.trangThai)}
                    </span>
                  </span>
                </div>
                <div className="info-row">
                  <span className="info-label">Trạng thái thanh toán:</span>
                  <span className="info-val">{statusText(selectedBooking.trangThaiThanhToan)}</span>
                </div>
                {selectedBooking.trangThaiHoanTien !== 'KHONG_AP_DUNG' && (
                  <div className="info-row">
                    <span className="info-label">Trạng thái hoàn tiền:</span>
                    <span className="info-val">{statusText(selectedBooking.trangThaiHoanTien)}</span>
                  </div>
                )}
                <div className="info-row">
                  <span className="info-label">Mã vé QR:</span>
                  <span className="info-val">
                    {selectedBooking.maVeQr ? <b>{selectedBooking.maVeQr}</b> : 'Chưa cấp'}
                  </span>
                </div>
                <div className="info-row">
                  <span className="info-label">Thời điểm đặt:</span>
                  <span className="info-val">
                    {selectedBooking.ngayDat ? new Date(selectedBooking.ngayDat).toLocaleString('vi-VN') : '—'}
                  </span>
                </div>
              </div>

              {selectedBooking.trangThai === 'DA_HUY' && (
                <div className="canceled-info-box" style={{ marginTop: '1.25rem' }}>
                  <div className="canceled-title"><AlertTriangle size={16} /> Thông tin hoàn hủy</div>
                  <p><b>Lý do:</b> {selectedBooking.lyDoHuy || '—'}</p>
                  <p><b>Thời gian hủy:</b> {selectedBooking.thoiGianHuy ? new Date(selectedBooking.thoiGianHuy).toLocaleString('vi-VN') : '—'}</p>
                  <p><b>Giờ bắt đầu ca tại thời điểm hủy:</b> {selectedBooking.thoiDiemBatDauApDung ? new Date(selectedBooking.thoiDiemBatDauApDung).toLocaleString('vi-VN') : '—'}</p>
                  <p><b>Tỷ lệ hoàn:</b> {selectedBooking.tyLeHoan}%</p>
                  <p><b>Trạng thái hoàn tiền:</b> {statusText(selectedBooking.trangThaiHoanTien)}</p>
                  <p><b>Số tiền hoàn theo chính sách:</b> {money(selectedBooking.soTienHoan)}</p>
                  {['CHO_HOAN', 'DANG_XU_LY'].includes(selectedBooking.trangThaiHoanTien) && (
                    <>
                      <p><b>Số tiền đã ghi nhận yêu cầu hoàn:</b> {money(selectedBooking.soTienYeuCauHoan)}</p>
                      <p><b>Thời gian ghi nhận yêu cầu:</b> {selectedBooking.thoiGianGhiNhanHoan ? new Date(selectedBooking.thoiGianGhiNhanHoan).toLocaleString('vi-VN') : '—'}</p>
                      <p><b>Mã yêu cầu hoàn:</b> <code>{selectedBooking.maYeuCauHoan || '—'}</code></p>
                      <p><b>Phương thức thanh toán:</b> {selectedBooking.phuongThucThanhToan || '—'}</p>
                      {selectedBooking.ghiChuHoan && <p className="refund-admin-note">{selectedBooking.ghiChuHoan}</p>}
                      {selectedBooking.trangThaiHoanTien === 'CHO_HOAN'
                        && selectedBooking.phuongThucThanhToan === 'VNPAY'
                        && selectedBooking.vnpayThoiDiemGiaoDichGoc && (
                        <button
                          type="button"
                          className="btn btn-primary"
                          style={{ marginTop: '0.75rem' }}
                          onClick={handleRefund}
                          disabled={refundSubmitting}
                        >
                          {refundSubmitting ? 'Đang gửi VNPay...' : `Hoàn ${money(selectedBooking.soTienYeuCauHoan)} qua VNPay`}
                        </button>
                      )}
                      {selectedBooking.trangThaiHoanTien === 'CHO_HOAN'
                        && selectedBooking.phuongThucThanhToan === 'VNPAY'
                        && !selectedBooking.vnpayThoiDiemGiaoDichGoc && (
                        <p className="refund-admin-note">
                          Thiếu ngày giao dịch gốc VNPay nên không thể hoàn tự động. Không đánh dấu hoàn thủ công.
                        </p>
                      )}
                      {selectedBooking.trangThaiHoanTien === 'CHO_HOAN'
                        && selectedBooking.phuongThucThanhToan !== 'VNPAY' && (
                        <p className="refund-admin-note">
                          Giao dịch mô phỏng không thể chuyển tiền thật qua VNPay.
                        </p>
                      )}
                    </>
                  )}
                  {selectedBooking.trangThaiHoanTien === 'DANG_XU_LY' && (
                    <p className="refund-admin-note">
                      VNPay đang xử lý hoặc hệ thống đang đối soát. Không gửi lại để tránh hoàn trùng.
                      {selectedBooking.ghiChuHoan ? ` ${selectedBooking.ghiChuHoan}` : ''}
                    </p>
                  )}
                  {refundFeedback && (
                    <p role="status" className={`refund-admin-feedback ${refundFeedback.type}`}>
                      {refundFeedback.message}
                    </p>
                  )}
                  {['HOAN_MOT_PHAN', 'HOAN_TOAN'].includes(selectedBooking.trangThaiHoanTien) && (
                    <>
                      <p><b>Số tiền hoàn thực tế:</b> {money(selectedBooking.soTienHoanThucTe)}</p>
                      <p><b>Thời gian hoàn thực tế:</b> {selectedBooking.thoiGianHoanThucTe ? new Date(selectedBooking.thoiGianHoanThucTe).toLocaleString('vi-VN') : '—'}</p>
                      <p><b>Mã giao dịch hoàn:</b> <code>{selectedBooking.maGiaoDichHoan || '—'}</code></p>
                    </>
                  )}
                  {selectedBooking.trangThaiHoanTien === 'CHO_HOAN' && (
                    <p>Yêu cầu hoàn tiền đang chờ xử lý; chưa xác nhận tiền đã chuyển về tài khoản.</p>
                  )}
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={() => setSelectedBooking(null)}>Đóng</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
