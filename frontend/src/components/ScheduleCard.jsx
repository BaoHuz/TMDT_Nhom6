import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { CalendarDays, MapPin, Users, Clock, ArrowRight, Waves } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { dateText, money } from '../utils/view';

export default function ScheduleCard({ item }) {
  const { user } = useAuth();
  const navigate = useNavigate();

  const handleBook = () => {
    const path = `/checkout/${item.maLich}`;
    if (!user) {
      navigate(`/login?redirect=${encodeURIComponent(path)}`, {
        state: { message: 'Bạn cần đăng nhập để tiếp tục đặt vé.' }
      });
    } else {
      navigate(path);
    }
  };

  const isAvailable = item.soChoCon > 0 && item.trangThai !== 'DONG';

  return (
    <article className="card schedule-card">
      <div className="schedule-card-top">
        <div className="schedule-top-left">
          <Waves size={18} className="schedule-icon-wave" />
          <span className="schedule-service-pill">{item.dichVu}</span>
        </div>
        <span className={`seats-badge ${item.soChoCon > 5 ? 'seats-plenty' : item.soChoCon > 0 ? 'seats-low' : 'seats-empty'}`}>
          {item.soChoCon > 0 ? `${item.soChoCon} chỗ trống` : 'Hết chỗ'}
        </span>
      </div>

      <div className="schedule-card-content">
        <h3 className="schedule-pool-name">{item.hoBoi}</h3>
        <p className="schedule-area-name">
          <MapPin size={14} /> {item.khuVuc}
        </p>

        <div className="schedule-meta-list">
          <div className="schedule-meta-row">
            <CalendarDays size={14} />
            <span>{dateText(item.ngay)}</span>
          </div>
          <div className="schedule-meta-row">
            <Clock size={14} />
            <span>{item.gioBatDau?.slice(0, 5)} – {item.gioKetThuc?.slice(0, 5)}</span>
          </div>
          <div className="schedule-meta-row">
            <Users size={14} />
            <span>Sức chứa: <b>{item.sucChua} người</b></span>
          </div>
        </div>
      </div>

      <div className="schedule-card-bottom">
        <div className="schedule-price-block">
          <span className="price-label">Giá vé</span>
          <span className="price-value">{money(item.donGia)}</span>
        </div>

        <div className="schedule-actions-block">
          <Link to={`/schedules/${item.maLich}`} className="btn btn-outline btn-sm">
            Chi tiết
          </Link>
          <button
            className="btn btn-primary btn-sm"
            disabled={!isAvailable}
            onClick={handleBook}
          >
            {isAvailable ? 'Đặt vé' : 'Hết vé'}
          </button>
        </div>
      </div>
    </article>
  );
}
