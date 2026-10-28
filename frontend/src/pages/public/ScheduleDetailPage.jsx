import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Calendar, Clock, MapPin, Users, Ticket, ArrowLeft, ShieldCheck } from 'lucide-react';
import { scheduleService } from '../../services/scheduleService';
import { useAuth } from '../../context/AuthContext';
import { Loading, ErrorState, PageTitle } from '../../components/States';
import { money, dateText, statusText } from '../../utils/view';

export default function ScheduleDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [schedule, setSchedule] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    scheduleService.get(id)
      .then(res => setSchedule(res.data.data))
      .catch(() => setError('Không tìm thấy thông tin ca bơi này.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleBook = () => {
    const checkoutPath = `/checkout/${id}`;
    if (!user) {
      navigate(`/login?redirect=${encodeURIComponent(checkoutPath)}`, {
        state: { message: 'Bạn cần đăng nhập để tiếp tục đặt vé.' }
      });
    } else {
      navigate(checkoutPath);
    }
  };

  if (loading) return <div className="section-container"><Loading /></div>;
  if (error || !schedule) return <div className="section-container"><ErrorState message={error || 'Không tìm thấy ca bơi'} /></div>;

  const isAvailable = schedule.trangThai === 'MO' && schedule.soChoCon > 0;

  return (
    <div className="section-container">
      <Link to="/schedules" className="btn-back">
        <ArrowLeft size={16} /> Quay lại danh sách ca bơi
      </Link>

      <div className="schedule-detail-layout">
        <div className="schedule-detail-main">
          <div className="detail-hero">
            <span className="badge-pill">{schedule.dichVu}</span>
            <h1>{schedule.hoBoi}</h1>
            <p className="detail-sub"><MapPin size={18} /> {schedule.khuVuc}</p>
          </div>

          <div className="detail-info-grid">
            <div className="info-box">
              <Calendar size={20} />
              <div>
                <small>Ngày bơi</small>
                <b>{dateText(schedule.ngay)}</b>
              </div>
            </div>

            <div className="info-box">
              <Clock size={20} />
              <div>
                <small>Khung giờ</small>
                <b>{schedule.gioBatDau?.slice(0, 5)} – {schedule.gioKetThuc?.slice(0, 5)}</b>
              </div>
            </div>

            <div className="info-box">
              <Users size={20} />
              <div>
                <small>Chỗ còn lại</small>
                <b style={{ color: schedule.soChoCon > 0 ? 'var(--primary-dark)' : 'var(--danger)' }}>
                  {schedule.soChoCon} / {schedule.sucChua} chỗ
                </b>
              </div>
            </div>

            <div className="info-box">
              <ShieldCheck size={20} />
              <div>
                <small>Trạng thái ca</small>
                <b>{statusText(schedule.trangThai)}</b>
              </div>
            </div>
          </div>

          <div className="schedule-policy-box">
            <h3>Quy định & Chính sách hủy vé</h3>
            <ul>
              <li>Check-in mở trước giờ bắt đầu ca bơi 15 phút và đóng khi kết thúc ca.</li>
              <li>Hủy trước ngày bơi: Hoàn tiền <b>100%</b> giá trị vé.</li>
              <li>Hủy trong ngày bơi, trước giờ bắt đầu: Hoàn tiền <b>50%</b> giá trị vé.</li>
              <li>Hủy khi ca bơi đang diễn ra: Không hoàn tiền.</li>
              <li>Sau khi ca bơi kết thúc hoặc vé đã check-in: Không thể hủy.</li>
            </ul>
          </div>
        </div>

        {/* Booking Card Sidebar */}
        <div className="schedule-booking-sidebar">
          <div className="card booking-summary-box">
            <div className="price-display">
              <small>Giá vé niêm yết</small>
              <div className="price-tag">{money(schedule.donGia)} <span style={{ fontSize: '0.85rem', fontWeight: 400 }}>/ người</span></div>
            </div>

            <div className="booking-status-indicator">
              {isAvailable ? (
                <span className="badge-available">Còn chỗ đặt vé ({schedule.soChoCon} chỗ trống)</span>
              ) : (
                <span className="badge-unavailable">Hết chỗ hoặc ca đã đóng</span>
              )}
            </div>

            <button
              className="btn btn-primary btn-large"
              style={{ width: '100%', marginTop: '1.5rem' }}
              disabled={!isAvailable}
              onClick={handleBook}
            >
              <Ticket size={20} /> Đặt vé ngay
            </button>

            {!user && (
              <p className="login-hint">
                Bạn chưa đăng nhập? Bạn sẽ được chuyển tới trang đăng nhập và tự động quay lại đây.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
