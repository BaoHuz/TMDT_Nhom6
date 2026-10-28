import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { Waves, LogOut, User, TicketCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function StaffLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="staff-shell">
      <header className="staff-header">
        <div className="staff-header-inner">
          <div className="staff-brand-group">
            <Link className="logo" to="/">
              <div className="logo-icon"><Waves size={23} /></div>
              <span>Aqua<strong>Flow</strong></span>
            </Link>
            <span className="staff-role-chip">STAFF</span>
          </div>

          <div className="staff-header-section">
            <TicketCheck size={18} />
            <span>Check-in vé</span>
          </div>

          <div className="staff-user-control">
            <div className="staff-user-profile">
              <span className="staff-avatar"><User size={18} /></span>
              <span className="staff-user-copy">
                <strong>{user?.fullName || user?.username || 'Nhân viên'}</strong>
                <small>STAFF · Nhân viên tiếp đón</small>
              </span>
            </div>
            <button className="btn-staff-logout" onClick={handleLogout} title="Đăng xuất">
              <LogOut size={17} /> <span>Đăng xuất</span>
            </button>
          </div>
        </div>
      </header>
      <main className="staff-main"><Outlet /></main>
    </div>
  );
}
