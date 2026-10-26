import React, { useEffect, useState } from 'react';
import { Plus, Edit, Layers, Users, X, CheckCircle2, AlertCircle } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';

export default function AreasManagementPage() {
  const [areas, setAreas] = useState([]);
  const [pools, setPools] = useState([]);
  const [selectedPoolFilter, setSelectedPoolFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal
  const [showModal, setShowModal] = useState(false);
  const [editingArea, setEditingArea] = useState(null);
  const [form, setForm] = useState({
    maHoBoi: '',
    tenKhuVuc: '',
    sucChua: 30,
    trangThai: 'HOAT_DONG'
  });
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const loadData = () => {
    setLoading(true);
    Promise.all([adminService.areas(), adminService.pools()])
      .then(([areasRes, poolsRes]) => {
        setAreas(areasRes.data.data || []);
        const pList = poolsRes.data.data || [];
        setPools(pList);
        if (pList.length > 0 && !form.maHoBoi) {
          setForm(prev => ({ ...prev, maHoBoi: pList[0].maHoBoi }));
        }
      })
      .catch(() => setError('Không thể tải danh sách khu vực.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadData();
  }, []);

  const openCreateModal = () => {
    setEditingArea(null);
    setForm({
      maHoBoi: pools[0]?.maHoBoi || '',
      tenKhuVuc: '',
      sucChua: 30,
      trangThai: 'HOAT_DONG'
    });
    setFormError('');
    setShowModal(true);
  };

  const openEditModal = (area) => {
    setEditingArea(area);
    setForm({
      maHoBoi: area.maHoBoi,
      tenKhuVuc: area.tenKhuVuc,
      sucChua: area.sucChua,
      trangThai: area.trangThai || 'HOAT_DONG'
    });
    setFormError('');
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setFormError('');

    if (form.sucChua <= 0) {
      setFormError('Sức chứa phải lớn hơn 0.');
      return;
    }

    setSaving(true);
    try {
      if (editingArea) {
        await adminService.updateArea(editingArea.maKhuVuc, form);
        setSuccessMsg('Cập nhật khu vực thành công!');
      } else {
        await adminService.createArea(form);
        setSuccessMsg('Tạo khu vực mới thành công!');
      }
      setShowModal(false);
      loadData();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Lưu thông tin khu vực thất bại.');
    } finally {
      setSaving(false);
    }
  };

  const poolMap = Object.fromEntries(pools.map(p => [p.maHoBoi, p.tenHoBoi]));

  const filteredAreas = areas.filter(a => {
    if (!selectedPoolFilter) return true;
    return a.maHoBoi === selectedPoolFilter;
  });

  return (
    <div className="admin-page-container">
      <div className="admin-page-header-row">
        <PageTitle eyebrow="PHÂN KHU HỒ BƠI" title="Quản lý Khu vực">
          Thiết lập các phân khu chức năng (hồ người lớn, hồ trẻ em, làn bơi) và sức chứa.
        </PageTitle>
        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} /> Thêm khu vực mới
        </button>
      </div>

      {successMsg && (
        <div className="alert-box alert-success" style={{ marginBottom: '1.5rem' }}>
          <CheckCircle2 size={18} />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Filter by Pool */}
      <div className="filter-bar" style={{ marginBottom: '1rem' }}>
        <label>Lọc theo Hồ bơi:</label>
        <select
          className="form-input"
          style={{ maxWidth: '280px' }}
          value={selectedPoolFilter}
          onChange={e => setSelectedPoolFilter(e.target.value)}
        >
          <option value="">Tất cả các hồ</option>
          {pools.map(p => (
            <option key={p.maHoBoi} value={p.maHoBoi}>{p.tenHoBoi}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <Loading />
      ) : error ? (
        <ErrorState message={error} />
      ) : filteredAreas.length === 0 ? (
        <EmptyState message="Chưa có khu vực nào." />
      ) : (
        <div className="card table-card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Mã KV</th>
                  <th>Tên khu vực</th>
                  <th>Thuộc Hồ bơi</th>
                  <th>Sức chứa</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {filteredAreas.map(area => (
                  <tr key={area.maKhuVuc}>
                    <td><code>{area.maKhuVuc}</code></td>
                    <td><b>{area.tenKhuVuc}</b></td>
                    <td>{poolMap[area.maHoBoi] || area.maHoBoi}</td>
                    <td><Users size={14} style={{ display: 'inline', verticalAlign: 'middle' }} /> {area.sucChua} người</td>
                    <td>
                      <span className={`status-tag ${area.trangThai === 'HOAT_DONG' ? 'tag-green' : 'tag-gray'}`}>
                        {area.trangThai === 'HOAT_DONG' ? 'Hoạt động' : area.trangThai === 'TAM_DONG' ? 'Tạm đóng' : 'Ngừng hoạt động'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button className="btn btn-outline btn-sm" onClick={() => openEditModal(area)}>
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
              <h3>{editingArea ? 'Chỉnh sửa Khu vực' : 'Thêm Khu vực mới'}</h3>
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
                  <label>Hồ bơi cơ sở *</label>
                  <select
                    className="form-input"
                    required
                    value={form.maHoBoi}
                    onChange={e => setForm({ ...form, maHoBoi: e.target.value })}
                  >
                    {pools.map(p => (
                      <option key={p.maHoBoi} value={p.maHoBoi}>{p.tenHoBoi}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Tên khu vực *</label>
                  <input
                    type="text"
                    className="form-input"
                    required
                    placeholder="VD: Hồ người lớn, Làn bơi 50m..."
                    value={form.tenKhuVuc}
                    onChange={e => setForm({ ...form, tenKhuVuc: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Sức chứa tối đa (người) *</label>
                  <input
                    type="number"
                    className="form-input"
                    required
                    min={1}
                    value={form.sucChua}
                    onChange={e => setForm({ ...form, sucChua: parseInt(e.target.value) || 1 })}
                  />
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
                  {saving ? 'Đang lưu...' : editingArea ? 'Cập nhật' : 'Thêm khu vực'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
