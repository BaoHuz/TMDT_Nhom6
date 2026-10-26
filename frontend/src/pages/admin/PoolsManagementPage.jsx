import React, { useEffect, useState } from 'react';
import { Plus, Edit, Waves, Clock, MapPin, X, CheckCircle2, AlertCircle } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';

export default function PoolsManagementPage() {
  const [pools, setPools] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [editingPool, setEditingPool] = useState(null); // null if creating
  const [form, setForm] = useState({
    tenHoBoi: '',
    diaChi: '',
    gioMoCua: '06:00',
    gioDongCua: '21:00',
    trangThai: 'HOAT_DONG'
  });
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const loadPools = () => {
    setLoading(true);
    adminService.pools()
      .then(res => setPools(res.data.data || []))
      .catch(() => setError('Không thể tải danh sách hồ bơi.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadPools();
  }, []);

  const openCreateModal = () => {
    setEditingPool(null);
    setForm({
      tenHoBoi: '',
      diaChi: '',
      gioMoCua: '06:00',
      gioDongCua: '21:00',
      trangThai: 'HOAT_DONG'
    });
    setFormError('');
    setShowModal(true);
  };

  const openEditModal = (pool) => {
    setEditingPool(pool);
    setForm({
      tenHoBoi: pool.tenHoBoi,
      diaChi: pool.diaChi,
      gioMoCua: pool.gioMoCua?.slice(0, 5) || '06:00',
      gioDongCua: pool.gioDongCua?.slice(0, 5) || '21:00',
      trangThai: pool.trangThai || 'HOAT_DONG'
    });
    setFormError('');
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setFormError('');

    if (form.gioDongCua <= form.gioMoCua) {
      setFormError('Giờ đóng cửa phải sau giờ mở cửa.');
      return;
    }

    setSaving(true);
    try {
      if (editingPool) {
        await adminService.updatePool(editingPool.maHoBoi, form);
        setSuccessMsg('Cập nhật hồ bơi thành công!');
      } else {
        await adminService.createPool(form);
        setSuccessMsg('Thêm hồ bơi mới thành công!');
      }
      setShowModal(false);
      loadPools();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Lưu thông tin hồ bơi thất bại.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="admin-page-container">
      <div className="admin-page-header-row">
        <PageTitle eyebrow="VẬN HÀNH CƠ SỞ" title="Quản lý Hồ bơi">
          Danh mục tất cả các cụm hồ bơi công cộng trong hệ thống.
        </PageTitle>
        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} /> Thêm hồ bơi mới
        </button>
      </div>

      {successMsg && (
        <div className="alert-box alert-success" style={{ marginBottom: '1.5rem' }}>
          <CheckCircle2 size={18} />
          <span>{successMsg}</span>
        </div>
      )}

      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} />
      ) : pools.length === 0 ? (
        <EmptyState message="Chưa có hồ bơi nào trong hệ thống." />
      ) : (
        <div className="card table-card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Mã hồ</th>
                  <th>Tên hồ bơi</th>
                  <th>Địa chỉ</th>
                  <th>Giờ hoạt động</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {pools.map(pool => (
                  <tr key={pool.maHoBoi}>
                    <td><code>{pool.maHoBoi}</code></td>
                    <td><b>{pool.tenHoBoi}</b></td>
                    <td>{pool.diaChi}</td>
                    <td>{pool.gioMoCua?.slice(0, 5)} - {pool.gioDongCua?.slice(0, 5)}</td>
                    <td>
                      <span className={`status-tag ${pool.trangThai === 'HOAT_DONG' ? 'tag-green' : 'tag-gray'}`}>
                        {pool.trangThai === 'HOAT_DONG' ? 'Hoạt động' : pool.trangThai === 'TAM_DONG' ? 'Tạm đóng' : 'Ngừng hoạt động'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button className="btn btn-outline btn-sm" onClick={() => openEditModal(pool)}>
                        <Edit size={14} /> Chỉnh sửa
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal Add/Edit */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-box">
            <div className="modal-header">
              <h3>{editingPool ? 'Chỉnh sửa Hồ bơi' : 'Thêm Hồ bơi mới'}</h3>
              <button className="btn-close" onClick={() => setShowModal(false)}><X size={18} /></button>
            </div>

            <form onSubmit={handleSave}>
              <div className="modal-body">
                {formError && (
                  <div className="alert-box alert-danger">
                    <AlertCircle size={18} />
                    <span>{formError}</span>
                  </div>
                )}

                <div className="form-group">
                  <label>Tên hồ bơi *</label>
                  <input
                    type="text"
                    className="form-input"
                    required
                    placeholder="VD: Hồ Bơi Thảo Điền"
                    value={form.tenHoBoi}
                    onChange={e => setForm({ ...form, tenHoBoi: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Địa chỉ *</label>
                  <input
                    type="text"
                    className="form-input"
                    required
                    placeholder="VD: Thảo Điền, TP. Hồ Chí Minh"
                    value={form.diaChi}
                    onChange={e => setForm({ ...form, diaChi: e.target.value })}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label>Giờ mở cửa *</label>
                    <input
                      type="time"
                      className="form-input"
                      required
                      value={form.gioMoCua}
                      onChange={e => setForm({ ...form, gioMoCua: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label>Giờ đóng cửa *</label>
                    <input
                      type="time"
                      className="form-input"
                      required
                      value={form.gioDongCua}
                      onChange={e => setForm({ ...form, gioDongCua: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Trạng thái</label>
                  <select
                    className="form-input"
                    value={form.trangThai}
                    onChange={e => setForm({ ...form, trangThai: e.target.value })}
                  >
                    <option value="HOAT_DONG">Hoạt động (HOAT_DONG)</option>
                    <option value="TAM_DONG">Tạm đóng (TAM_DONG)</option>
                    <option value="NGUNG_HOAT_DONG">Ngừng hoạt động (NGUNG_HOAT_DONG)</option>
                  </select>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Đang lưu...' : editingPool ? 'Cập nhật' : 'Thêm hồ bơi'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
