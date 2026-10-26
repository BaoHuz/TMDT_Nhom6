import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Waves, MapPin, Clock, Users, Calendar, ArrowLeft } from 'lucide-react';
import { catalogService } from '../../services/catalogService';
import { scheduleService } from '../../services/scheduleService';
import ScheduleCard from '../../components/ScheduleCard';
import { Loading, ErrorState, EmptyState } from '../../components/States';

export default function PoolDetailPage() {
  const { id } = useParams();
  const [pool, setPool] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;
    Promise.all([
      catalogService.pool(id),
      scheduleService.list({ maHoBoi: id })
    ])
      .then(([poolRes, schedulesRes]) => {
        if (!mounted) return;
        setPool(poolRes.data.data);
        setSchedules(schedulesRes.data.data || []);
      })
      .catch(err => {
        if (mounted) setError('Không tìm thấy thông tin hồ bơi hoặc có lỗi xảy ra.');
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });

    return () => { mounted = false; };
  }, [id]);

  if (loading) return <div className="section-container"><Loading /></div>;
  if (error || !pool) return <div className="section-container"><ErrorState message={error || 'Hồ bơi không tồn tại'} /></div>;

  return (
    <div className="section-container">
      <Link to="/pools" className="btn-back">
        <ArrowLeft size={16} /> Quay lại danh sách hồ bơi
      </Link>

      <div className="detail-hero">
        <div className="detail-hero-body">
          <div className="badge-pill">Cơ sở hồ bơi</div>
          <h1>{pool.tenHoBoi}</h1>
          <p className="pool-address"><MapPin size={18} /> {pool.diaChi}</p>
          <div className="pool-meta-chips">
            <span className="chip"><Clock size={16} /> Giờ mở cửa: {pool.gioMoCua?.slice(0, 5)} - {pool.gioDongCua?.slice(0, 5)}</span>
            <span className={`status-tag ${pool.trangThai === 'HOAT_DONG' ? 'tag-green' : 'tag-gray'}`}>
              {pool.trangThai === 'HOAT_DONG' ? 'Đang hoạt động' : 'Tạm dừng hoạt động'}
            </span>
          </div>
        </div>
      </div>

      {/* Areas in this pool */}
      <div className="detail-sub-section">
        <h2>Các khu vực tại hồ</h2>
        <p>Hồ bơi được chia thành nhiều khu vực với quy mô và sức chứa đa dạng:</p>
        <div className="grid-responsive-3" style={{ marginTop: '1.25rem' }}>
          {pool.khuVucList && pool.khuVucList.length > 0 ? (
            pool.khuVucList.map(kv => (
              <div key={kv.maKhuVuc} className="card area-card">
                <div className="area-icon">
                  <Waves size={24} />
                </div>
                <h3>{kv.tenKhuVuc}</h3>
                <p><Users size={16} /> Sức chứa tối đa: <b>{kv.sucChua} người</b></p>
                <span className={`status-tag ${kv.trangThai === 'HOAT_DONG' ? 'tag-green' : 'tag-gray'}`}>
                  {kv.trangThai === 'HOAT_DONG' ? 'Hoạt động' : 'Tạm đóng'}
                </span>
              </div>
            ))
          ) : (
            <EmptyState message="Chưa có thông tin khu vực cho hồ bơi này." />
          )}
        </div>
      </div>

      {/* Schedules in this pool */}
      <div className="detail-sub-section" style={{ marginTop: '3rem', marginBottom: '4rem' }}>
        <div className="section-header-row">
          <div>
            <h2>Lịch ca bơi tại {pool.tenHoBoi}</h2>
            <p>Các ca bơi sắp diễn ra tại hồ bơi này</p>
          </div>
          <Link to={`/schedules?maHoBoi=${pool.maHoBoi}`} className="view-more-link">
            Xem tất cả ca bơi <Calendar size={16} />
          </Link>
        </div>

        {schedules.length === 0 ? (
          <EmptyState message="Hiện chưa có lịch ca bơi mở tại hồ bơi này." />
        ) : (
          <div className="grid-responsive-3" style={{ marginTop: '1.25rem' }}>
            {schedules.map(item => (
              <ScheduleCard key={item.maLich} item={item} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
