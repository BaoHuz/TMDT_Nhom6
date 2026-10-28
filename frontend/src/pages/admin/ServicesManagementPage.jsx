import React, { useEffect, useState } from 'react';
import { Plus, Edit, Tag, Clock, X, CheckCircle2, AlertCircle } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';
import { money, statusText } from '../../utils/view';

export default function ServicesManagementPage() {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editingService, setEditingService] = useState(null);
  const [form, setForm] = useState({
    tenDv: '',
    loaiDv: 'VE_BOI',
    donGia: 50000,
    thoiLuongPhut: 90,
    trangThai: 'DANG_BAN'
  });
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const loadServices = () => {
    setLoading(true);
    adminService.services()
      .then(res => setServices(res.data.data || []))
      .catch(() => setError('Không thể tải danh sách dịch vụ.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadServices();
  }, []);

  const openCreateModal = () => {
    setEditingService(null);
    setForm({
      tenDv: '',
      loaiDv: 'VE_BOI',
      donGia: 50000,
      thoiLuongPhut: 90,
      trangThai: 'DANG_BAN'
    });
    setFormError('');
    setShowModal(true);
  };

  const openEditModal = (svc) => {
    setEditingService(svc);
    setForm({
      tenDv: svc.tenDv,
      loaiDv: svc.loaiDv,
      donGia: svc.donGia,
      thoiLuongPhut: svc.thoiLuongPhut,
      trangThai: svc.trangThai || 'DANG_BAN'
    });
    setFormError('');
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setFormError('');

    if (form.donGia < 0) {
      setFormError('Đơn giá không được âm.');
      return;
    }
    if (form.thoiLuongPhut <= 0) {
      setFormError('Thời lượng phải lớn hơn 0 phút.');
      return;
    }

    setSaving(true);
    try {
      if (editingService) {
        await adminService.updateService(editingService.maDv, form);
        setSuccessMsg('Cập nhật dịch vụ thành công!');
      } else {
        await adminService.createService(form);
        setSuccessMsg('Tạo dịch vụ mới thành công!');
      }
      setShowModal(false);
      loadServices();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Lưu dịch vụ thất bại.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="admin-page-container">
      <div className="admin-page-header-row">
        <PageTitle eyebrow="GÓI DỊCH VỤ & VÉ" title="Quản lý Dịch vụ Bơi">
          Cấu hình giá vé, thời lượng ca bơi và trạng thái mở bán dịch vụ.
        </PageTitle>
        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} /> Thêm dịch vụ mới
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
      ) : services.length === 0 ? (
        <EmptyState message="Chưa có dịch vụ nào." />
      ) : (
        <div className="card table-card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Mã DV</th>
                  <th>Tên dịch vụ</th>
                  <th>Loại dịch vụ</th>
                  <th>Đơn giá</th>
                  <th>Thời lượng</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {services.map(svc => (
                  <tr key={svc.maDv}>
                    <td><code>{svc.maDv}</code></td>
                    <td><b>{svc.tenDv}</b></td>
                    <td><span className="badge-pill">{svc.loaiDv}</span></td>
                    <td><b>{money(svc.donGia)}</b></td>
                    <td><Clock size={14} style={{ display: 'inline', verticalAlign: 'middle' }} /> {svc.thoiLuongPhut} phút</td>
                    <td>
                      <span className={`status-tag ${svc.trangThai === 'DANG_BAN' ? 'tag-green' : svc.trangThai === 'TAM_DUNG' ? 'tag-yellow' : 'tag-gray'}`}>
                        {statusText(svc.trangThai)}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button className="btn btn-outline btn-sm" onClick={() => openEditModal(svc)}>
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
              <h3>{editingService ? 'Chỉnh sửa Dịch vụ' : 'Thêm Dịch vụ mới'}</h3>
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
                  <label>Tên dịch vụ *</label>
                  <input
                    type="text"
                    className="form-input"
                    required
                    placeholder="VD: Vé bơi người lớn, Vé tập luyện..."
                    value={form.tenDv}
                    onChange={e => setForm({ ...form, tenDv: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Loại dịch vụ *</label>
                  <select
                    className="form-input"
                    value={form.loaiDv}
                    onChange={e => setForm({ ...form, loaiDv: e.target.value })}
                  >
                    <option value="VE_BOI">Vé bơi tự do (VE_BOI)</option>
                    <option value="LUYEN_TAP">Vé luyện tập chuyên sâu (LUYEN_TAP)</option>
                    <option value="KHOA_HOC">Khóa học bơi (KHOA_HOC)</option>
                  </select>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label>Đơn giá (VNĐ) *</label>
                    <input
                      type="number"
                      className="form-input"
                      required
                      min={0}
                      step={1000}
                      value={form.donGia}
                      onChange={e => setForm({ ...form, donGia: parseFloat(e.target.value) || 0 })}
                    />
                  </div>
                  <div className="form-group">
                    <label>Thời lượng (phút) *</label>
                    <input
                      type="number"
                      className="form-input"
                      required
                      min={1}
                      value={form.thoiLuongPhut}
                      onChange={e => setForm({ ...form, thoiLuongPhut: parseInt(e.target.value) || 1 })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Trạng thái bán</label>
                  <select
                    className="form-input"
                    value={form.trangThai}
                    onChange={e => setForm({ ...form, trangThai: e.target.value })}
                  >
                    <option value="DANG_BAN">Đang bán (DANG_BAN)</option>
                    <option value="TAM_DUNG">Tạm dừng (TAM_DUNG)</option>
                    <option value="NGUNG_BAN">Ngừng bán (NGUNG_BAN)</option>
                  </select>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Đang lưu...' : editingService ? 'Cập nhật' : 'Thêm dịch vụ'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
