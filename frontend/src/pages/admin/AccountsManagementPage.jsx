import React, { useEffect, useState } from 'react';
import { UserPlus, Lock, Unlock, Search, X, CheckCircle2, AlertCircle, Shield } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';

export default function AccountsManagementPage() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');

  // Modal create staff
  const [showModal, setShowModal] = useState(false);
  const [staffForm, setStaffForm] = useState({
    username: '',
    email: '',
    password: '',
    fullName: ''
  });
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const loadAccounts = () => {
    setLoading(true);
    adminService.accounts()
      .then(res => setAccounts(res.data.data || []))
      .catch(() => setError('Không thể tải danh sách tài khoản.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  const handleCreateStaff = async (e) => {
    e.preventDefault();
    setFormError('');

    if (staffForm.password.length < 8) {
      setFormError('Mật khẩu tối thiểu 8 ký tự.');
      return;
    }

    setSaving(true);
    try {
      await adminService.createStaff(staffForm);
      setSuccessMsg(`Tạo tài khoản STAFF "${staffForm.username}" thành công!`);
      setShowModal(false);
      setStaffForm({ username: '', email: '', password: '', fullName: '' });
      loadAccounts();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Tạo tài khoản STAFF thất bại.');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleLock = async (acc) => {
    const isLocked = acc.status === 'KHOA';
    const actionText = isLocked ? 'mở khóa' : 'khóa';
    if (!window.confirm(`Bạn có chắc muốn ${actionText} tài khoản "${acc.username}" không?`)) return;

    try {
      if (isLocked) {
        await adminService.unlockAccount(acc.maTk);
        setSuccessMsg(`Đã mở khóa tài khoản ${acc.username}`);
      } else {
        await adminService.lockAccount(acc.maTk);
        setSuccessMsg(`Đã khóa tài khoản ${acc.username}`);
      }
      loadAccounts();
    } catch (err) {
      alert(err.response?.data?.message || `Thao tác ${actionText} tài khoản thất bại.`);
    }
  };

  const filteredAccounts = accounts.filter(acc => {
    const matchSearch = !searchTerm.trim() ||
      acc.username?.toLowerCase().includes(searchTerm.trim().toLowerCase()) ||
      acc.email?.toLowerCase().includes(searchTerm.trim().toLowerCase());
    const matchRole = roleFilter === 'ALL' || acc.role === roleFilter;
    return matchSearch && matchRole;
  });

  return (
    <div className="admin-page-container">
      <div className="admin-page-header-row">
        <PageTitle eyebrow="BẢO MẬT & PHÂN QUYỀN" title="Quản lý Tài khoản">
          Quản lý danh sách người dùng, cấp tài khoản cho Nhân viên tiếp đón (STAFF) và kiểm soát trạng thái khóa.
        </PageTitle>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          <UserPlus size={18} /> Thêm tài khoản STAFF
        </button>
      </div>

      {successMsg && (
        <div className="alert-box alert-success" style={{ marginBottom: '1.5rem' }}>
          <CheckCircle2 size={18} />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Filter panel */}
      <div className="filter-bar-card card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ flex: 1, minWidth: '240px' }}>
            <div className="search-input-wrapper">
              <Search size={16} className="input-icon" />
              <input
                type="text"
                className="form-input with-icon"
                placeholder="Tìm theo username hoặc email..."
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
              />
            </div>
          </div>

          <div style={{ minWidth: '180px' }}>
            <select
              className="form-input"
              value={roleFilter}
              onChange={e => setRoleFilter(e.target.value)}
            >
              <option value="ALL">Tất cả vai trò</option>
              <option value="CUSTOMER">Khách hàng (CUSTOMER)</option>
              <option value="STAFF">Nhân viên (STAFF)</option>
              <option value="ADMIN">Quản trị (ADMIN)</option>
            </select>
          </div>
        </div>
      </div>

      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} />
      ) : filteredAccounts.length === 0 ? (
        <EmptyState message="Không tìm thấy tài khoản nào." />
      ) : (
        <div className="card table-card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Mã TK</th>
                  <th>Tên đăng nhập</th>
                  <th>Email</th>
                  <th>Vai trò</th>
                  <th>Trạng thái</th>
                  <th>Ngày tạo</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredAccounts.map(acc => (
                  <tr key={acc.maTk}>
                    <td><code>{acc.maTk}</code></td>
                    <td><b>{acc.username}</b></td>
                    <td>{acc.email}</td>
                    <td>
                      <span className={`status-tag ${acc.role === 'ADMIN' ? 'tag-purple' : acc.role === 'STAFF' ? 'tag-blue' : 'tag-gray'}`}>
                        {acc.role}
                      </span>
                    </td>
                    <td>
                      <span className={`status-tag ${acc.status === 'HOAT_DONG' ? 'tag-green' : 'tag-red'}`}>
                        {acc.status === 'HOAT_DONG' ? 'Hoạt động' : 'Đã khóa'}
                      </span>
                    </td>
                    <td>{acc.createdAt ? new Date(acc.createdAt).toLocaleDateString('vi-VN') : '—'}</td>
                    <td style={{ textAlign: 'right' }}>
                      {acc.role !== 'ADMIN' && (
                        <button
                          className={`btn btn-sm ${acc.status === 'KHOA' ? 'btn-outline' : 'btn-danger-outline'}`}
                          onClick={() => handleToggleLock(acc)}
                        >
                          {acc.status === 'KHOA' ? (
                            <><Unlock size={13} /> Mở khóa</>
                          ) : (
                            <><Lock size={13} /> Khóa</>
                          )}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal Create Staff */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-box">
            <div className="modal-header">
              <h3>Tạo tài khoản Nhân viên (STAFF)</h3>
              <button className="btn-close" onClick={() => setShowModal(false)}><X size={18} /></button>
            </div>

            <form onSubmit={handleCreateStaff}>
              <div className="modal-body">
                {formError && (
                  <div className="alert-box alert-danger">
                    <AlertCircle size={18} />
                    <span>{formError}</span>
                  </div>
                )}

                <div className="form-group">
                  <label>Họ và tên nhân viên *</label>
                  <input
                    type="text"
                    className="form-input"
                    required
                    placeholder="VD: Lê Thị Thu"
                    value={staffForm.fullName}
                    onChange={e => setStaffForm({ ...staffForm, fullName: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Tên đăng nhập *</label>
                  <input
                    type="text"
                    className="form-input"
                    required
                    placeholder="VD: staff_thudn"
                    value={staffForm.username}
                    onChange={e => setStaffForm({ ...staffForm, username: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Email *</label>
                  <input
                    type="email"
                    className="form-input"
                    required
                    placeholder="VD: staff_thudn@pool.local"
                    value={staffForm.email}
                    onChange={e => setStaffForm({ ...staffForm, email: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Mật khẩu ban đầu (tối thiểu 8 ký tự) *</label>
                  <input
                    type="password"
                    className="form-input"
                    required
                    minLength={8}
                    placeholder="Nhập mật khẩu an toàn"
                    value={staffForm.password}
                    onChange={e => setStaffForm({ ...staffForm, password: e.target.value })}
                  />
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Đang tạo...' : 'Tạo tài khoản STAFF'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
