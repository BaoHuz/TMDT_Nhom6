import React, { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { UserPlus, Waves, AlertCircle } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function RegisterPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { register } = useAuth();

  const redirectUrl = searchParams.get('redirect') || '';

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (password.length < 8) {
      setError('Mật khẩu phải có ít nhất 8 ký tự.');
      return;
    }

    setLoading(true);
    try {
      await register({
        username,
        email,
        password,
        fullName,
        phone
      });

      if (redirectUrl) {
        navigate(redirectUrl, { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Đăng ký không thành công. Vui lòng kiểm tra lại thông tin.';
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
          <h2>Tạo tài khoản AquaFlow</h2>
          <p>Đăng ký để đặt vé nhanh chóng và quản lý lịch bơi</p>
        </div>

        {error && (
          <div className="alert-box alert-danger" style={{ marginBottom: '1.25rem' }}>
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Họ và tên *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="VD: Nguyễn Văn A"
              value={fullName}
              onChange={e => setFullName(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Tên đăng nhập *</label>
            <input
              type="text"
              className="form-input"
              required
              placeholder="VD: nguyenvana (ít nhất 3 ký tự)"
              value={username}
              onChange={e => setUsername(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Địa chỉ Email *</label>
            <input
              type="email"
              className="form-input"
              required
              placeholder="VD: nguyenvana@email.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Số điện thoại liên hệ</label>
            <input
              type="tel"
              className="form-input"
              placeholder="VD: 0901234567"
              value={phone}
              onChange={e => setPhone(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Mật khẩu *</label>
            <input
              type="password"
              className="form-input"
              required
              placeholder="Ít nhất 8 ký tự"
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
            <UserPlus size={18} /> {loading ? 'Đang tạo tài khoản...' : 'Đăng ký ngay'}
          </button>
        </form>

        <div className="auth-footer">
          Đã có tài khoản?{' '}
          <Link to={`/login${redirectUrl ? `?redirect=${encodeURIComponent(redirectUrl)}` : ''}`}>
            Đăng nhập
          </Link>
        </div>
      </div>
    </div>
  );
}
