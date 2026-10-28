import React, { useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Waves, Menu, X, Calendar, MapPin, User, LogOut, Ticket, Shield } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function PublicLayout() {
  const { user, logout } = useAuth();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
    setMobileMenuOpen(false);
  };

  const closeMenu = () => setMobileMenuOpen(false);

  return (
    <div className="site">
      {/* Top Header */}
      <header className="public-header">
        <div className="header-inner">
          <Link to="/" className="logo" onClick={closeMenu}>
            <div className="logo-icon">
              <Waves size={24} />
            </div>
            <span>Aqua<strong>Flow</strong></span>
          </Link>

          {/* Desktop Navigation */}
          <nav className={`public-nav ${mobileMenuOpen ? 'open' : ''}`}>
            <div className="nav-main-links">
              <NavLink to="/" end onClick={closeMenu}>
                Trang chủ
              </NavLink>
              <NavLink to="/pools" onClick={closeMenu}>
                Hồ bơi
              </NavLink>
              <NavLink to="/services" onClick={closeMenu}>
                Dịch vụ & Vé
              </NavLink>
              <NavLink to="/schedules" onClick={closeMenu}>
                Lịch bơi
              </NavLink>
            </div>

            <div className="nav-auth-links">
              {user ? (
                <>
                  {user.role === 'CUSTOMER' && (
                    <NavLink to="/my/bookings" className="nav-item-icon" onClick={closeMenu}>
                      <Ticket size={16} /> Đơn của tôi
                    </NavLink>
                  )}
                  {user.role === 'STAFF' && (
                    <NavLink to="/staff/check-in" className="nav-item-badge staff" onClick={closeMenu}>
                      <Shield size={16} /> Quầy Soát vé
                    </NavLink>
                  )}
                  {user.role === 'ADMIN' && (
                    <NavLink to="/admin" className="nav-item-badge admin" onClick={closeMenu}>
                      <Shield size={16} /> Trang Quản trị
                    </NavLink>
                  )}

                  <NavLink to="/my/profile" className="user-profile-pill" onClick={closeMenu}>
                    <div className="avatar-circle">
                      <User size={15} />
                    </div>
                    <span>{user.fullName || user.username}</span>
                  </NavLink>

                  <button className="btn-logout" onClick={handleLogout} title="Đăng xuất">
                    <LogOut size={16} /> <span>Đăng xuất</span>
                  </button>
                </>
              ) : (
                <>
                  <NavLink to="/login" className="btn btn-outline btn-sm" onClick={closeMenu}>
                    Đăng nhập
                  </NavLink>
                  <NavLink to="/register" className="btn btn-primary btn-sm" onClick={closeMenu}>
                    Đăng ký ngay
                  </NavLink>
                </>
              )}
            </div>
          </nav>

          {/* Mobile Menu Toggle Button */}
          <button
            className="mobile-toggle-btn"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle Navigation"
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="public-main">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="public-footer">
        <div className="footer-inner">
          <div className="footer-grid">
            <div className="footer-brand">
              <div className="footer-logo">
                <Waves size={24} />
                <span>Aqua<strong>Flow</strong></span>
              </div>
              <p>
                Hệ thống nền tảng đặt lịch và quản lý dịch vụ hồ bơi công cộng thông minh.
                Đặt vé bơi nhanh chóng, thanh toán trực tuyến bảo mật và check-in không chạm bằng mã QR.
              </p>
              <div className="footer-badges">
                <span className="badge-tech">React + Spring Boot</span>
                <span className="badge-tech">MS SQL Server</span>
              </div>
            </div>

            <div className="footer-column">
              <h4>Khám phá</h4>
              <ul>
                <li><Link to="/pools">Danh sách hồ bơi</Link></li>
                <li><Link to="/services">Bảng giá dịch vụ</Link></li>
                <li><Link to="/schedules">Lịch ca bơi công cộng</Link></li>
              </ul>
            </div>

            <div className="footer-column">
              <h4>Tài khoản</h4>
              <ul>
                {user ? (
                  <>
                    <li><Link to="/my/bookings">Lịch sử đặt vé</Link></li>
                    <li><Link to="/my/profile">Hồ sơ cá nhân</Link></li>
                    {user.role === 'ADMIN' && <li><Link to="/admin">Trang quản trị</Link></li>}
                    {user.role === 'STAFF' && <li><Link to="/staff/check-in">Quầy check-in</Link></li>}
                  </>
                ) : (
                  <>
                    <li><Link to="/login">Đăng nhập tài khoản</Link></li>
                    <li><Link to="/register">Đăng ký thành viên mới</Link></li>
                  </>
                )}
              </ul>
            </div>

            <div className="footer-column">
              <h4>Hỗ trợ & Thông tin</h4>
              <ul>
                <li><span>Tổng đài CSKH: 1900 6868</span></li>
                <li><span>Email: hotro@aquaflow.vn</span></li>
                <li><span>Giờ phục vụ: 05:30 - 21:00 hàng ngày</span></li>
              </ul>
            </div>
          </div>

          <div className="footer-bottom">
            <p>© 2026 AquaFlow — Đồ án môn học Thương mại điện tử (Nhóm 6). Mọi quyền được bảo lưu.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
