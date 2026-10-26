import React, { useEffect, useState } from 'react';
import { User, Mail, Phone, ShieldCheck, CheckCircle2, AlertCircle, Save } from 'lucide-react';
import { profileService } from '../../services/profileService';
import { useAuth } from '../../context/AuthContext';
import { Loading, ErrorState, PageTitle } from '../../components/States';

export default function ProfilePage() {
  const { user } = useAuth();

  const [profile, setProfile] = useState(null);
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    profileService.get()
      .then(res => {
        const data = res.data.data;
        setProfile(data);
        setFullName(data.fullName || '');
        setPhone(data.phone || '');
      })
      .catch(() => setError('Không thể tải thông tin hồ sơ.'))
      .finally(() => setLoading(false));
  }, []);

  const handleUpdate = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);

    try {
      const res = await profileService.update({ fullName, phone });
      setProfile(res.data.data);
      setSuccess('Cập nhật hồ sơ thành công!');
    } catch (err) {
      setError(err.response?.data?.message || 'Cập nhật hồ sơ thất bại.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="section-container"><Loading /></div>;
  if (error && !profile) return <div className="section-container"><ErrorState message={error} /></div>;

  return (
    <div className="section-container">
      <PageTitle eyebrow="TÀI KHOẢN CỦA TÔI" title="Hồ sơ khách hàng">
        Quản lý thông tin liên hệ và số điện thoại nhận thông báo đặt chỗ.
      </PageTitle>

      <div className="profile-layout-container">
        <div className="card profile-card">
          {success && (
            <div className="alert-box alert-success" style={{ marginBottom: '1.5rem' }}>
              <CheckCircle2 size={18} />
              <span>{success}</span>
            </div>
          )}

          {error && (
            <div className="alert-box alert-danger" style={{ marginBottom: '1.5rem' }}>
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          <div className="profile-header-info">
            <div className="profile-avatar">
              <User size={36} />
            </div>
            <div>
              <h3>{fullName || profile?.username}</h3>
              <p style={{ color: 'var(--text-muted)' }}>{profile?.email}</p>
            </div>
          </div>

          <form onSubmit={handleUpdate} className="profile-form">
            <div className="form-group">
              <label>Tên đăng nhập (Cố định)</label>
              <input
                type="text"
                className="form-input"
                disabled
                value={profile?.username || ''}
              />
            </div>

            <div className="form-group">
              <label>Email tài khoản (Cố định)</label>
              <input
                type="email"
                className="form-input"
                disabled
                value={profile?.email || ''}
              />
            </div>

            <div className="form-group">
              <label>Họ và tên *</label>
              <input
                type="text"
                className="form-input"
                required
                placeholder="Nhập họ và tên đầy đủ"
                value={fullName}
                onChange={e => setFullName(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label>Số điện thoại</label>
              <input
                type="tel"
                className="form-input"
                placeholder="VD: 0901234567"
                value={phone}
                onChange={e => setPhone(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label>Trạng thái tài khoản</label>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.25rem' }}>
                <ShieldCheck size={18} color="var(--primary)" />
                <span className="status-tag tag-green">{profile?.status === 'HOAT_DONG' ? 'Đang hoạt động' : profile?.status}</span>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              style={{ marginTop: '1rem', width: '100%' }}
              disabled={saving}
            >
              <Save size={18} /> {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
