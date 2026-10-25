import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { CalendarDays, Ticket, ScanLine, BarChart3, Waves, Bell, Search, Plus, ChevronDown, Clock3, MapPin, Users, ArrowUpRight, MoreHorizontal, X, Check, QrCode, CreditCard } from 'lucide-react';
import { AuthProvider } from './context/AuthContext';
import { useAuth } from './context/AuthContext';
import { adminService, bookingService, checkInService, paymentService, scheduleService } from './services/authService';
import './styles.css';
import './auth.css';

const money = value => new Intl.NumberFormat('vi-VN').format(value) + ' đ';
const todayInput = () => {
  const date = new Date();
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};
const dateAfter = days => {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};
const formatDate = value => value ? new Intl.DateTimeFormat('vi-VN').format(new Date(`${value}T00:00:00`)) : '';
const formatDateTime = value => value ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '';
const errorMessage = error => error.response?.data?.message || 'Không thể hoàn tất yêu cầu. Vui lòng thử lại.';
const statusText = {
  CHO_THANH_TOAN: 'Chờ thanh toán',
  DA_XAC_NHAN: 'Đã thanh toán',
  DA_CHECK_IN: 'Đã sử dụng',
  DA_HUY: 'Đã hủy',
  HET_HAN: 'Hết hạn'
};
const statusClass = status => status === 'DA_XAC_NHAN' ? 'confirmed' : status === 'DA_CHECK_IN' ? 'checked' : status === 'CHO_THANH_TOAN' ? 'pending' : 'done';
const scheduleCard = item => ({
  ...item,
  id: item.maLich,
  date: formatDate(item.ngay),
  rawDate: item.ngay,
  time: `${String(item.gioBatDau).slice(0, 5)} - ${String(item.gioKetThuc).slice(0, 5)}`,
  service: item.dichVu,
  pool: item.tenHo || item.khuVuc,
  area: item.khuVuc,
  price: Number(item.donGia),
  left: item.soChoCon,
  total: item.sucChua,
  color: ['mint', 'purple', 'orange', 'blue'][String(item.maLich).charCodeAt(String(item.maLich).length - 1) % 4]
});

