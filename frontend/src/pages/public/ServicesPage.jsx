import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Clock, Calendar, CheckCircle2 } from 'lucide-react';
import { catalogService } from '../../services/catalogService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';
import { money, statusText } from '../../utils/view';

export default function ServicesPage() {
  const [services, setServices] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    catalogService.services()
      .then(res => setServices(res.data.data || []))
      .catch(() => setError('Không thể tải danh sách dịch vụ.'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = services.filter(s => {
    if (filter === 'ALL') return true;
    return s.trangThai === filter;
  });

  return (
    <div className="section-container">
      <PageTitle eyebrow="BẢNG GIÁ DỊCH VỤ" title="Dịch vụ & Vé bơi công cộng">
        Bảng giá niêm yết minh bạch, đa dạng lựa chọn cho cá nhân, trẻ em và người bơi luyện tập chuyên nghiệp.
      </PageTitle>

      <div className="filter-bar">
        <label>Trạng thái bán:</label>
        <div className="filter-tags">
          {[
            { id: 'ALL', label: 'Tất cả' },
            { id: 'DANG_BAN', label: 'Đang mở bán' },
            { id: 'TAM_DUNG', label: 'Tạm dừng' }
          ].map(t => (
            <button
              key={t.id}
              className={`filter-btn ${filter === t.id ? 'active' : ''}`}
              onClick={() => setFilter(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} />
      ) : filtered.length === 0 ? (
        <EmptyState message="Không có dịch vụ nào phù hợp." />
      ) : (
        <div className="grid-responsive-3" style={{ marginTop: '1.5rem', marginBottom: '4rem' }}>
          {filtered.map(svc => (
            <div key={svc.maDv} className="card service-pricing-card">
              <div className="service-header">
                <span className="badge-pill">{svc.loaiDv}</span>
                <span className={`status-tag ${svc.trangThai === 'DANG_BAN' ? 'tag-green' : 'tag-gray'}`}>
                  {statusText(svc.trangThai)}
                </span>
              </div>
              <h3 className="service-title">{svc.tenDv}</h3>
              <div className="service-price-tag">
                <span className="price-num">{money(svc.donGia)}</span>
                <span className="price-unit">/ lượt bơi</span>
              </div>
              <div className="service-features">
                <div className="feat-item">
                  <Clock size={16} /> Thời lượng ca: <b>{svc.thoiLuongPhut} phút</b>
                </div>
                <div className="feat-item">
                  <CheckCircle2 size={16} /> Đã bao gồm tủ đồ cá nhân
                </div>
                <div className="feat-item">
                  <CheckCircle2 size={16} /> Nước tắm tráng sạch sẽ
                </div>
              </div>
              <div style={{ marginTop: 'auto', paddingTop: '1.5rem' }}>
                <Link
                  to={`/schedules?maDv=${svc.maDv}`}
                  className="btn btn-primary"
                  style={{ width: '100%', textAlign: 'center' }}
                >
                  <Calendar size={16} /> Tìm ca & Đặt vé
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
