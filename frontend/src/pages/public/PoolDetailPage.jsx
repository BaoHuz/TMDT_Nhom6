import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Waves, MapPin, Clock, Users, Calendar, ArrowLeft, X, ChevronLeft, ChevronRight, Images } from 'lucide-react';
import { catalogService } from '../../services/catalogService';
import { scheduleService } from '../../services/scheduleService';
import ScheduleCard from '../../components/ScheduleCard';
import { Loading, ErrorState, EmptyState } from '../../components/States';
import { assetUrl } from '../../utils/view';

export default function PoolDetailPage() {
  const { id } = useParams();
  const [pool, setPool] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lightboxIndex, setLightboxIndex] = useState(null);

  useEffect(() => {
    let mounted = true;
    Promise.all([
      catalogService.pool(id),
      scheduleService.list({ maHoBoi: id })
    ])
      .then(([poolRes, schedulesRes]) => {
        if (!mounted) return;
        const detail = poolRes.data.data;
        setPool({
          ...detail.pool,
          khuVucList: detail.areas || [],
          images: detail.images || []
        });
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

  const images = pool?.images || [];
  const closeLightbox = () => setLightboxIndex(null);
  const showPrevious = () => setLightboxIndex(index => (index - 1 + images.length) % images.length);
  const showNext = () => setLightboxIndex(index => (index + 1) % images.length);

  useEffect(() => {
    if (lightboxIndex === null) return undefined;
    const onKeyDown = event => {
      if (event.key === 'Escape') closeLightbox();
      if (event.key === 'ArrowLeft') showPrevious();
      if (event.key === 'ArrowRight') showNext();
    };
    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', onKeyDown);
    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', onKeyDown);
    };
  }, [lightboxIndex, images.length]);

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

      <section className="pool-gallery" aria-label="Hình ảnh hồ bơi">
        {images.length > 0 ? (
          <>
            <button className="pool-gallery-main" onClick={() => setLightboxIndex(0)} aria-label="Mở ảnh lớn">
              <img src={assetUrl(images[0].duongDan)} alt={`${pool.tenHoBoi} - ảnh 1`} />
            </button>
            {images.length > 1 && (
              <div className="pool-gallery-thumbnails">
                {images.slice(1, 5).map((image, index) => (
                  <button key={image.maAnh} onClick={() => setLightboxIndex(index + 1)} aria-label={`Mở ảnh ${index + 2}`}>
                    <img src={assetUrl(image.duongDan)} alt={`${pool.tenHoBoi} - ảnh ${index + 2}`} />
                  </button>
                ))}
              </div>
            )}
            <button className="pool-gallery-all" onClick={() => setLightboxIndex(0)}>
              <Images size={17} /> Xem tất cả {images.length} ảnh
            </button>
          </>
        ) : (
          <div className="pool-gallery-empty"><Waves size={42} /><span>Hồ bơi chưa có hình ảnh</span></div>
        )}
      </section>

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

      {lightboxIndex !== null && images[lightboxIndex] && (
        <div className="pool-lightbox" role="dialog" aria-modal="true" aria-label="Xem hình ảnh hồ bơi" onMouseDown={event => event.target === event.currentTarget && closeLightbox()}>
          <button className="lightbox-close" onClick={closeLightbox} aria-label="Đóng"><X size={26} /></button>
          <button className="lightbox-nav lightbox-prev" onClick={showPrevious} aria-label="Ảnh trước"><ChevronLeft size={34} /></button>
          <div className="lightbox-content">
            <img className="lightbox-main-image" src={assetUrl(images[lightboxIndex].duongDan)} alt={`${pool.tenHoBoi} - ảnh ${lightboxIndex + 1}`} />
            <div className="lightbox-counter">Ảnh {lightboxIndex + 1} / {images.length}</div>
            <div className="lightbox-thumbnails">
              {images.map((image, index) => (
                <button key={image.maAnh} className={index === lightboxIndex ? 'active' : ''} onClick={() => setLightboxIndex(index)} aria-label={`Chọn ảnh ${index + 1}`}>
                  <img src={assetUrl(image.duongDan)} alt="" />
                </button>
              ))}
            </div>
          </div>
          <button className="lightbox-nav lightbox-next" onClick={showNext} aria-label="Ảnh sau"><ChevronRight size={34} /></button>
        </div>
      )}
    </div>
  );
}
