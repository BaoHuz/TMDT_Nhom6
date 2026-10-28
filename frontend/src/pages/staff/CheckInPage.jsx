import React, { useState } from 'react';
import { QrCode, Search, CheckCircle2, AlertCircle, Users, Calendar, Clock, RotateCcw } from 'lucide-react';
import { checkInService } from '../../services/checkInService';
import { Loading, PageTitle } from '../../components/States';
import { money, statusText } from '../../utils/view';

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
      setPreviewData(null); // done previewing
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

  return (
    <div className="section-container staff-checkin-container">
      <PageTitle eyebrow="BỘ PHẬN TIẾP ĐÓN" title="Quầy Check-in Vé Bơi">
        Xác thực mã vé QR của khách hàng trước khi vào hồ bơi.
      </PageTitle>

      <div className="checkin-panel card">
        <form onSubmit={handlePreview} className="qr-search-form">
          <div className="form-group" style={{ flex: 1, margin: 0 }}>
            <label>Nhập hoặc quét mã QR vé:</label>
            <div className="search-input-wrapper">
              <QrCode size={20} className="input-icon" />
              <input
                type="text"
                className="form-input with-icon"
                placeholder="VD: QR-DL-c23fd9... (hoặc quét máy quét barcode)"
                value={qrInput}
                onChange={e => setQrInput(e.target.value)}
              />
            </div>
          </div>
          <button
            type="submit"
            className="btn btn-primary"
            style={{ height: '44px', alignSelf: 'flex-end' }}
            disabled={loadingPreview || !qrInput.trim()}
          >
            <Search size={18} /> {loadingPreview ? 'Đang kiểm tra...' : 'Tra cứu vé'}
          </button>
        </form>

        {previewError && (
          <div className="alert-box alert-danger" style={{ marginTop: '1.5rem' }}>
            <AlertCircle size={20} />
            <div>
              <b>Không thể check-in:</b>
              <div>{previewError}</div>
            </div>
          </div>
        )}

        {checkInError && (
          <div className="alert-box alert-danger" style={{ marginTop: '1.5rem' }}>
            <AlertCircle size={20} />
            <div>
              <b>Lỗi xác nhận:</b>
              <div>{checkInError}</div>
            </div>
          </div>
        )}

        {checkInSuccess && (
          <div className="alert-box alert-success checkin-success-box" style={{ marginTop: '1.5rem' }}>
            <CheckCircle2 size={32} />
            <div>
              <h3>CHECK-IN THÀNH CÔNG!</h3>
              <p>Mã đơn: <b>{checkInSuccess.maDat}</b> | Số người: <b>{checkInSuccess.soNguoi}</b></p>
              <p>Khách hàng đã được xác nhận vào hồ bơi.</p>
              <button className="btn btn-primary" style={{ marginTop: '0.75rem' }} onClick={handleReset}>
                <RotateCcw size={16} /> Tiếp tục lượt check-in mới
              </button>
            </div>
          </div>
        )}

        {/* Step 2 Preview Card */}
        {previewData && (
          <div className="ticket-preview-card" style={{ marginTop: '1.5rem' }}>
            <div className="ticket-badge-valid">
              ✓ Vé hợp lệ - Đủ điều kiện check-in
            </div>

            <div className="ticket-grid">
              <div className="ticket-col">
                <small>Mã đơn đặt:</small>
                <b>{previewData.maDat}</b>
              </div>
              <div className="ticket-col">
                <small>Mã ca bơi:</small>
                <b>{previewData.maLich}</b>
              </div>
              <div className="ticket-col">
                <small>Số lượng khách:</small>
                <b><Users size={16} style={{ display: 'inline', verticalAlign: 'middle' }} /> {previewData.soNguoi} người</b>
              </div>
              <div className="ticket-col">
                <small>Tổng tiền vé:</small>
                <b>{money(previewData.tongTien)}</b>
              </div>
            </div>

            <div className="ticket-action-row">
              <button
                className="btn btn-secondary"
                disabled={checkingIn}
                onClick={handleReset}
              >
                Hủy bỏ
              </button>
              <button
                className="btn btn-primary btn-large"
                style={{ minWidth: '220px' }}
                disabled={checkingIn}
                onClick={handleConfirmCheckIn}
              >
                <CheckCircle2 size={18} /> {checkingIn ? 'Đang xác nhận...' : 'Xác nhận Check-in cho khách'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
