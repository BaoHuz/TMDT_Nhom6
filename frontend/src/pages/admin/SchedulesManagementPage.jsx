import React, { useEffect, useState } from 'react';
import { Plus, Calendar, Clock, Users, X, CheckCircle2, AlertCircle, Ban, Lock } from 'lucide-react';
import { adminService } from '../../services/adminService';
import { Loading, ErrorState, EmptyState, PageTitle } from '../../components/States';
import { dateText, statusText } from '../../utils/view';

export default function SchedulesManagementPage() {
  const [schedules, setSchedules] = useState([]);
  const [services, setServices] = useState([]);
  const [areas, setAreas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal create
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({
    maDv: '',
    maKhuVuc: '',
    ngay: new Date().toISOString().slice(0, 10),
    gioBatDau: '06:00',
    gioKetThuc: '07:30',
    sucChua: 30
  });
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Cancel schedule modal
  const [cancelModalSchedule, setCancelModalSchedule] = useState(null);
  const [cancelReason, setCancelReason] = useState('');
  const [cancelling, setCancelling] = useState(false);

  const loadData = () => {
    setLoading(true);
    Promise.all([
      adminService.schedules(),
      adminService.services(),
      adminService.areas()
    ])
      .then(([schRes, svcRes, areaRes]) => {
        setSchedules(schRes.data.data || []);
        const sList = (svcRes.data.data || []).filter(s => s.trangThai === 'DANG_BAN');
        const aList = (areaRes.data.data || []).filter(a => a.trangThai === 'HOAT_DONG');
        setServices(sList);
        setAreas(aList);
        if (sList.length > 0 && !form.maDv) setForm(prev => ({ ...prev, maDv: sList[0].maDv }));
        if (aList.length > 0 && !form.maKhuVuc) setForm(prev => ({ ...prev, maKhuVuc: aList[0].maKhuVuc }));
      })
      .catch(() => setError('Không thể tải danh sách ca bơi.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadData();
  }, []);

  const openCreateModal = () => {
    setForm({
      maDv: services[0]?.maDv || '',
      maKhuVuc: areas[0]?.maKhuVuc || '',
      ngay: new Date().toISOString().slice(0, 10),
      gioBatDau: '06:00',
      gioKetThuc: '07:30',
      sucChua: areas[0]?.sucChua || 30
    });
    setFormError('');
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setFormError('');

    if (form.gioKetThuc <= form.gioBatDau) {
      setFormError('Giờ kết thúc phải sau giờ bắt đầu.');
      return;
    }
    if (form.sucChua <= 0) {
      setFormError('Sức chứa ca phải lớn hơn 0.');
      return;
    }

    setSaving(true);
    try {
      await adminService.createSchedule(form);
      setSuccessMsg('Tạo ca bơi mới thành công!');
      setShowModal(false);
      loadData();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Tạo ca bơi thất bại.');
    } finally {
      setSaving(false);
    }
  };

  const handleCloseSchedule = async (id) => {
    if (!window.confirm('Bạn có chắc chắn muốn đóng ca bơi này không?')) return;
    try {
      await adminService.closeSchedule(id);
      setSuccessMsg('Đã đóng ca bơi!');
      loadData();
    } catch (err) {
      alert(err.response?.data?.message || 'Đóng ca thất bại.');
    }
  };

  const handleConfirmCancelSchedule = async () => {
    if (!cancelModalSchedule) return;
    setCancelling(true);
    try {
      await adminService.cancelSchedule(cancelModalSchedule.maLich, cancelReason);
      setSuccessMsg('Đã hủy ca bơi. Yêu cầu hoàn tiền 100% đã được gửi hoặc đang chờ xử lý.');
      setCancelModalSchedule(null);
      setCancelReason('');
      loadData();
    } catch (err) {
      alert(err.response?.data?.message || 'Hủy ca bơi thất bại.');
    } finally {
      setCancelling(false);
    }
  };

  const svcMap = Object.fromEntries(services.map(s => [s.maDv, s.tenDv]));
  const areaMap = Object.fromEntries(areas.map(a => [a.maKhuVuc, a.tenKhuVuc]));

  return (
    <div className="admin-page-container">
      <div className="admin-page-header-row">
        <PageTitle eyebrow="VẬN HÀNH CA BƠI" title="Quản lý Ca bơi / Lịch dịch vụ">
          Mở ca bơi theo ngày, giám sát số chỗ trống, đóng ca hoặc hủy ca do sự cố (hoàn tiền tự động).
        </PageTitle>
        <button className="btn btn-primary" onClick={openCreateModal}>
          <Plus size={18} /> Mở ca bơi mới
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
      ) : schedules.length === 0 ? (
        <EmptyState message="Chưa có ca bơi nào được thiết lập." />
      ) : (
        <div className="card table-card">
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Mã ca</th>
                  <th>Dịch vụ</th>
                  <th>Khu vực</th>
                  <th>Ngày bơi</th>
                  <th>Khung giờ</th>
                  <th>Chỗ còn / Sức chứa</th>
                  <th>Trạng thái</th>
                  <th style={{ textAlign: 'right' }}>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {schedules.map(sch => (
                  <tr key={sch.maLich}>
                    <td><code>{sch.maLich}</code></td>
                    <td><b>{svcMap[sch.maDv] || sch.maDv}</b></td>
                    <td>{areaMap[sch.maKhuVuc] || sch.maKhuVuc}</td>
                    <td>{dateText(sch.ngay)}</td>
                    <td>{sch.gioBatDau?.slice(0, 5)} - {sch.gioKetThuc?.slice(0, 5)}</td>
                    <td>
                      <b style={{ color: sch.soChoCon > 0 ? 'var(--primary-dark)' : 'var(--danger)' }}>
                        {sch.soChoCon}
                      </b> / {sch.sucChua}
                    </td>
                    <td>
                      <span className={`status-tag ${sch.trangThai === 'MO' ? 'tag-green' : sch.trangThai === 'HET_CHO' ? 'tag-yellow' : 'tag-gray'}`}>
                        {statusText(sch.trangThai)}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                      {sch.trangThai === 'MO' && (
                        <>
                          <button
                            className="btn btn-outline btn-sm"
                            style={{ marginRight: '0.5rem' }}
                            title="Đóng ca (không nhận thêm khách)"
                            onClick={() => handleCloseSchedule(sch.maLich)}
                          >
                            <Lock size={13} /> Đóng ca
                          </button>
                          <button
                            className="btn btn-danger-outline btn-sm"
                            title="Hủy ca do sự cố (hoàn 100% cho đơn đã thanh toán)"
                            onClick={() => {
                              setCancelModalSchedule(sch);
                              setCancelReason('');
                            }}
                          >
                            <Ban size={13} /> Hủy ca
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal Add Schedule */}
      {showModal && (
        <div className="modal-backdrop">
          <div className="modal-box">
            <div className="modal-header">
              <h3>Mở ca bơi mới</h3>
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
                  <label>Dịch vụ bơi *</label>
                  <select
                    className="form-input"
                    required
                    value={form.maDv}
                    onChange={e => setForm({ ...form, maDv: e.target.value })}
                  >
                    {services.map(s => (
                      <option key={s.maDv} value={s.maDv}>{s.tenDv}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Khu vực hồ *</label>
                  <select
                    className="form-input"
                    required
                    value={form.maKhuVuc}
                    onChange={e => {
                      const kv = areas.find(a => a.maKhuVuc === e.target.value);
                      setForm({
                        ...form,
                        maKhuVuc: e.target.value,
                        sucChua: kv ? kv.sucChua : form.sucChua
                      });
                    }}
                  >
                    {areas.map(a => (
                      <option key={a.maKhuVuc} value={a.maKhuVuc}>{a.tenKhuVuc} (Sức chứa: {a.sucChua})</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Ngày diễn ra ca *</label>
                  <input
                    type="date"
                    className="form-input"
                    required
                    value={form.ngay}
                    onChange={e => setForm({ ...form, ngay: e.target.value })}
                  />
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <div className="form-group">
                    <label>Giờ bắt đầu *</label>
                    <input
                      type="time"
                      className="form-input"
                      required
                      value={form.gioBatDau}
                      onChange={e => setForm({ ...form, gioBatDau: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label>Giờ kết thúc *</label>
                    <input
                      type="time"
                      className="form-input"
                      required
                      value={form.gioKetThuc}
                      onChange={e => setForm({ ...form, gioKetThuc: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label>Sức chứa ca (người) *</label>
                  <input
                    type="number"
                    className="form-input"
                    required
                    min={1}
                    value={form.sucChua}
                    onChange={e => setForm({ ...form, sucChua: parseInt(e.target.value) || 1 })}
                  />
                  <small style={{ color: 'var(--text-muted)' }}>Sức chứa không được vượt quá sức chứa khu vực.</small>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Đang tạo...' : 'Mở ca bơi'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Cancel Schedule */}
      {cancelModalSchedule && (
        <div className="modal-backdrop">
          <div className="modal-box">
            <div className="modal-header">
              <h3>Hủy ca bơi do sự cố</h3>
              <button className="btn-close" onClick={() => setCancelModalSchedule(null)}><X size={18} /></button>
            </div>
            <div className="modal-body">
              <div className="alert-box alert-danger">
                <AlertCircle size={20} />
                <div>
                  <b>Lưu ý quan trọng:</b>
                  <p>Hành động này sẽ đóng ca <b>{cancelModalSchedule.maLich}</b>, hủy tất cả đơn đặt vé chưa check-in và gửi yêu cầu hoàn 100% với đơn đã thanh toán. Trạng thái hoàn tiền sẽ được cập nhật theo phản hồi xử lý.</p>
                </div>
              </div>

              <div className="form-group" style={{ marginTop: '1rem' }}>
                <label>Lý do hủy ca:</label>
                <textarea
                  className="form-input"
                  rows={3}
                  placeholder="VD: Sự cố bảo trì hồ bơi đột xuất, thời tiết xấu..."
                  value={cancelReason}
                  onChange={e => setCancelReason(e.target.value)}
                />
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={() => setCancelModalSchedule(null)}>Hủy bỏ</button>
              <button type="button" className="btn btn-danger" disabled={cancelling} onClick={handleConfirmCancelSchedule}>
                {cancelling ? 'Đang xử lý hoàn tiền...' : 'Xác nhận hủy ca & hoàn tiền'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
