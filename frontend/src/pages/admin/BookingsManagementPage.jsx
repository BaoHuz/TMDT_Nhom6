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
                  <p><b>Tỷ lệ hoàn:</b> {selectedBooking.tyLeHoan}%</p>
                  <p><b>Tiền hoàn:</b> {money(selectedBooking.soTienHoan)}</p>
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
