import React, {useState} from 'react';
import {Waves} from 'lucide-react';
import {useAuth} from './context/AuthContext';
import './auth.css';

export default function AuthGate({children}) {
  const {user, login, register} = useAuth();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({email: '', password: '', username: '', fullName: '', phone: ''});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) return children;

  const change = event => setForm({...form, [event.target.name]: event.target.value});
  const submit = async event => {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (mode === 'login') await login({email: form.email, password: form.password});
      else await register(form);
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể kết nối máy chủ');
    } finally {
      setLoading(false);
    }
  };

  return <main className="auth-page">
    <section className="auth-card">
      <div className="auth-brand"><span><Waves/></span><b>AquaFlow</b></div>
      <h1>{mode === 'login' ? 'Đăng nhập' : 'Tạo tài khoản'}</h1>
      <p>{mode === 'login'
        ? 'Đăng nhập để đặt lịch và quản lý vé bơi.'
        : 'Tài khoản đăng ký mới có quyền khách hàng.'}</p>
      <form onSubmit={submit}>
        {mode === 'register' && <>
          <label>Họ và tên<input name="fullName" value={form.fullName} onChange={change} required/></label>
          <label>Tên đăng nhập<input name="username" value={form.username} onChange={change} required/></label>
        </>}
        <label>Email<input name="email" type="email" value={form.email} onChange={change} required/></label>
        <label>Mật khẩu<input name="password" type="password" minLength="8" value={form.password} onChange={change} required/></label>
        {mode === 'register' && <label>Số điện thoại<input name="phone" value={form.phone} onChange={change}/></label>}
        {error && <div className="auth-error">{error}</div>}
        <button className="primary wide" disabled={loading}>
          {loading ? 'Đang xử lý...' : mode === 'login' ? 'Đăng nhập' : 'Đăng ký'}
        </button>
      </form>
      <button className="auth-switch" onClick={() => {
        setMode(mode === 'login' ? 'register' : 'login');
        setError('');
      }}>
        {mode === 'login' ? 'Chưa có tài khoản? Đăng ký' : 'Đã có tài khoản? Đăng nhập'}
      </button>
    </section>
  </main>;
}
