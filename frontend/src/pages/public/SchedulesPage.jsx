import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Filter, Calendar, RotateCcw } from 'lucide-react';
import { scheduleService } from '../../services/scheduleService';
import { catalogService } from '../../services/catalogService';
import ScheduleCard from '../../components/ScheduleCard';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';

export default function SchedulesPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [date, setDate] = useState(searchParams.get('date') || '');
  const [maHoBoi, setMaHoBoi] = useState(searchParams.get('maHoBoi') || '');
  const [maKhuVuc, setMaKhuVuc] = useState(searchParams.get('maKhuVuc') || '');
  const [maDv, setMaDv] = useState(searchParams.get('maDv') || '');
  const [sort, setSort] = useState(searchParams.get('sort') || 'earliest');

  const [pools, setPools] = useState([]);
  const [areas, setAreas] = useState([]);
  const [services, setServices] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load catalog data for filters
  useEffect(() => {
    Promise.all([
      catalogService.pools(),
      catalogService.services()
    ]).then(([poolsRes, servicesRes]) => {
      setPools(poolsRes.data.data || []);
      setServices(servicesRes.data.data || []);
    }).catch(() => {});
  }, []);

  // When pool changes, load areas
  useEffect(() => {
    if (maHoBoi) {
      catalogService.areas(maHoBoi)
        .then(res => setAreas(res.data.data || []))
        .catch(() => setAreas([]));
    } else {
      catalogService.areas()
        .then(res => setAreas(res.data.data || []))
        .catch(() => setAreas([]));
    }
  }, [maHoBoi]);

  // Load schedules based on filters
  useEffect(() => {
    setLoading(true);
    setError(null);

    const params = {};
    if (date) params.date = date;
    if (maHoBoi) params.maHoBoi = maHoBoi;
    if (maKhuVuc) params.maKhuVuc = maKhuVuc;
    if (maDv) params.maDv = maDv;
    if (sort) params.sort = sort;

    scheduleService.list(params)
      .then(res => {
        setSchedules(res.data.data || []);
      })
      .catch(err => {
        setError('Không thể tải danh sách ca bơi.');
      })
      .finally(() => {
        setLoading(false);
      });
  }, [date, maHoBoi, maKhuVuc, maDv, sort]);

  const handleReset = () => {
    setDate('');
    setMaHoBoi('');
    setMaKhuVuc('');
    setMaDv('');
    setSort('earliest');
    setSearchParams({});
  };

  return (
    <div className="section-container">
      <PageTitle eyebrow="LỊCH BƠI CÔNG CỘNG" title="Tìm ca bơi & Giữ chỗ trực tuyến">
        Tra cứu lịch hoạt động của các hồ bơi theo ngày, khu vực và gói dịch vụ mong muốn.
      </PageTitle>

      {/* Filter panel */}
      <div className="filter-panel-card">
        <div className="filter-panel-title">
          <Filter size={18} />
          <span>Bộ lọc tìm kiếm</span>
        </div>

        <div className="filter-grid">
          <div className="form-group">
            <label>Ngày bơi</label>
            <input
              type="date"
              className="form-input"
              value={date}
              onChange={e => setDate(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Hồ bơi</label>
            <select
              className="form-input"
              value={maHoBoi}
              onChange={e => {
                setMaHoBoi(e.target.value);
                setMaKhuVuc('');
              }}
            >
              <option value="">Tất cả hồ bơi</option>
              {pools.map(p => (
                <option key={p.maHoBoi} value={p.maHoBoi}>{p.tenHoBoi}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Khu vực hồ</label>
            <select
              className="form-input"
              value={maKhuVuc}
              onChange={e => setMaKhuVuc(e.target.value)}
            >
              <option value="">Tất cả khu vực</option>
              {areas.map(a => (
                <option key={a.maKhuVuc} value={a.maKhuVuc}>{a.tenKhuVuc}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Dịch vụ / Loại vé</label>
            <select
              className="form-input"
              value={maDv}
              onChange={e => setMaDv(e.target.value)}
            >
              <option value="">Tất cả dịch vụ</option>
              {services.map(s => (
                <option key={s.maDv} value={s.maDv}>{s.tenDv}</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Sắp xếp theo</label>
            <select
              className="form-input"
              value={sort}
              onChange={e => setSort(e.target.value)}
            >
              <option value="earliest">Giờ sớm nhất</option>
              <option value="latest">Giờ muộn nhất</option>
              <option value="priceAsc">Giá: Thấp đến Cao</option>
              <option value="priceDesc">Giá: Cao đến Thấp</option>
              <option value="available">Chỗ trống nhiều nhất</option>
            </select>
          </div>

          <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end' }}>
            <button
              className="btn btn-secondary"
              style={{ width: '100%', height: '42px' }}
              onClick={handleReset}
            >
              <RotateCcw size={15} /> Xóa bộ lọc
            </button>
          </div>
        </div>
      </div>

      {/* Results */}
      <div style={{ marginTop: '2rem', marginBottom: '4rem' }}>
        <div style={{ marginBottom: '1rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          Tìm thấy <b>{schedules.length}</b> ca bơi phù hợp
        </div>

        {loading ? (
          <Loading />
        ) : error ? (
          <ErrorState message={error} />
        ) : schedules.length === 0 ? (
          <EmptyState message="Không tìm thấy ca bơi nào theo tiêu chí đã chọn. Vui lòng thử chọn ngày khác hoặc mở rộng bộ lọc." />
        ) : (
          <div className="grid-responsive-3">
            {schedules.map(item => (
              <ScheduleCard key={item.maLich} item={item} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