function App() {
  const { user, login, register, logout } = useAuth();
  const [page, setPage] = useState('schedule');
  const [authMode, setAuthMode] = useState(null);
  const [pendingBooking, setPendingBooking] = useState(false);
  const [modal, setModal] = useState(null);
  const [selected, setSelected] = useState(null);
  const [people, setPeople] = useState(1);
  const [toast, setToast] = useState('');
  const notify = message => { setToast(message); setTimeout(() => setToast(''), 2600); };
  const customerNav = [['schedule', 'Lịch bơi', CalendarDays], ['orders', 'Đơn của tôi', Ticket]];
  const staffNav = [['checkin', 'Check-in tại quầy', ScanLine], ['manage', 'Quản lý ca bơi', CalendarDays], ['reports', 'Báo cáo tổng quan', BarChart3]];
  const role = user?.role;
  const nav = role === 'STAFF' || role === 'ADMIN'
    ? staffNav
    : user
      ? customerNav
      : [['schedule', 'Lịch bơi', CalendarDays]];
  const changePage = nextPage => {
    if (!user && nextPage === 'orders') {
      setAuthMode('login');
      return;
    }
    setPage(nextPage);
  };
  const book = session => {
    if (!user) {
      setSelected(session);
      setPendingBooking(true);
      setAuthMode('login');
      return;
    }
    setSelected(session);
    setPeople(1);
    setModal('book');
  };
  if (authMode) {
    return <AuthPage
      mode={authMode}
      setMode={setAuthMode}
      close={() => { setAuthMode(null); setPendingBooking(false); }}
      onSubmit={authMode === 'login' ? login : register}
      onSuccess={value => {
        setAuthMode(null);
        setPage(value.role === 'ADMIN' ? 'reports' : value.role === 'STAFF' ? 'manage' : 'schedule');
        if (pendingBooking) setModal('book');
        setPendingBooking(false);
      }}
    />;
  }
  return <div className="app-shell">
    <aside className="sidebar"><div className="brand"><div className="brand-mark"><Waves size={21} /></div><div><b>Aqua<span>Flow</span></b><small>POOL SERVICES</small></div></div>
      <div className="side-label">MENU CHÍNH</div><nav>{nav.map(([id, label, Icon]) => <button key={id} className={page === id ? 'active' : ''} onClick={() => changePage(id)}><Icon size={18} />{label}</button>)}</nav>
      <div className="help-card"><div className="help-icon"><Waves size={17} /></div><b>Cần hỗ trợ?</b><p>Đội ngũ AquaFlow luôn sẵn sàng.</p><button onClick={() => notify('Đã gửi yêu cầu hỗ trợ')}>Liên hệ hỗ trợ <ArrowUpRight size={14} /></button></div>
      <div className="profile"><div className="avatar">{user ? user.hoTen?.slice(0, 2).toUpperCase() : 'KH'}</div><div><b>{user?.hoTen || 'Khách'}</b><small>{user ? user.role : 'Chưa đăng nhập'}</small></div></div>
    </aside>
    <main className="main"><header><div className="mobile-logo"><Waves size={19} /> AquaFlow</div><div className="breadcrumb">AquaFlow <span>/</span> {nav.find(item => item[0] === page)?.[1]}</div><div className="header-actions"><div className="search"><Search size={17} /><input placeholder="Tìm kiếm..." /></div><button className="icon-button"><Bell size={18} /></button>{user ? <div className="auth-header"><span className="online" />{user.hoTen}<button className="auth-link" onClick={() => { logout(); setPage('schedule'); }}>Đăng xuất</button></div> : <div className="auth-header"><button className="auth-link" onClick={() => setAuthMode('login')}>Đăng nhập</button><button className="primary auth-register" onClick={() => setAuthMode('register')}>Đăng ký</button></div>}</div></header>
      {page === 'schedule' && <Schedule book={book} />}
      {page === 'orders' && <Orders show={setModal} />}
      {page === 'checkin' && <CheckIn notify={notify} />}
      {page === 'manage' && <Manage />}
      {page === 'reports' && <Reports />}
    </main>
    {modal === 'book' && selected && <Booking session={selected} people={people} setPeople={setPeople} close={() => setModal(null)} done={order => setModal({ kind: 'success', order })} />}
    {modal?.kind === 'success' && <Modal close={() => { setModal(null); setPage('orders'); }}><div className="success-icon"><Check size={28} /></div><h2>Thanh toán thành công!</h2><p>Vé của bạn dùng đến {formatDateTime(modal.order.validUntil)}.</p><div className="qr-box"><QrCode size={82} /><b>{modal.order.maVeQr}</b></div><button className="primary wide" onClick={() => { setModal(null); setPage('orders'); }}>Xem đơn hàng</button></Modal>}
    {modal?.maDat && <Modal close={() => setModal(null)}><div className="eyebrow">MÃ VÉ</div><h2>{modal.maDat}</h2><p>{modal.dichVu} - {formatDate(modal.ngay)} · {String(modal.gioBatDau).slice(0, 5)}</p><div className="qr-box"><QrCode size={110} /><b>{modal.maVeQr || statusText[modal.trangThai]}</b></div><p>Vé có hiệu lực đến {formatDateTime(modal.validUntil)}.</p><button className="primary wide" onClick={() => setModal(null)}>Đóng</button></Modal>}
    {toast && <div className="toast"><Check size={17} />{toast}</div>}
  </div>;
}

