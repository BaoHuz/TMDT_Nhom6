import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Waves, MapPin, Clock } from 'lucide-react';
import { catalogService } from '../../services/catalogService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';

export default function PoolsPage() {
  const [pools, setPools] = useState([]);
  const [filter, setFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    catalogService.pools()
      .then(res => setPools(res.data.data || []))
      .catch(() => setError('Không thể tải danh sách hồ bơi.'))
      .finally(() => setLoading(false));
  }, []);

  const filteredPools = pools.filter(p => {
    if (filter === 'ALL') return true;
    return p.trangThai === filter;
  });

  return (
    <div className="section-container">
      <PageTitle eyebrow="DANH MỤC CƠ SỞ" title="Hệ thống Hồ bơi AquaFlow">
        Khám phá các hồ bơi công cộng tiêu chuẩn, trang thiết bị hiện đại và nguồn nước sạch đạt chuẩn an toàn y tế.
      </PageTitle>

      <div className="filter-bar">
        <label>Lọc theo trạng thái:</label>
        <div className="filter-tags">
          {[
            { id: 'ALL', label: 'Tất cả' },
            { id: 'HOAT_DONG', label: 'Đang hoạt động' },
            { id: 'TAM_DONG', label: 'Tạm đóng' }
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
      ) : filteredPools.length === 0 ? (
        <EmptyState message="Không tìm thấy hồ bơi nào theo bộ lọc." />
      ) : (
        <div className="grid-responsive-3">
          {filteredPools.map(pool => (
            <div key={pool.maHoBoi} className="card pool-card">
              <div className="card-header-art">
                <Waves size={36} />
                <span className={`status-tag ${pool.trangThai === 'HOAT_DONG' ? 'tag-green' : 'tag-gray'}`}>
                  {pool.trangThai === 'HOAT_DONG' ? 'Hoạt động' : 'Tạm đóng'}
                </span>
              </div>
              <div className="card-body">
                <h3>{pool.tenHoBoi}</h3>
                <p className="pool-address"><MapPin size={16} /> {pool.diaChi}</p>
                <p className="pool-time"><Clock size={16} /> Mở cửa: {pool.gioMoCua?.slice(0, 5)} - {pool.gioDongCua?.slice(0, 5)}</p>
                <div style={{ marginTop: '1.25rem' }}>
                  <Link to={`/pools/${pool.maHoBoi}`} className="btn btn-primary" style={{ width: '100%', textAlign: 'center' }}>
                    Xem chi tiết & Ca bơi
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
