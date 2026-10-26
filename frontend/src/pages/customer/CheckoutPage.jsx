import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { CreditCard, CheckCircle2, AlertCircle, ArrowLeft, Calendar, Clock, MapPin, Users, ShieldAlert } from 'lucide-react';
import { scheduleService } from '../../services/scheduleService';
import { bookingService } from '../../services/bookingService';
import { paymentService } from '../../services/paymentService';
import { Loading, ErrorState, PageTitle } from '../../components/States';
import { money, dateText } from '../../utils/view';

export default function CheckoutPage() {
  const { scheduleId } = useParams();
  const navigate = useNavigate();

  const [schedule, setSchedule] = useState(null);
  const [soNguoi, setSoNguoi] = useState(1);
  const [loadingSchedule, setLoadingSchedule] = useState(true);
  const [loadError, setLoadError] = useState(null);

  // Checkout flow state
  const [step, setStep] = useState(1); // 1: review & create booking, 2: simulate payment
  const [booking, setBooking] = useState(null);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState('');

  // Payment state
  const [simulateFailure, setSimulateFailure] = useState(false);
  const [paying, setPaying] = useState(false);
  const [paymentError, setPaymentError] = useState('');

  useEffect(() => {
    scheduleService.get(scheduleId)
      .then(res => setSchedule(res.data.data))
      .catch(() => setLoadError('Không thể tải thông tin ca bơi.'))
      .finally(() => setLoadingSchedule(false));
  }, [scheduleId]);

  const handleCreateBooking = async (e) => {
    e.preventDefault();
    setCreateError('');
    setCreating(true);

    try {
      const res = await bookingService.create({
        maLich: scheduleId,
        soNguoi: parseInt(soNguoi, 10)
      });
      setBooking(res.data.data);
      setStep(2); // move to payment step
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Không thể tạo đơn đặt lịch. Vui lòng kiểm tra lại số chỗ.');
    } finally {
      setCreating(false);
    }
  };

  const handlePay = async () => {
    setPaymentError('');
    setPaying(true);

    try {
      await paymentService.pay({
        maDat: booking.maDat,
        simulateFailure
      });
      // Payment successful: redirect to booking detail with QR code
      navigate(`/my/bookings/${booking.maDat}`, {
        state: { successMessage: 'Thanh toán thành công! Vé bơi của bạn đã được kích hoạt.' }
      });
    } catch (err) {
      const msg = err.response?.data?.message || 'Thanh toán thất bại.';
      setPaymentError(msg);
    } finally {
      setPaying(false);
    }
  };

  if (loadingSchedule) return <div className="section-container"><Loading /></div>;
  if (loadError || !schedule) return <div className="section-container"><ErrorState message={loadError || 'Không tìm thấy ca bơi'} /></div>;

  const totalEstimate = (schedule.donGia || 0) * soNguoi;

  return (
    <div className="section-container checkout-container">
      <Link to={`/schedules/${scheduleId}`} className="btn-back">
        <ArrowLeft size={16} /> Quay lại chi tiết ca
      </Link>

      <PageTitle eyebrow="THANH TOÁN TRỰC TUYẾN" title="Xác nhận đặt vé & Thanh toán">
        {step === 1 ? 'Bước 1: Chọn số lượng vé và kiểm tra thông tin' : 'Bước 2: Thanh toán mô phỏng an toàn'}
      </PageTitle>

      {/* Progress steps */}
      <div className="checkout-steps-bar">
        <div className={`step-item ${step === 1 ? 'active' : 'completed'}`}>
          <span className="step-circle">{step > 1 ? '✓' : '1'}</span>
          <span>Chọn số lượng vé</span>
        </div>
        <div className="step-line" />
        <div className={`step-item ${step === 2 ? 'active' : ''}`}>
          <span className="step-circle">2</span>
          <span>Thanh toán mô phỏng</span>
        </div>
      </div>

      <div className="checkout-grid">
        {/* Left Column: Flow step content */}
        <div className="checkout-main">
          {step === 1 ? (
            <div className="card checkout-card">
              <h3>Thông tin người tham gia</h3>

              {createError && (
                <div className="alert-box alert-danger">
                  <AlertCircle size={18} />
                  <span>{createError}</span>
                </div>
              )}

              <form onSubmit={handleCreateBooking}>
                <div className="form-group">
                  <label>Số lượng vé (người bơi)</label>
                  <div className="quantity-control">
                    <button
                      type="button"
                      className="btn-qty"
                      disabled={soNguoi <= 1}
                      onClick={() => setSoNguoi(prev => Math.max(1, prev - 1))}
                    >
                      –
                    </button>
                    <input
                      type="number"
                      className="form-input qty-input"
                      min={1}
                      max={schedule.soChoCon}
                      value={soNguoi}
                      onChange={e => setSoNguoi(Math.min(schedule.soChoCon, Math.max(1, parseInt(e.target.value) || 1)))}
                    />
                    <button
                      type="button"
                      className="btn-qty"
                      disabled={soNguoi >= schedule.soChoCon}
                      onClick={() => setSoNguoi(prev => Math.min(schedule.soChoCon, prev + 1))}
                    >
                      +
                    </button>
                  </div>
                  <small style={{ color: 'var(--text-muted)', marginTop: '0.5rem', display: 'block' }}>
                    Tối đa <b>{schedule.soChoCon} chỗ</b> còn trống trong ca bơi này.
                  </small>
                </div>

                <div className="checkout-summary-box">
                  <div className="summary-row">
                    <span>Đơn giá niêm yết:</span>
                    <b>{money(schedule.donGia)}</b>
                  </div>
                  <div className="summary-row">
                    <span>Số lượng vé:</span>
                    <b>x {soNguoi}</b>
                  </div>
                  <div className="summary-divider" />
                  <div className="summary-row total-row">
                    <span>Tổng tiền tạm tính:</span>
                    <span className="total-highlight">{money(totalEstimate)}</span>
                  </div>
                </div>

                <button
                  type="submit"
                  className="btn btn-primary btn-large"
                  style={{ width: '100%', marginTop: '1.5rem' }}
                  disabled={creating || schedule.soChoCon <= 0}
                >
                  {creating ? 'Đang tạo đơn...' : 'Xác nhận & Tiến hành thanh toán'}
                </button>
              </form>
            </div>
          ) : (
            <div className="card checkout-card">
              <h3>Cổng thanh toán mô phỏng</h3>
              <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
                Đơn đặt vé <b>{booking?.maDat}</b> đã được tạo ở trạng thái Chờ thanh toán. Hãy chọn kịch bản để kiểm thử hệ thống:
              </p>

              {paymentError && (
                <div className="alert-box alert-danger">
                  <ShieldAlert size={20} />
                  <div>
                    <b>Lỗi thanh toán:</b>
                    <div>{paymentError}</div>
                  </div>
                </div>
              )}

              <div className="payment-options">
                <label className={`payment-option-card ${!simulateFailure ? 'selected' : ''}`}>
                  <input
                    type="radio"
                    name="payOption"
                    checked={!simulateFailure}
                    onChange={() => setSimulateFailure(false)}
                  />
                  <div className="option-content">
                    <div className="option-title">
                      <CreditCard size={18} /> Thanh toán thành công (Mô phỏng chuẩn)
                    </div>
                    <p>Hệ thống ghi nhận giao dịch thành công, xác nhận đơn và sinh mã QR check-in ngay lập tức.</p>
                  </div>
                </label>

                <label className={`payment-option-card ${simulateFailure ? 'selected' : ''}`}>
                  <input
                    type="radio"
                    name="payOption"
                    checked={simulateFailure}
                    onChange={() => setSimulateFailure(true)}
                  />
                  <div className="option-content">
                    <div className="option-title">
                      <ShieldAlert size={18} /> Mô phỏng thanh toán thất bại
                    </div>
                    <p>Kiểm tra xử lý lỗi khi giao dịch bị từ chối. Đơn hàng vẫn ở trạng thái Chờ thanh toán và không sinh mã QR.</p>
                  </div>
                </label>
              </div>

              <div style={{ marginTop: '2rem', display: 'flex', gap: '1rem' }}>
                <button
                  type="button"
                  className="btn btn-primary btn-large"
                  style={{ flex: 1 }}
                  disabled={paying}
                  onClick={handlePay}
                >
                  {paying ? 'Đang giao dịch...' : 'Xác nhận thanh toán'}
                </button>

                <Link to="/my/bookings" className="btn btn-secondary btn-large">
                  Để sau / Đơn của tôi
                </Link>
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Schedule Info Summary */}
        <div className="checkout-sidebar">
          <div className="card order-preview-card">
            <h4>Chi tiết ca bơi</h4>
            <div className="preview-service-name">{schedule.dichVu}</div>

            <div className="preview-item">
              <MapPin size={16} />
              <div>
                <b>{schedule.hoBoi}</b>
                <small>{schedule.khuVuc}</small>
              </div>
            </div>

            <div className="preview-item">
              <Calendar size={16} />
              <div>
                <b>{dateText(schedule.ngay)}</b>
              </div>
            </div>

            <div className="preview-item">
              <Clock size={16} />
              <div>
                <b>{schedule.gioBatDau?.slice(0, 5)} – {schedule.gioKetThuc?.slice(0, 5)}</b>
              </div>
            </div>

            <div className="preview-divider" />

            <div className="preview-calc">
              <div className="calc-row">
                <span>Số vé:</span>
                <span>{soNguoi} người</span>
              </div>
              <div className="calc-row">
                <span>Đơn giá:</span>
                <span>{money(schedule.donGia)}</span>
              </div>
              <div className="calc-row calc-total">
                <span>Tổng cộng:</span>
                <b>{money(booking ? booking.tongTien : totalEstimate)}</b>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