function AuthPage({ mode, setMode, close, onSubmit, onSuccess }) {
  const [values, setValues] = useState({ tenDangNhap: '', matKhau: '', hoTen: '', email: '', dienThoai: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const isRegister = mode === 'register';
  const update = event => setValues(current => ({ ...current, [event.target.name]: event.target.value }));
  const submit = async event => {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const result = await onSubmit(isRegister
        ? values
        : { tenDangNhap: values.tenDangNhap, matKhau: values.matKhau });
      onSuccess(result);
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Không thể xác thực. Vui lòng kiểm tra thông tin và thử lại.');
    } finally {
      setSubmitting(false);
    }
  };
  return <main className="auth-page">
    <section className="auth-card">
      <div className="auth-brand"><div className="brand-mark"><Waves size={21} /></div><b>Aqua<span>Flow</span></b></div>
      <div className="eyebrow">{isRegister ? 'TẠO TÀI KHOẢN' : 'CHÀO MỪNG TRỞ LẠI'}</div>
      <h1>{isRegister ? 'Đăng ký tài khoản' : 'Đăng nhập'}</h1>
      <p className="auth-description">{isRegister ? 'Tạo tài khoản để đặt lịch bơi và quản lý vé của bạn.' : 'Đăng nhập để đặt vé và quản lý lịch bơi.'}</p>
      <form onSubmit={submit}>
        {isRegister && <label>Họ và tên<input name="hoTen" autoComplete="name" value={values.hoTen} onChange={update} required /></label>}
        <label>Tên đăng nhập<input name="tenDangNhap" autoComplete="username" value={values.tenDangNhap} onChange={update} required /></label>
        <label>Mật khẩu<input name="matKhau" type="password" autoComplete={isRegister ? 'new-password' : 'current-password'} minLength={isRegister ? 6 : undefined} value={values.matKhau} onChange={update} required /></label>
        {isRegister && <>
          <label>Email <span className="optional">(không bắt buộc)</span><input name="email" type="email" autoComplete="email" value={values.email} onChange={update} /></label>
          <label>Số điện thoại <span className="optional">(không bắt buộc)</span><input name="dienThoai" type="tel" autoComplete="tel" value={values.dienThoai} onChange={update} /></label>
        </>}
        {error && <p className="auth-error" role="alert">{error}</p>}
        <button className="primary wide" type="submit" disabled={submitting}>{submitting ? 'Đang xử lý...' : isRegister ? 'Tạo tài khoản' : 'Đăng nhập'}</button>
      </form>
      <p className="auth-switch">{isRegister ? 'Đã có tài khoản?' : 'Chưa có tài khoản?'} <button onClick={() => { setError(''); setMode(isRegister ? 'login' : 'register'); }}>{isRegister ? 'Đăng nhập' : 'Đăng ký ngay'}</button></p>
      <button className="auth-back" onClick={close}>Quay lại xem lịch bơi</button>
    </section>
  </main>;
}

const Title = ({ eyebrow, title, description, action }) => <div className="page-title"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1><p>{description}</p></div>{action}</div>;
const Stat = ({ label, value, color, Icon }) => <div className="stat"><div className={'stat-icon ' + color}><Icon /></div><div><small>{label}</small><strong>{value}</strong><span>↗ Tăng so với tháng trước</span></div></div>;
function SessionCard({ session, book }) {
  const soldOut = session.left < 1;
  return <div className="session-card">
    <div className={'session-art ' + session.color}><Waves size={44} /><span>{soldOut ? 'Đã hết vé' : `Còn ${session.left}/${session.total} vé`}</span></div>
    <div className="session-content">
      <div className="session-time"><b>{session.time}</b><span>{session.date}</span></div>
      <h3>{session.service}</h3><p><MapPin size={14} /> {session.area}</p>
      <div className="card-bottom"><strong>{money(session.price)} <small>/ vé</small></strong><button className="outline" disabled={soldOut} onClick={() => book(session)}>Đặt vé <ArrowUpRight size={14} /></button></div>
    </div>
  </div>;
}
function Filters({ date = todayInput(), setDate = () => {}, onSearch = () => {}, loading = false } = {}) {
  return <div className="filters schedule-filters">
    <label>Ngày bơi<input type="date" min={todayInput()} value={date} onChange={event => setDate(event.target.value)} /></label>
    <button className="filter-button" onClick={onSearch} disabled={loading}>Tìm ca bơi</button>
  </div>;
}
function Schedule({ book }) {
  const [date, setDate] = useState(todayInput);
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searched, setSearched] = useState(false);
  const load = async selectedDate => {
    setLoading(true);
    setError('');
    setSearched(true);
    try {
      const response = await scheduleService.list({ date: selectedDate });
      setSessions(response.data.map(scheduleCard));
    } catch (requestError) {
      setError(errorMessage(requestError));
      setSessions([]);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { load(date); }, []);
  return <>
    <Title eyebrow="DỊCH VỤ VÀ LỊCH BƠI" title="Chọn ca bơi phù hợp" description="Chọn ngày muốn bơi, kiểm tra vé còn và giữ vé trong 5 phút để thanh toán." />
    <Filters date={date} setDate={setDate} onSearch={() => load(date)} loading={loading} />
    {error && <p className="flow-error" role="alert">{error}</p>}
    <div className="schedule-meta"><b>{loading ? 'Đang tải lịch bơi...' : `${sessions.length} ca bơi còn vé`}</b><span>Chọn ngày tương lai để đặt trước</span></div>
    {!loading && !error && sessions.length === 0 && searched && <div className="panel empty-orders">Không có ca bơi còn vé trong ngày này. Hãy chọn ngày khác.</div>}
    <div className="session-grid four">{sessions.map(session => <SessionCard key={session.maLich} session={session} book={book} />)}</div>
  </>;
}
function Booking({ session, people, setPeople, close, done }) {
  const [reservation, setReservation] = useState(null);
  const [remaining, setRemaining] = useState(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  useEffect(() => {
    if (!reservation?.holdExpiresAt) return undefined;
    const update = () => setRemaining(Math.max(0, Math.ceil((new Date(reservation.holdExpiresAt).getTime() - Date.now()) / 1000)));
    update();
    const interval = setInterval(update, 1000);
    return () => clearInterval(interval);
  }, [reservation]);
  const reserve = async () => {
    setBusy(true);
    setError('');
    try {
      const response = await bookingService.create({ maLich: session.maLich, soNguoi: people });
      setReservation(response.data);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setBusy(false);
    }
  };
  const pay = async () => {
    setBusy(true);
    setError('');
    try {
      const response = await paymentService.pay({ maDat: reservation.maDat });
      done(response.data);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setBusy(false);
    }
  };
  const cancelHold = async () => {
    if (reservation && reservation.trangThai === 'CHO_THANH_TOAN' && remaining > 0) {
      setBusy(true);
      setError('');
      try {
        await bookingService.cancel(reservation.maDat);
      } catch (requestError) {
        setError(errorMessage(requestError));
        setBusy(false);
        return;
      }
      setBusy(false);
    }
    close();
  };
  const canReserve = people > 0 && people <= session.left;
  return <div className="modal-wrap"><div className="booking-modal">
    <button className="close" onClick={cancelHold} disabled={busy}><X /></button>
    <div className="eyebrow">{reservation ? 'GIỮ VÉ THÀNH CÔNG' : 'XÁC NHẬN ĐẶT VÉ'}</div>
    <h2>{reservation ? 'Thanh toán vé bơi' : 'Xác nhận đặt vé'}</h2>
    <p>{reservation ? 'Vui lòng thanh toán trước khi hết thời gian giữ vé.' : 'Vé chỉ được giữ trong 5 phút sau khi xác nhận.'}</p>
    <div className="checkout-session"><div className={'mini-art ' + session.color}><Waves /></div><div><b>{session.service}</b><span>{session.date} - {session.time}</span><span>{session.area}</span></div></div>
    <div className="people-control"><span>Số vé <small>({session.left} vé còn)</small></span><div><button disabled={!!reservation || people <= 1} onClick={() => setPeople(Math.max(1, people - 1))}>-</button><b>{people}</b><button disabled={!!reservation || people >= session.left} onClick={() => setPeople(Math.min(session.left, people + 1))}>+</button></div></div>
    <div className="price-line"><span>{money(session.price)} x {people} vé</span><b>{money(session.price * people)}</b></div>
    {reservation && <div className={'hold-timer ' + (remaining === 0 ? 'expired' : '')}>{remaining > 0 ? `Thời gian giữ vé còn ${Math.floor(remaining / 60)}:${String(remaining % 60).padStart(2, '0')}` : 'Đã hết thời gian giữ vé. Hãy đặt lại.'}</div>}
    {error && <p className="flow-error" role="alert">{error}</p>}
    <div className="secure-note"><CreditCard size={16} /> Thanh toán mô phỏng, không phát sinh giao dịch thật.</div>
    {!reservation
      ? <button className="primary wide" disabled={busy || !canReserve} onClick={reserve}>{busy ? 'Đang giữ vé...' : 'Giữ vé và tiếp tục thanh toán'}</button>
      : <button className="primary wide" disabled={busy || remaining === 0 || reservation.trangThai !== 'CHO_THANH_TOAN'} onClick={pay}>{busy ? 'Đang xử lý...' : `Thanh toán ${money(reservation.tongTien)}`}</button>}
    {reservation && <button className="auth-back" disabled={busy} onClick={cancelHold}>Hủy giữ vé</button>}
  </div></div>;
}
function Modal({ children, close }) { return <div className="modal-wrap"><div className="success-modal"><button className="close" onClick={close}><X /></button>{children}</div></div>; }
function Orders({ show }) {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [working, setWorking] = useState('');
  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await bookingService.mine();
      setOrders(response.data);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { load(); }, []);
  const runOrderAction = async (order, action) => {
    setWorking(order.maDat);
    setError('');
    try {
      if (action === 'pay') await paymentService.pay({ maDat: order.maDat });
      else await bookingService.cancel(order.maDat);
      await load();
    } catch (requestError) {
      await load();
      setError(errorMessage(requestError));
    } finally {
      setWorking('');
    }
  };
  return <>
    <Title eyebrow="QUẢN LÝ ĐƠN ĐẶT" title="Đơn của tôi" description="Theo dõi thanh toán, thời hạn giữ chỗ và hạn sử dụng vé." />
    {error && <p className="flow-error" role="alert">{error}</p>}
    <div className="panel order-panel"><table><thead><tr><th>Mã đơn</th><th>Dịch vụ</th><th>Ngày và giờ bơi</th><th>Số vé</th><th>Tổng tiền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
      <tbody>{orders.map(order => <tr key={order.maDat}>
        <td><b>{order.maDat}</b><small>Đặt lúc {formatDateTime(order.ngayDat)}</small></td>
        <td><b>{order.dichVu}</b><small>{order.khuVuc}</small></td>
        <td>{formatDate(order.ngay)}<small>{String(order.gioBatDau).slice(0, 5)} - {String(order.gioKetThuc).slice(0, 5)}</small>
          <small>{order.trangThai === 'CHO_THANH_TOAN' ? `Giữ vé đến ${formatDateTime(order.holdExpiresAt)}` : `Vé dùng đến ${formatDateTime(order.validUntil)}`}</small></td>
        <td>{order.soNguoi}</td><td><b>{money(Number(order.tongTien))}</b></td>
        <td><span className={'status ' + statusClass(order.trangThai)}>{statusText[order.trangThai] || order.trangThai}</span></td>
        <td className="order-actions">
          {order.trangThai === 'CHO_THANH_TOAN' && <><button className="primary order-action" disabled={working === order.maDat} onClick={() => runOrderAction(order, 'pay')}>Thanh toán</button><button className="auth-link" disabled={working === order.maDat} onClick={() => runOrderAction(order, 'cancel')}>Hủy giữ vé</button></>}
          {order.maVeQr && <button className="more-action" aria-label="Xem mã vé" onClick={() => show(order)}><QrCode size={17} /></button>}
        </td>
      </tr>)}</tbody></table>
      {loading && <p className="empty-orders">Đang tải đơn hàng...</p>}
      {!loading && !error && orders.length === 0 && <p className="empty-orders">Bạn chưa có đơn đặt vé nào.</p>}
    </div>
  </>;
}
function CheckIn() {
  const [code, setCode] = useState('');
  const [checkedIn, setCheckedIn] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const verify = async () => {
    setBusy(true);
    setError('');
    setCheckedIn(null);
    try {
      const response = await checkInService.check({ maVeQr: code.trim() });
      setCheckedIn(response.data);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setBusy(false);
    }
  };
  return <><Title eyebrow="VẬN HÀNH - STAFF" title="Check-in tại quầy" description="Nhập mã vé đã thanh toán để kiểm tra và xác nhận lượt sử dụng." /><div className="checkin-layout"><div className="panel scanner"><div className="scanner-head"><div><h3>Nhập mã vé</h3><p>Chức năng quét camera có thể bổ sung sau; hiện nhập mã vé thủ công.</p></div><span className="live">● Sẵn sàng</span></div><div className="manual"><input value={code} onChange={event => setCode(event.target.value)} placeholder="Nhập mã vé, ví dụ: QR-DL-..." /><button className="primary" disabled={busy || !code.trim()} onClick={verify}>{busy ? 'Đang kiểm tra...' : 'Check-in'}</button></div></div><div className="panel checkin-result">{error ? <div className="empty-result"><QrCode size={44} /><h3>Không thể check-in</h3><p className="flow-error" role="alert">{error}</p></div> : checkedIn ? <><span className="status confirmed">Đã check-in</span><h3>Vé hợp lệ</h3><p>Mã vé: {checkedIn.maVeQr}</p><div className="result-details"><span><Users size={15} /> {checkedIn.soNguoi} vé</span><span>{statusText[checkedIn.trangThai]}</span></div></> : <div className="empty-result"><QrCode size={44} /><h3>Thông tin vé</h3><p>Kết quả check-in sẽ hiển thị tại đây.</p></div>}</div></div></>;
}
function Manage() {
  const [services, setServices] = useState([]);
  const [areas, setAreas] = useState([]);
  const [schedules, setSchedules] = useState([]);
  const [form, setForm] = useState({
    maDv: '',
    maKhuVuc: '',
    ngay: dateAfter(1),
    gioBatDau: '08:00',
    gioKetThuc: '09:30',
    sucChua: 1
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [serviceResponse, areaResponse, scheduleResponse] = await Promise.all([
        adminService.services(),
        adminService.areas(),
        adminService.schedules()
      ]);
      setServices(serviceResponse.data);
      setAreas(areaResponse.data);
      setSchedules(scheduleResponse.data);
      setForm(current => ({
        ...current,
        maDv: current.maDv || serviceResponse.data[0]?.maDv || '',
        maKhuVuc: current.maKhuVuc || areaResponse.data[0]?.maKhuVuc || ''
      }));
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { load(); }, []);
  const area = areas.find(item => item.maKhuVuc === form.maKhuVuc);
  const update = event => setForm(current => ({
    ...current,
    [event.target.name]: event.target.name === 'sucChua' ? Number(event.target.value) : event.target.value
  }));
  const createSchedule = async event => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await adminService.createSchedule(form);
      await load();
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };
  const upcoming = schedules
    .filter(item => item.ngay >= todayInput())
    .sort((left, right) => `${left.ngay}${left.gioBatDau}`.localeCompare(`${right.ngay}${right.gioBatDau}`));
  return <>
    <Title eyebrow="QUẢN LÝ VẬN HÀNH" title="Lịch ca bơi" description="Tạo lịch bán vé trước cho hôm nay hoặc những ngày sắp tới." />
    {error && <p className="flow-error" role="alert">{error}</p>}
    <form className="panel schedule-create" onSubmit={createSchedule}>
      <h3>Tạo ca bơi mới</h3>
      <div className="schedule-create-grid">
        <label>Dịch vụ<select name="maDv" value={form.maDv} onChange={update} required>{services.map(item => <option key={item.maDv} value={item.maDv}>{item.tenDv} · {money(Number(item.donGia))}</option>)}</select></label>
        <label>Khu vực<select name="maKhuVuc" value={form.maKhuVuc} onChange={update} required>{areas.map(item => <option key={item.maKhuVuc} value={item.maKhuVuc}>{item.tenKhuVuc}</option>)}</select></label>
        <label>Ngày bơi<input name="ngay" type="date" min={todayInput()} value={form.ngay} onChange={update} required /></label>
        <label>Bắt đầu<input name="gioBatDau" type="time" value={form.gioBatDau} onChange={update} required /></label>
        <label>Kết thúc<input name="gioKetThuc" type="time" value={form.gioKetThuc} onChange={update} required /></label>
        <label>Sức chứa (tối đa {area?.sucChua || 0})<input name="sucChua" type="number" min="1" max={area?.sucChua || 1} value={form.sucChua} onChange={update} required /></label>
      </div>
      <button className="primary" type="submit" disabled={saving || loading || !services.length || !areas.length}>{saving ? 'Đang tạo...' : 'Tạo ca bơi'}</button>
    </form>
    <div className="panel manage-panel"><div className="panel-head"><div><h3>Ca bơi đã lên lịch</h3><p>{upcoming.length} ca từ hôm nay trở đi</p></div></div>
      <table><thead><tr><th>Ngày</th><th>Dịch vụ / Khu vực</th><th>Giờ bơi</th><th>Vé còn</th><th>Trạng thái</th></tr></thead>
        <tbody>{upcoming.map(item => <tr key={item.maLich}>
          <td><b>{formatDate(item.ngay)}</b><small>{item.maLich}</small></td>
          <td><b>{services.find(service => service.maDv === item.maDv)?.tenDv || item.maDv}</b><small>{areas.find(pool => pool.maKhuVuc === item.maKhuVuc)?.tenKhuVuc || item.maKhuVuc}</small></td>
          <td>{String(item.gioBatDau).slice(0, 5)} - {String(item.gioKetThuc).slice(0, 5)}</td>
          <td>{item.soChoCon}/{item.sucChua}</td><td><span className={'status ' + (item.trangThai === 'MO' ? 'confirmed' : item.trangThai === 'HET_CHO' ? 'pending' : 'done')}>{item.trangThai}</span></td>
        </tr>)}</tbody>
      </table>
      {loading && <p className="empty-orders">Đang tải lịch...</p>}
      {!loading && !error && upcoming.length === 0 && <p className="empty-orders">Chưa có ca bơi sắp tới. Tạo ca mới để mở bán vé.</p>}
    </div>
  </>;
}
function Reports() { return <><Title eyebrow="BÁO CÁO - ADMIN" title="Tổng quan hoạt động" description="Theo dõi doanh thu và hiệu suất vận hành hồ bơi." action={<button className="outline"><CalendarDays size={16} /> 01/06 - 24/06/2026 <ChevronDown size={14} /></button>} /><div className="stats report-stats"><Stat label="Doanh thu" value="42.580.000 đ" color="blue" Icon={CreditCard} /><Stat label="Tổng đơn hàng" value="386" color="mint" Icon={Ticket} /><Stat label="Lượt check-in" value="342" color="orange" Icon={ScanLine} /><Stat label="Tỷ lệ lấp đầy" value="76,8%" color="purple" Icon={Users} /></div><div className="report-grid"><div className="panel chart"><div className="panel-head"><div><h3>Doanh thu theo ngày</h3><p>Tháng 06/2026</p></div><span className="legend">● Doanh thu</span></div><div className="chart-body"><div className="y-labels"><span>4tr</span><span>3tr</span><span>2tr</span><span>1tr</span><span>0</span></div><div className="bars">{[38, 54, 45, 70, 58, 82, 63, 76, 92, 69, 78, 88, 73, 96].map((height, index) => <div className="bar-col" key={index}><div className="bar" style={{ height: height + '%' }} /><small>{index + 1}</small></div>)}</div></div></div><div className="panel popular"><div className="panel-head"><div><h3>Dịch vụ phổ biến</h3><p>Theo số vé đã bán</p></div><MoreHorizontal size={19} /></div>{[['Vé bơi tự do', 48], ['Vé bơi buổi chiều', 32], ['Vé bơi trẻ em', 20]].map(item => <div className="popular-row" key={item[0]}><div className="service-dot"><Waves size={15} /></div><div><b>{item[0]}</b><div className="progress"><i style={{ width: item[1] * 1.7 + '%' }} /></div></div><strong>{item[1]}%</strong></div>)}</div></div></>; }

createRoot(document.getElementById('root')).render(<AuthProvider><App /></AuthProvider>);
