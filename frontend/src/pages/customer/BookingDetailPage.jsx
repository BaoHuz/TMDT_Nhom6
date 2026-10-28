import React, { useEffect, useState } from 'react';
import { useParams, Link, useLocation } from 'react-router-dom';
import { ArrowLeft, Calendar, Clock, MapPin, Users, QrCode, AlertTriangle, CheckCircle2, ShieldAlert, AlertCircle, CreditCard, X } from 'lucide-react';
import { bookingService } from '../../services/bookingService';
import { paymentService } from '../../services/paymentService';
import { Loading, ErrorState, PageTitle } from '../../components/States';
import { money, dateText, statusText } from '../../utils/view';

export default function BookingDetailPage() {
  const { id } = useParams();
  const location = useLocation();

  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Cancellation modal states
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelPreview, setCancelPreview] = useState(null);
  const [loadingPreview, setLoadingPreview] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState('');

  // Payment retry if CHO_THANH_TOAN
  const [paying, setPaying] = useState(false);
  const [paymentMsg, setPaymentMsg] = useState('');

  const gatewayResult = new URLSearchParams(location.search).get('payment');
  const flashMessage = location.state?.successMessage || (gatewayResult === 'success'
    ? 'VNPay báo giao dịch thành công. Đơn sẽ được xác nhận sau khi máy chủ nhận và kiểm tra IPN.'
    : gatewayResult === 'failed' ? 'Giao dịch VNPay chưa thành công. Bạn có thể thử thanh toán lại trong thời hạn đơn.'
    : gatewayResult === 'invalid' ? 'Không xác minh được kết quả trả về từ VNPay.' : '');

  const loadBooking = () => {
    setLoading(true);
    bookingService.get(id)
      .then(res => setBooking(res.data.data))
      .catch(err => setError(err.response?.data?.message || 'Không tìm thấy thông tin đơn đặt lịch.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadBooking();
  }, [id]);

  useEffect(() => {
    if (gatewayResult !== 'success') return undefined;
    let attempts = 0;
    const timer = window.setInterval(async () => {
      attempts += 1;
      try {
        const res = await bookingService.get(id);
        const latest = res.data.data;
        setBooking(latest);
        if (latest.trangThai !== 'CHO_THANH_TOAN' || attempts >= 15) window.clearInterval(timer);
      } catch { if (attempts >= 15) window.clearInterval(timer); }
    }, 2000);
    return () => window.clearInterval(timer);
  }, [gatewayResult, id]);

  const handleOpenCancelModal = async () => {
    setCancelError('');
    setLoadingPreview(true);
    setShowCancelModal(true);

    try {
      const res = await bookingService.cancelPreview(id);
      setCancelPreview(res.data.data);
    } catch (err) {
      setCancelError(err.response?.data?.message || 'Không thể kiểm tra điều kiện hủy đơn.');
    } finally {
      setLoadingPreview(false);
    }
  };

  const handleConfirmCancel = async () => {
    setCancelling(true);
    setCancelError('');

    try {
      const res = await bookingService.cancel(id, cancelReason);
      setBooking(res.data.data);
      setShowCancelModal(false);
    } catch (err) {
      setCancelError(err.response?.data?.message || 'Hủy đơn thất bại. Vui lòng thử lại.');
    } finally {
      setCancelling(false);
    }
  };

  const handlePayNow = async () => {
    setPaying(true);
    setPaymentMsg('');

    try {
      const res = await paymentService.createVnPay(id);
      window.location.assign(res.data.data.paymentUrl);
    } catch (err) {
      setPaymentMsg(err.response?.data?.message || 'Thanh toán thất bại.');
    } finally {
      setPaying(false);
    }
  };

  if (loading) return <div className="section-container"><Loading /></div>;
  if (error || !booking) return <div className="section-container"><ErrorState message={error || 'Đơn không tồn tại.'} /></div>;

  const canCancel = booking.trangThai === 'DA_XAC_NHAN' || booking.trangThai === 'CHO_THANH_TOAN';

  return (
    <div className="section-container">
      <Link to="/my/bookings" className="btn-back">
        <ArrowLeft size={16} /> Quay lại danh sách đơn
      </Link>

      <PageTitle eyebrow="CHI TIẾT VÉ ĐIỆN TỬ" title={`Đơn hàng #${booking.maDat}`}>
        Thông tin chi tiết ca bơi, mã vé check-in và chính sách hoàn tiền.
      </PageTitle>

      {flashMessage && (
        <div className="alert-box alert-success" style={{ marginBottom: '1.5rem' }}>
          <CheckCircle2 size={18} />
          <span>{flashMessage}</span>
        </div>
      )}

      {paymentMsg && (
        <div className={`alert-box ${paymentMsg.includes('thành công') ? 'alert-success' : 'alert-danger'}`} style={{ marginBottom: '1.5rem' }}>
          <CheckCircle2 size={18} />
          <span>{paymentMsg}</span>
        </div>
      )}

      <div className="booking-detail-grid">
        {/* Main Details */}
        <div className="booking-detail-content card">
          <div className="detail-section-title">Thông tin ca bơi</div>
          <h2 style={{ fontSize: '1.4rem', color: 'var(--primary-dark)', margin: '0.25rem 0 1rem' }}>{booking.dichVu}</h2>

          <div className="info-rows">
            <div className="info-row">
              <span className="info-label"><MapPin size={16} /> Địa điểm:</span>
              <span className="info-val"><b>{booking.hoBoi}</b> ({booking.khuVuc})</span>
            </div>
            <div className="info-row">
              <span className="info-label"><Calendar size={16} /> Ngày bơi:</span>
              <span className="info-val"><b>{dateText(booking.ngay)}</b></span>
            </div>
            <div className="info-row">
              <span className="info-label"><Clock size={16} /> Khung giờ:</span>
              <span className="info-val"><b>{booking.gioBatDau?.slice(0, 5)} – {booking.gioKetThuc?.slice(0, 5)}</b></span>
            </div>
            <div className="info-row">
              <span className="info-label"><Users size={16} /> Số lượng vé:</span>
              <span className="info-val"><b>{booking.soNguoi} người</b></span>
            </div>
            <div className="info-row">
              <span className="info-label">Đơn giá tại thời điểm đặt:</span>
              <span className="info-val">{money(booking.donGiaTaiDat)} / vé</span>
            </div>
            <div className="info-row total-highlight-row">
              <span className="info-label">Tổng thanh toán:</span>
              <span className="info-val total-price">{money(booking.tongTien)}</span>
            </div>
          </div>

          <div className="detail-section-title" style={{ marginTop: '2rem' }}>Trạng thái giao dịch</div>
          <div className="info-rows">
            <div className="info-row">
              <span className="info-label">Trạng thái đơn:</span>
              <span className="info-val">
                <span className={`status-tag ${booking.trangThai === 'DA_XAC_NHAN' ? 'tag-green' : booking.trangThai === 'DA_HUY' ? 'tag-red' : booking.trangThai === 'DA_CHECK_IN' ? 'tag-blue' : 'tag-yellow'}`}>
                  {statusText(booking.trangThai)}
                </span>
              </span>
            </div>
            <div className="info-row">
              <span className="info-label">Trạng thái thanh toán:</span>
              <span className="info-val">
                <span className={`status-tag ${booking.trangThaiThanhToan === 'THANH_CONG' ? 'tag-green' : 'tag-yellow'}`}>
                  {statusText(booking.trangThaiThanhToan)}
                </span>
              </span>
            </div>
            {booking.trangThai === 'CHO_THANH_TOAN' && booking.hanThanhToan && (
              <div className="info-row">
                <span className="info-label">Hạn thanh toán:</span>
                <span className="info-val"><b>{new Date(booking.hanThanhToan).toLocaleString('vi-VN')}</b> (trong 15 phút kể từ lúc đặt)</span>
              </div>
            )}
          </div>

          {/* If booking was canceled */}
          {booking.trangThai === 'DA_HUY' && (
            <div className="canceled-info-box">
              <div className="canceled-title">
                <AlertTriangle size={18} /> Đơn đã hủy
              </div>
              <p><b>Thời gian hủy:</b> {booking.thoiGianHuy ? new Date(booking.thoiGianHuy).toLocaleString('vi-VN') : '—'}</p>
              <p><b>Lý do:</b> {booking.lyDoHuy || 'Không có lý do cụ thể'}</p>
              <p><b>Tỷ lệ hoàn tiền:</b> {booking.tyLeHoan}%</p>
              <p><b>Số tiền hoàn lại:</b> <b style={{ color: 'var(--primary-dark)' }}>{money(booking.soTienHoan)}</b></p>
            </div>
          )}

          {/* Action buttons */}
          <div className="booking-actions-footer">
            {canCancel && (
              <button className="btn btn-danger-outline" onClick={handleOpenCancelModal}>
                <AlertTriangle size={16} /> Hủy vé bơi này
              </button>
            )}

            {booking.trangThai === 'CHO_THANH_TOAN' && (
              <button className="btn btn-primary" style={{ width: '100%', marginTop: '1rem' }} disabled={paying} onClick={handlePayNow}>
                <CreditCard size={16} /> {paying ? 'Đang chuyển tới VNPay...' : 'Thanh toán qua VNPay Sandbox'}
              </button>
            )}
          </div>
        </div>

        {/* QR Code Ticket Section */}
        <div className="booking-qr-sidebar card">
          <h3>Mã vé điện tử Check-in</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Xuất trình mã này cho nhân viên tại hồ bơi để hoàn tất thủ tục check-in vào hồ.
          </p>

          {booking.maVeQr ? (
            <div className="qr-container">
              <div className="qr-box">
                {/* Visual QR representation */}
                <div className="qr-graphic">
                  <QrCode size={140} color="var(--primary-dark)" />
                </div>
                <div className="qr-code-text">{booking.maVeQr}</div>
                <small className="qr-instruction">Quét mã tại quầy tiếp đón</small>
              </div>
              <div className={`qr-status-indicator ${booking.trangThai === 'DA_CHECK_IN' ? 'checked-in' : 'valid'}`}>
                {booking.trangThai === 'DA_CHECK_IN' ? '✓ ĐÃ CHECK-IN THÀNH CÔNG' : 'MÃ HỢP LỆ — SẴN SÀNG CHECK-IN'}
              </div>
            </div>
          ) : (
            <div className="qr-empty-box">
              <ShieldAlert size={36} color="var(--text-muted)" />
              <p>Chưa có mã vé QR. Mã sẽ tự động được cấp sau khi thanh toán thành công.</p>
            </div>
          )}
        </div>
      </div>

      {/* Cancel Confirmation Modal */}
      {showCancelModal && (
        <div className="modal-backdrop">
          <div className="modal-box">
            <div className="modal-header">
              <h3>Xác nhận hủy vé bơi</h3>
              <button className="btn-close" onClick={() => setShowCancelModal(false)}>
                <X size={18} />
              </button>
            </div>

            <div className="modal-body">
              {loadingPreview ? (
                <Loading />
              ) : cancelError ? (
                <div className="alert-box alert-danger">
                  <AlertCircle size={18} />
                  <span>{cancelError}</span>
                </div>
              ) : cancelPreview ? (
                <>
                  <div className="refund-summary-box">
                    <div className="refund-row">
                      <span>Thời gian còn lại đến ca bơi:</span>
                      <b>{cancelPreview.hoursRemaining < 0 ? 'Ca đang diễn ra' : `${cancelPreview.hoursRemaining} giờ`}</b>
                    </div>
                    <div className="refund-row">
                      <span>Tỷ lệ hoàn tiền theo quy định:</span>
                      <b style={{ color: 'var(--primary-dark)', fontSize: '1.1rem' }}>{cancelPreview.refundRate}%</b>
                    </div>
                    <div className="refund-row">
                      <span>Số tiền dự kiến hoàn lại:</span>
                      <b style={{ color: 'var(--primary-dark)', fontSize: '1.2rem' }}>{money(cancelPreview.refundAmount)}</b>
                    </div>
                    <p className="refund-policy-hint">{cancelPreview.message}</p>
                  </div>

                  <div className="form-group" style={{ marginTop: '1.25rem' }}>
                    <label>Lý do hủy vé (tùy chọn):</label>
                    <textarea
                      className="form-input"
                      rows={3}
                      placeholder="VD: Bận việc đột xuất, đổi lịch công tác..."
                      value={cancelReason}
                      onChange={e => setCancelReason(e.target.value)}
                    />
                  </div>
                </>
              ) : null}
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-secondary"
                disabled={cancelling}
                onClick={() => setShowCancelModal(false)}
              >
                Giữ lại vé
              </button>
              <button
                type="button"
                className="btn btn-danger"
                disabled={cancelling || loadingPreview || (cancelPreview && !cancelPreview.eligible)}
                onClick={handleConfirmCancel}
              >
                {cancelling ? 'Đang xử lý...' : 'Xác nhận hủy vé'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
