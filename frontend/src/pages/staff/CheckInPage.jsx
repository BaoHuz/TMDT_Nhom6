import React, { useState } from 'react';
import {
  QrCode, Search, CheckCircle2, Users, RotateCcw, ScanLine,
  TicketCheck, Loader2, XCircle, ShieldCheck, Hash, Banknote
} from 'lucide-react';
import { checkInService } from '../../services/checkInService';
import { money } from '../../utils/view';

const errorTone = message => {
  const normalized = (message || '').toLocaleLowerCase('vi');
  return normalized.includes('đã check-in') || normalized.includes('thời gian') || normalized.includes('chưa đến')
    ? 'warning'
    : 'danger';
};

export default function CheckInPage() {
  const [qrInput, setQrInput] = useState('');
  const [previewData, setPreviewData] = useState(null);
  const [loadingPreview, setLoadingPreview] = useState(false);
  const [previewError, setPreviewError] = useState('');

  const [checkingIn, setCheckingIn] = useState(false);
  const [checkInSuccess, setCheckInSuccess] = useState(null);
  const [checkInError, setCheckInError] = useState('');

  const handlePreview = async (e) => {
    e.preventDefault();
    if (!qrInput.trim()) return;

    setLoadingPreview(true);
    setPreviewError('');
    setPreviewData(null);
    setCheckInSuccess(null);
    setCheckInError('');

    try {
      const res = await checkInService.preview(qrInput.trim());
      setPreviewData(res.data.data);
    } catch (err) {
      setPreviewError(err.response?.data?.message || 'Mã vé QR không hợp lệ hoặc không đủ điều kiện.');
    } finally {
      setLoadingPreview(false);
    }
  };

  const handleConfirmCheckIn = async () => {
    if (!qrInput.trim()) return;

    setCheckingIn(true);
    setCheckInError('');

    try {
      const res = await checkInService.check({ maVeQr: qrInput.trim() });
      setCheckInSuccess(res.data.data);
      setPreviewData(null);
    } catch (err) {
      setCheckInError(err.response?.data?.message || 'Check-in thất bại.');
    } finally {
      setCheckingIn(false);
    }
  };

  const handleReset = () => {
    setQrInput('');
    setPreviewData(null);
    setCheckInSuccess(null);
    setPreviewError('');
    setCheckInError('');
  };

  const hasResult = previewData || previewError || checkInError || checkInSuccess;
  const visibleError = checkInError || previewError;

  return (
    <div className="staff-checkin-page">
      <section className="staff-page-heading">
        <div className="staff-page-heading-icon"><QrCode size={27} /></div>
        <div>
          <span className="staff-page-eyebrow">BỘ PHẬN TIẾP ĐÓN</span>
          <h1>Quầy Check-in Vé Bơi</h1>
          <p>Xác thực mã vé QR của khách hàng trước khi vào hồ bơi.</p>
        </div>
      </section>

      <div className={`checkin-workspace ${hasResult ? 'has-result' : ''}`}>
        <section className="checkin-lookup-card" aria-labelledby="lookup-title">
          <div className="checkin-card-heading">
            <span className="checkin-card-icon"><ScanLine size={22} /></span>
            <div>
              <h2 id="lookup-title">Xác thực vé</h2>
              <p>Nhập hoặc quét mã QR của khách hàng</p>
            </div>
          </div>

          <form onSubmit={handlePreview} className="qr-search-form">
            <div className="form-group checkin-input-group">
              <label htmlFor="staff-qr-input">Mã QR vé</label>
              <div className="search-input-wrapper">
                <QrCode size={21} className="input-icon" aria-hidden="true" />
                <input
                  id="staff-qr-input"
                  type="text"
                  className="form-input with-icon"
                  placeholder="Ví dụ: QR-DL-c23fd9..."
                  value={qrInput}
                  onChange={e => setQrInput(e.target.value)}
                  autoComplete="off"
                />
              </div>
            </div>
            <button type="submit" className="btn btn-primary checkin-search-button" disabled={loadingPreview || !qrInput.trim()}>
              {loadingPreview ? <Loader2 size={18} className="state-spinner" /> : <Search size={18} />}
              {loadingPreview ? 'Đang kiểm tra...' : 'Tra cứu vé'}
            </button>
          </form>

          <div className="scanner-hint">
            <ScanLine size={17} />
            <span>Có thể dùng máy quét barcode/QR; mã sẽ tự động nhập vào ô phía trên.</span>
          </div>

          {!hasResult && (
            <div className="checkin-ready-state">
              <ShieldCheck size={30} />
              <div><strong>Sẵn sàng tiếp nhận vé</strong><span>Thông tin vé sẽ xuất hiện sau khi tra cứu.</span></div>
            </div>
          )}
        </section>

        {hasResult && (
          <section className="checkin-result-column" aria-live="polite">
            {visibleError && (
              <div className={`checkin-status-panel ${errorTone(visibleError)}`}>
                <span className="checkin-status-icon"><XCircle size={30} /></span>
                <div>
                  <span className="status-panel-label">KHÔNG THỂ CHECK-IN</span>
                  <h2>Vé chưa đủ điều kiện</h2>
                  <p>{visibleError}</p>
                  <button className="btn btn-secondary" type="button" onClick={handleReset}>
                    <RotateCcw size={16} /> Quét vé khác
                  </button>
                </div>
              </div>
            )}

            {checkInSuccess && (
              <div className="checkin-status-panel success">
                <span className="checkin-status-icon"><CheckCircle2 size={34} /></span>
                <div className="checkin-success-content">
                  <span className="status-panel-label">CHECK-IN THÀNH CÔNG</span>
                  <h2>Đã xác nhận khách vào hồ</h2>
                  <div className="checkin-success-summary">
                    <div><small>Mã đơn</small><strong>{checkInSuccess.maDat}</strong></div>
                    <div><small>Mã ca bơi</small><strong>{checkInSuccess.maLich}</strong></div>
                    <div><small>Số khách</small><strong>{checkInSuccess.soNguoi} người</strong></div>
                  </div>
                  <button className="btn btn-primary" type="button" onClick={handleReset}>
                    <RotateCcw size={16} /> Quét vé tiếp theo
                  </button>
                </div>
              </div>
            )}

            {previewData && (
              <div className="ticket-preview-card">
                <div className="ticket-preview-header">
                  <div>
                    <span className="ticket-badge-valid"><CheckCircle2 size={16} /> VÉ HỢP LỆ</span>
                    <h2>Thông tin vé check-in</h2>
                    <p>Vé đã được xác thực và đủ điều kiện check-in.</p>
                  </div>
                  <TicketCheck size={38} />
                </div>

                <div className="ticket-grid">
                  <div className="ticket-col"><span><Hash size={16} /> Mã đơn đặt</span><strong>{previewData.maDat}</strong></div>
                  <div className="ticket-col"><span><TicketCheck size={16} /> Mã ca bơi</span><strong>{previewData.maLich}</strong></div>
                  <div className="ticket-col"><span><Users size={16} /> Số lượng khách</span><strong>{previewData.soNguoi} người</strong></div>
                  <div className="ticket-col"><span><Banknote size={16} /> Tổng tiền vé</span><strong>{money(previewData.tongTien)}</strong></div>
                </div>

                <div className="ticket-action-row">
                  <button className="btn btn-secondary" type="button" disabled={checkingIn} onClick={handleReset}>Hủy bỏ</button>
                  <button className="btn btn-primary btn-large" type="button" disabled={checkingIn} onClick={handleConfirmCheckIn}>
                    {checkingIn ? <Loader2 size={18} className="state-spinner" /> : <CheckCircle2 size={18} />}
                    {checkingIn ? 'Đang xác nhận...' : 'Xác nhận check-in'}
                  </button>
                </div>
              </div>
            )}
          </section>
        )}
      </div>
    </div>
  );
}
