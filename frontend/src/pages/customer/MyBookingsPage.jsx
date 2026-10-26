import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Calendar, Clock, MapPin, Users, QrCode, ArrowRight, CheckCircle2 } from 'lucide-react';
import { bookingService } from '../../services/bookingService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';
import { money, dateText, statusText } from '../../utils/view';

export default function MyBookingsPage() {
  const location = useLocation();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const flashMessage = location.state?.successMessage;

  useEffect(() => {
    bookingService.mine()
      .then(res => setBookings(res.data.data || []))
      .catch(() => setError('Không thể tải danh sách đơn đặt của bạn.'))
      .finally(() => setLoading(false));
  }, []);

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
    <div className="section-container">
      <PageTitle eyebrow="TÀI KHOẢN CỦA TÔI" title="Lịch sử đặt vé bơi">
        Quản lý các đơn đặt lịch, mã vé điện tử QR để check-in và trạng thái hoàn tiền khi hủy vé.
      </PageTitle>

      {flashMessage && (
        <div className="alert-box alert-success" style={{ marginBottom: '1.5rem' }}>
          <CheckCircle2 size={18} />
          <span>{flashMessage}</span>
        </div>
      )}

      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} />
      ) : bookings.length === 0 ? (
        <div className="card empty-bookings-card">
          <EmptyState message="Bạn chưa có đơn đặt lịch bơi nào." />
          <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
            <Link to="/schedules" className="btn btn-primary">
              Khám phá ca bơi ngay <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      ) : (
        <div className="bookings-list">
          {bookings.map(b => (
            <div key={b.maDat} className="card booking-item-card">
              <div className="booking-item-header">
                <div>
                  <span className="booking-code">Mã đơn: <b>{b.maDat}</b></span>
                  <span className={`status-tag ${getStatusBadgeClass(b.trangThai)}`}>
                    {statusText(b.trangThai)}
                  </span>
                </div>
                <div className="booking-item-price">
                  {money(b.tongTien)}
                </div>
              </div>

              <div className="booking-item-body">
                <div className="item-detail-column">
                  <h3 className="item-service-title">{b.dichVu}</h3>
                  <p className="item-sub-info"><MapPin size={15} /> {b.hoBoi} — {b.khuVuc}</p>
                  <p className="item-sub-info"><Calendar size={15} /> {dateText(b.ngay)} · <Clock size={15} /> {b.gioBatDau?.slice(0, 5)}–{b.gioKetThuc?.slice(0, 5)}</p>
                  <p className="item-sub-info"><Users size={15} /> Số lượng: <b>{b.soNguoi} vé</b></p>
                </div>

                <div className="item-action-column">
                  {b.maVeQr ? (
                    <div className="qr-badge-preview">
                      <QrCode size={20} />
                      <span>{b.maVeQr}</span>
                    </div>
                  ) : b.trangThai === 'CHO_THANH_TOAN' ? (
                    <span className="badge-warning-text">Chưa thanh toán</span>
                  ) : null}

                  <Link to={`/my/bookings/${b.maDat}`} className="btn btn-outline">
                    Xem chi tiết & vé <ArrowRight size={14} />
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
