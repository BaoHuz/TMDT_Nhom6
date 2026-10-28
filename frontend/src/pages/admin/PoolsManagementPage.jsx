import React, { useEffect, useState } from 'react';
import { Plus, Edit, X, CheckCircle2, AlertCircle, Images, Upload, Trash2, Star } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';
import { assetUrl } from '../../utils/view';

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
  const [imagePool, setImagePool] = useState(null);
  const [poolImages, setPoolImages] = useState([]);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [imageLoading, setImageLoading] = useState(false);
  const [imageError, setImageError] = useState('');

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

  const loadImages = async (poolId) => {
    setImageLoading(true);
    setImageError('');
    try {
      const response = await adminService.poolImages(poolId);
      setPoolImages(response.data.data || []);
    } catch (err) {
      setImageError(err.response?.data?.message || 'Không thể tải danh sách ảnh.');
    } finally {
      setImageLoading(false);
    }
  };

  const openImageManager = (pool) => {
    setImagePool(pool);
    setSelectedFiles([]);
    loadImages(pool.maHoBoi);
  };

  const handleUploadImages = async (event) => {
    event.preventDefault();
    if (!selectedFiles.length) {
      setImageError('Vui lòng chọn ít nhất một ảnh.');
      return;
    }
    setImageLoading(true);
    setImageError('');
    try {
      await adminService.uploadPoolImages(imagePool.maHoBoi, selectedFiles);
      setSelectedFiles([]);
      await loadImages(imagePool.maHoBoi);
      setSuccessMsg('Tải ảnh hồ bơi thành công!');
    } catch (err) {
      setImageError(err.response?.data?.message || 'Tải ảnh thất bại.');
      setImageLoading(false);
    }
  };

  const handleDeleteImage = async (image) => {
    if (!window.confirm('Xóa ảnh này khỏi hồ bơi?')) return;
    setImageLoading(true);
    setImageError('');
    try {
      await adminService.deletePoolImage(image.maAnh);
      await loadImages(imagePool.maHoBoi);
    } catch (err) {
      setImageError(err.response?.data?.message || 'Xóa ảnh thất bại.');
      setImageLoading(false);
    }
  };

  const handleSetRepresentative = async (image) => {
    setImageLoading(true);
    setImageError('');
    try {
      await adminService.setPoolRepresentative(image.maAnh);
      await loadImages(imagePool.maHoBoi);
    } catch (err) {
      setImageError(err.response?.data?.message || 'Không thể đặt ảnh đại diện.');
      setImageLoading(false);
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
                      <button className="btn btn-outline btn-sm" onClick={() => openImageManager(pool)} style={{ marginRight: '.5rem' }}>
                        <Images size={14} /> Hình ảnh
                      </button>
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

      {imagePool && (
        <div className="modal-backdrop">
          <div className="modal-box pool-images-modal">
            <div className="modal-header">
              <div><h3>Hình ảnh hồ bơi</h3><small>{imagePool.tenHoBoi}</small></div>
              <button className="btn-close" onClick={() => setImagePool(null)}><X size={18} /></button>
            </div>
            <div className="modal-body">
              {imageError && <div className="alert-box alert-danger"><AlertCircle size={18} /><span>{imageError}</span></div>}
              <form className="pool-image-upload" onSubmit={handleUploadImages}>
                <label className="pool-image-file-picker">
                  <Upload size={22} />
                  <span>{selectedFiles.length ? `Đã chọn ${selectedFiles.length} ảnh` : 'Chọn ảnh JPG, PNG hoặc WEBP'}</span>
                  <input type="file" accept="image/jpeg,image/png,image/webp" multiple onChange={event => setSelectedFiles(Array.from(event.target.files).slice(0, 10))} />
                </label>
                <small>Tối đa 10 ảnh/lần, mỗi ảnh không quá 5 MB.</small>
                <button className="btn btn-primary" type="submit" disabled={imageLoading || !selectedFiles.length}>
                  <Upload size={16} /> Tải ảnh lên
                </button>
              </form>

              {imageLoading && poolImages.length === 0 ? <Loading /> : poolImages.length === 0 ? (
                <EmptyState message="Hồ bơi chưa có ảnh." />
              ) : (
                <div className="admin-pool-image-grid">
                  {poolImages.map(image => (
                    <div className={`admin-pool-image ${image.laAnhDaiDien ? 'representative' : ''}`} key={image.maAnh}>
                      <img src={assetUrl(image.duongDan)} alt="" />
                      {image.laAnhDaiDien && <span className="representative-badge"><Star size={13} fill="currentColor" /> Đại diện</span>}
                      <div className="admin-pool-image-actions">
                        {!image.laAnhDaiDien && (
                          <button className="btn btn-outline btn-sm" type="button" disabled={imageLoading} onClick={() => handleSetRepresentative(image)}>
                            <Star size={14} /> Chọn đại diện
                          </button>
                        )}
                        <button className="btn btn-danger-outline btn-sm" type="button" disabled={imageLoading} onClick={() => handleDeleteImage(image)}>
                          <Trash2 size={14} /> Xóa
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
