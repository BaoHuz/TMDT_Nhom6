import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { DollarSign, ShoppingCart, CheckCircle, XCircle, UserCheck, Calendar, ArrowUpRight, Waves, Layers, Tag, Users } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { Loading, ErrorState, PageTitle } from '../../components/States';
import { money } from '../../utils/view';

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    adminService.dashboard()
      .then(res => setStats(res.data.data))
      .catch(() => setError('Không thể tải dữ liệu thống kê quản trị.'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading />;
  if (error) return <ErrorState message={error} />;

  return (
    <div className="admin-page-container">
      <PageTitle eyebrow="TỔNG QUAN HỆ THỐNG" title="Bảng điều khiển AquaFlow">
        Theo dõi doanh thu, số lượng đơn đặt vé và tình hình vận hành các cơ sở hồ bơi.
      </PageTitle>

      {/* KPI Cards */}
      <div className="kpi-grid">
        <div className="card kpi-card">
          <div className="kpi-icon icon-green">
            <DollarSign size={24} />
          </div>
          <div className="kpi-info">
            <small>Doanh thu sau khi trừ yêu cầu/hoàn tiền</small>
            <div className="kpi-value">{money(stats?.doanhThu)}</div>
            <small className="kpi-hint">
              Chờ xử lý/đối soát: {money(stats?.tienHoanChoXuLy)} · Đã hoàn thực tế: {money(stats?.tienDaHoanThucTe)}
            </small>
          </div>
        </div>

        <div className="card kpi-card">
          <div className="kpi-icon icon-blue">
            <ShoppingCart size={24} />
          </div>
          <div className="kpi-info">
            <small>Tổng đơn đặt vé</small>
            <div className="kpi-value">{stats?.tongDon || 0}</div>
          </div>
        </div>

        <div className="card kpi-card">
          <div className="kpi-icon icon-emerald">
            <CheckCircle size={24} />
          </div>
          <div className="kpi-info">
            <small>Đơn đã xác nhận</small>
            <div className="kpi-value">{stats?.donDaXacNhan || 0}</div>
          </div>
        </div>

        <div className="card kpi-card">
          <div className="kpi-icon icon-red">
            <XCircle size={24} />
          </div>
          <div className="kpi-info">
            <small>Đơn đã hủy</small>
            <div className="kpi-value">{stats?.donDaHuy || 0}</div>
          </div>
        </div>

        <div className="card kpi-card">
          <div className="kpi-icon icon-purple">
            <UserCheck size={24} />
          </div>
          <div className="kpi-info">
            <small>Lượt check-in thành công</small>
            <div className="kpi-value">{stats?.luotCheckIn || 0}</div>
          </div>
        </div>

        <div className="card kpi-card">
          <div className="kpi-icon icon-cyan">
            <Calendar size={24} />
          </div>
          <div className="kpi-info">
            <small>Ca bơi đang mở hôm nay</small>
            <div className="kpi-value">{stats?.caDangMo || 0}</div>
          </div>
        </div>
      </div>

      {/* Quick Access Grid */}
      <div style={{ marginTop: '2.5rem' }}>
        <h3 style={{ marginBottom: '1rem', color: 'var(--text-dark)' }}>Truy cập nhanh chức năng quản trị</h3>
        <div className="quick-access-grid">
          {[
            { to: '/admin/pools', title: 'Quản lý Hồ bơi', desc: 'Thêm, sửa thông tin hồ và giờ mở/đóng cửa', icon: <Waves size={24} /> },
            { to: '/admin/areas', title: 'Quản lý Khu vực', desc: 'Thiết lập các phân khu và sức chứa tối đa', icon: <Layers size={24} /> },
            { to: '/admin/services', title: 'Dịch vụ & Vé', desc: 'Cấu hình bảng giá và gói vé đang mở bán', icon: <Tag size={24} /> },
            { to: '/admin/schedules', title: 'Quản lý Ca bơi', desc: 'Mở ca mới, điều phối sức chứa và đóng ca', icon: <Calendar size={24} /> },
            { to: '/admin/bookings', title: 'Giao dịch & Đơn', desc: 'Tra cứu lịch sử đặt vé, mã QR và trạng thái', icon: <ShoppingCart size={24} /> },
            { to: '/admin/accounts', title: 'Tài khoản & Phân quyền', desc: 'Quản lý tài khoản, tạo STAFF và khóa/mở khóa', icon: <Users size={24} /> }
          ].map(item => (
            <Link key={item.to} to={item.to} className="card quick-access-card">
              <div className="quick-icon">{item.icon}</div>
              <div className="quick-body">
                <h4>{item.title}</h4>
                <p>{item.desc}</p>
              </div>
              <ArrowUpRight size={18} className="quick-arrow" />
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
