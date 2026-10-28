import React from 'react';
import { NavLink, Outlet, useNavigate, Link } from 'react-router-dom';
import {
  Waves,
  LayoutDashboard,
  MapPin,
  Layers,
  Tag,
  Calendar,
  ShoppingCart,
  Users,
  LogOut,
  ExternalLink,
  Shield
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const navItems = [
    { to: '/admin', label: 'Bảng điều khiển', icon: <LayoutDashboard size={18} />, end: true },
    { to: '/admin/pools', label: 'Quản lý Hồ bơi', icon: <MapPin size={18} /> },
    { to: '/admin/areas', label: 'Quản lý Khu vực', icon: <Layers size={18} /> },
    { to: '/admin/services', label: 'Dịch vụ & Gói vé', icon: <Tag size={18} /> },
    { to: '/admin/schedules', label: 'Lịch & Ca bơi', icon: <Calendar size={18} /> },
    { to: '/admin/bookings', label: 'Đơn hàng & Vé', icon: <ShoppingCart size={18} /> },
    { to: '/admin/accounts', label: 'Tài khoản & Phân quyền', icon: <Users size={18} /> },
  ];

  return (
    <div className="admin-portal">
      {/* Sidebar */}
      <aside className="admin-sidebar">
        <div className="admin-sidebar-header">
          <Link to="/" className="admin-brand">
            <div className="logo-icon">
              <Waves size={22} />
            </div>
            <span>Aqua<strong>Admin</strong></span>
          </Link>
          <span className="admin-badge-role">QUẢN TRỊ VIÊN</span>
        </div>

        <nav className="admin-nav">
          <div className="nav-group-label">VẬN HÀNH & KINH DOANH</div>
          {navItems.map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `admin-nav-item ${isActive ? 'active' : ''}`}
            >
              {item.icon}
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="admin-sidebar-footer">
          <Link to="/" className="btn-view-site" target="_blank" rel="noopener noreferrer">
            <ExternalLink size={16} /> Xem trang khách
          </Link>
        </div>
      </aside>

      {/* Main Admin Section */}
      <section className="admin-body">
        {/* Top Header */}
        <header className="admin-header">
          <div className="admin-header-title">
            <Shield size={18} className="header-icon" />
            <span>Phân hệ Quản trị Hệ thống AquaFlow</span>
          </div>

          <div className="admin-user-control">
            <div className="admin-user-info">
              <span className="user-name">{user?.fullName || user?.username}</span>
              <span className="user-role-badge">ADMIN</span>
            </div>
            <button className="btn-admin-logout" onClick={handleLogout} title="Đăng xuất">
              <LogOut size={16} /> <span>Đăng xuất</span>
            </button>
          </div>
        </header>

        {/* Dynamic Admin Content */}
        <main className="admin-main">
          <Outlet />
        </main>
      </section>
    </div>
  );
}
