import React, { useState } from 'react';
import { useNavigate, useSearchParams, useLocation, Link } from 'react-router-dom';
import { LogIn, Waves, AlertCircle } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function LoginPage() {
  const [searchParams] = useSearchParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuth();

  const redirectUrl = searchParams.get('redirect') || '';
  const initialMessage = location.state?.message || '';

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const fillSample = (sampleEmail, samplePass) => {
    setEmail(sampleEmail);
    setPassword(samplePass);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login({ email, password });

      if (redirectUrl) {
        navigate(redirectUrl, { replace: true });
      } else if (user?.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      } else if (user?.role === 'STAFF') {
        navigate('/staff/check-in', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Đăng nhập không thành công. Vui lòng kiểm tra lại email và mật khẩu.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-logo">
            <Waves size={30} />
          </div>
          <h2>Đăng nhập AquaFlow</h2>
          <p>Truy cập tài khoản để đặt vé và quản lý lịch bơi</p>
        </div>

        {initialMessage && (
          <div className="alert-box alert-info" style={{ marginBottom: '1.25rem' }}>
            <AlertCircle size={18} />
            <span>{initialMessage}</span>
          </div>
        )}

        {error && (
          <div className="alert-box alert-danger" style={{ marginBottom: '1.25rem' }}>
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Email hoặc Tên đăng nhập</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="VD: customer@pool.local hoặc customer"
              value={email}
              onChange={e => setEmail(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Mật khẩu</label>
            <input
              type="password"
              className="form-input"
              required
              placeholder="Nhập mật khẩu"
              value={password}
              onChange={e => setPassword(e.target.value)}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-large"
            style={{ width: '100%', marginTop: '0.5rem' }}
            disabled={loading}
          >
            <LogIn size={18} /> {loading ? 'Đang xác thực...' : 'Đăng nhập'}
          </button>
        </form>

        <div className="demo-accounts-hint">
          <small><b>Tài khoản mẫu dùng thử (nhấn để điền):</b></small>
          <ul style={{ marginTop: '0.5rem' }}>
            <li style={{ cursor: 'pointer', marginBottom: '0.35rem' }} onClick={() => fillSample('customer@pool.local', 'password')}>
              Khách hàng: <code>customer@pool.local</code> / <code>password</code>
            </li>
            <li style={{ cursor: 'pointer', marginBottom: '0.35rem' }} onClick={() => fillSample('staff@pool.local', 'password')}>
              Nhân viên: <code>staff@pool.local</code> / <code>password</code>
            </li>
            <li style={{ cursor: 'pointer' }} onClick={() => fillSample('admin@pool.local', 'password')}>
              Quản trị: <code>admin@pool.local</code> / <code>password</code>
            </li>
          </ul>
        </div>

        <div className="auth-footer">
          Chưa có tài khoản?{' '}
          <Link to={`/register${redirectUrl ? `?redirect=${encodeURIComponent(redirectUrl)}` : ''}`}>
            Đăng ký ngay
          </Link>
        </div>
      </div>
    </div>
  );
}
