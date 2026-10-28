import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Waves, Calendar, Clock, MapPin, ShieldCheck, CreditCard, QrCode, ArrowRight } from 'lucide-react';
import { catalogService } from '../../services/catalogService';
import { scheduleService } from '../../services/scheduleService';
import ScheduleCard from '../../components/ScheduleCard';
import { Loading, ErrorState, EmptyState } from '../../components/States';
import { money } from '../../utils/view';

export default function HomePage() {
  const [pools, setPools] = useState([]);
  const [services, setServices] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mounted = true;
    Promise.all([
      catalogService.pools(),
      catalogService.services(),
      scheduleService.list()
    ])
      .then(([poolsRes, servicesRes, schedulesRes]) => {
        if (!mounted) return;
        setPools(poolsRes.data.data?.slice(0, 4) || []);
        setServices(servicesRes.data.data?.filter(s => s.trangThai === 'DANG_BAN').slice(0, 4) || []);
        setSchedules(schedulesRes.data.data?.slice(0, 6) || []);
      })
      .catch(err => {
        if (mounted) setError('Không thể tải thông tin trang chủ.');
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });

    return () => { mounted = false; };
  }, []);

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero">
        <div className="hero-content">
          <span className="badge-pill">Nền tảng đặt vé bơi thông minh</span>
          <h1>Đặt lịch bơi công cộng dễ dàng hơn</h1>
          <p>
            Chọn hồ bơi yêu thích, tìm ca bơi phù hợp, thanh toán an toàn và nhận mã QR check-in tiện lợi chỉ trong vài bước.
          </p>
          <div className="hero-actions">
            <Link to="/schedules" className="btn btn-primary">
              <Calendar size={18} /> Xem lịch bơi
            </Link>
            <Link to="/pools" className="btn btn-secondary">
              <MapPin size={18} /> Khám phá hồ bơi
            </Link>
          </div>
        </div>
      </section>

      {/* Booking Process Steps */}
      <section className="section-container">
        <div className="section-header">
          <h2>Quy trình đặt vé đơn giản</h2>
          <p>Trải nghiệm dịch vụ bơi lội trọn vẹn chỉ với 6 bước nhanh chóng</p>
        </div>
        <div className="steps-grid">
          {[
            { step: '01', title: 'Chọn hồ bơi & ca', desc: 'Tìm kiếm hồ bơi gần bạn và chọn khung giờ bơi yêu thích.' },
            { step: '02', title: 'Đăng nhập tài khoản', desc: 'Đăng nhập hoặc đăng ký nhanh tài khoản thành viên.' },
            { step: '03', title: 'Xác nhận thông tin', desc: 'Chọn số lượng vé bơi và kiểm tra chi tiết đơn hàng.' },
            { step: '04', title: 'Thanh toán mô phỏng', desc: 'Xác thực thanh toán an toàn qua cổng mô phỏng.' },
            { step: '05', title: 'Nhận mã QR vé', desc: 'Mã QR vé được cấp ngay lập tức trong đơn của bạn.' },
            { step: '06', title: 'Check-in tại hồ', desc: 'Quét mã QR tại quầy tiếp đón để vào hồ bơi bơi ngay.' }
          ].map(item => (
            <div key={item.step} className="step-card">
              <span className="step-num">{item.step}</span>
              <h3>{item.title}</h3>
              <p>{item.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Featured Pools */}
      <section className="section-container">
        <div className="section-header-row">
          <div>
            <h2>Hồ bơi nổi bật</h2>
            <p>Hệ thống hồ bơi đạt tiêu chuẩn vệ sinh và an toàn</p>
          </div>
          <Link to="/pools" className="view-more-link">
            Xem tất cả <ArrowRight size={16} />
          </Link>
        </div>
        {loading ? (
          <Loading />
        ) : error ? (
          <ErrorState message={error} />
        ) : pools.length === 0 ? (
          <EmptyState message="Hiện chưa có hồ bơi nào hoạt động." />
        ) : (
          <div className="grid-responsive-3">
            {pools.map(pool => (
              <div key={pool.maHoBoi} className="card pool-card">
                <div className="card-header-art">
                  <Waves size={32} />
                  <span className={`status-tag ${pool.trangThai === 'HOAT_DONG' ? 'tag-green' : 'tag-gray'}`}>
                    {pool.trangThai === 'HOAT_DONG' ? 'Hoạt động' : 'Tạm đóng'}
                  </span>
                </div>
                <div className="card-body">
                  <h3>{pool.tenHoBoi}</h3>
                  <p className="pool-address"><MapPin size={15} /> {pool.diaChi}</p>
                  <p className="pool-time"><Clock size={15} /> Mở cửa: {pool.gioMoCua?.slice(0, 5)} - {pool.gioDongCua?.slice(0, 5)}</p>
                  <Link to={`/pools/${pool.maHoBoi}`} className="btn btn-outline" style={{ marginTop: '1rem', width: '100%', textAlign: 'center' }}>
                    Xem chi tiết & các ca
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Upcoming Schedules */}
      <section className="section-container">
        <div className="section-header-row">
          <div>
            <h2>Ca bơi sắp tới</h2>
            <p>Chọn ca bơi gần nhất và giữ chỗ ngay hôm nay</p>
          </div>
          <Link to="/schedules" className="view-more-link">
            Xem lịch đầy đủ <ArrowRight size={16} />
          </Link>
        </div>
        {loading ? (
          <Loading />
        ) : error ? (
          <ErrorState message={error} />
        ) : schedules.length === 0 ? (
          <EmptyState message="Không có ca bơi nào đang mở." />
        ) : (
          <div className="grid-responsive-3">
            {schedules.map(item => (
              <ScheduleCard key={item.maLich} item={item} />
            ))}
          </div>
        )}
      </section>

      {/* Services List */}
      <section className="section-container" style={{ marginBottom: '4rem' }}>
        <div className="section-header-row">
          <div>
            <h2>Dịch vụ & Gói vé</h2>
            <p>Đa dạng loại vé bơi phù hợp cho cả gia đình và người luyện tập</p>
          </div>
          <Link to="/services" className="view-more-link">
            Xem bảng giá <ArrowRight size={16} />
          </Link>
        </div>
        {loading ? (
          <Loading />
        ) : error ? (
          <ErrorState message={error} />
        ) : services.length === 0 ? (
          <EmptyState message="Hiện chưa có dịch vụ nào đang mở bán." />
        ) : (
          <div className="grid-responsive-4">
            {services.map(svc => (
              <div key={svc.maDv} className="card service-card">
                <span className="service-type-badge">{svc.loaiDv}</span>
                <h3>{svc.tenDv}</h3>
                <div className="service-price">{money(svc.donGia)}</div>
                <p className="service-duration"><Clock size={14} /> Thời lượng: {svc.thoiLuongPhut} phút</p>
                <Link to={`/schedules?maDv=${svc.maDv}`} className="btn btn-outline" style={{ width: '100%', textAlign: 'center' }}>
                  Tìm ca bơi
                </Link>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
