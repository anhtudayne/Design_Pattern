import { useState, useEffect, useCallback } from 'react';

const API = 'http://localhost:8080/api';
const getAuthHeaders = () => {
  const token = localStorage.getItem('starcine_token');
  return { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) };
};

const SEAT_TYPES = ['STANDARD', 'VIP', 'COUPLE'];
// Assumes seed ids in DB: 1=STANDARD, 2=VIP, 3=COUPLE
const SEAT_TYPE_ID = { STANDARD: 1, VIP: 2, COUPLE: 3 };
const SCREEN_TYPES = ['2D', '3D', 'IMAX', '4DX', 'SCREENX'];
const SEAT_COLORS = {
  STANDARD: { bg: 'bg-sky-100 dark:bg-sky-900/30', border: 'border-sky-300 dark:border-sky-700', text: 'text-sky-700 dark:text-sky-300', label: 'Standard', icon: 'event_seat' },
  VIP:      { bg: 'bg-amber-100 dark:bg-amber-900/30', border: 'border-amber-300 dark:border-amber-700', text: 'text-amber-700 dark:text-amber-300', label: 'VIP', icon: 'star' },
  COUPLE:   { bg: 'bg-pink-100 dark:bg-pink-900/30', border: 'border-pink-300 dark:border-pink-700', text: 'text-pink-700 dark:text-pink-300', label: 'Couple', icon: 'favorite' },
};

// ── Toast ────────────────────────────────────────────────────────────
function Toast({ msg, type, onClose }) {
  useEffect(() => { const t = setTimeout(onClose, 3000); return () => clearTimeout(t); }, [onClose]);
  const colors = { success: 'bg-green-600', error: 'bg-red-600', info: 'bg-blue-600' };
  return (
    <div className={`fixed bottom-6 right-6 z-[100] flex items-center gap-3 px-5 py-3.5 rounded-2xl text-white shadow-2xl text-sm font-semibold font-['Space_Grotesk'] backdrop-blur-xl ${colors[type] || colors.info}`}>
      <span className="material-symbols-outlined text-lg">{type === 'success' ? 'check_circle' : type === 'error' ? 'error' : 'info'}</span>
      {msg}
      <button onClick={onClose} className="ml-2 opacity-70 hover:opacity-100"><span className="material-symbols-outlined text-base">close</span></button>
    </div>
  );
}

// ── Modal (Glassmorphism) ────────────────────────────────────────────
function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-md p-4">
      <div className="bg-white/90 dark:bg-slate-900/90 backdrop-blur-xl rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto border border-slate-200/30 dark:border-slate-700/30 font-['Space_Grotesk']">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-800">
          <h3 className="font-bold text-slate-800 dark:text-white text-lg">{title}</h3>
          <button onClick={onClose} className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"><span className="material-symbols-outlined text-slate-500 dark:text-slate-400">close</span></button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}

const inputCls = "w-full px-3 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-sm text-slate-800 dark:text-white bg-white dark:bg-slate-800 focus:outline-none focus:border-orange-400 focus:ring-2 focus:ring-orange-100 dark:focus:ring-orange-900/30 transition-all font-['Space_Grotesk']";
const selectCls = inputCls;

// ── Tree Node ────────────────────────────────────────────────────────
function TreeNode({ icon, label, badge, isSelected, isOpen, onToggle, onSelect, onAdd, onEdit, onDelete, children, level = 0 }) {
  const hasChildren = !!children;
  return (
    <div>
      <div
        className={`group flex items-center gap-2 px-3 py-2.5 rounded-xl cursor-pointer transition-all duration-200 text-sm font-['Space_Grotesk'] ${
          isSelected
            ? 'bg-gradient-to-r from-orange-500 to-orange-600 text-white font-bold shadow-lg shadow-orange-200 dark:shadow-orange-900/30'
            : 'hover:bg-slate-50 dark:hover:bg-slate-800/50 text-slate-700 dark:text-slate-300'
        }`}
        style={{ paddingLeft: `${12 + level * 20}px` }}
      >
        {hasChildren ? (
          <button onClick={onToggle} className="w-5 h-5 flex items-center justify-center shrink-0">
            <span className={`material-symbols-outlined text-sm transition-transform duration-200 ${isSelected ? 'text-white/70' : 'text-slate-400'} ${isOpen ? 'rotate-90' : ''}`}>chevron_right</span>
          </button>
        ) : <span className="w-5" />}
        <span className={`material-symbols-outlined text-lg ${isSelected ? 'text-white' : 'text-slate-400 dark:text-slate-500'}`}>{icon}</span>
        <span className="flex-1 truncate" onClick={onSelect}>{label}</span>
        {badge && <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${isSelected ? 'bg-white/20 text-white' : 'bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400'}`}>{badge}</span>}
        <div className={`${isSelected ? 'flex' : 'hidden group-hover:flex'} items-center gap-0.5 shrink-0`}>
          {onAdd && <button onClick={(e) => { e.stopPropagation(); onAdd(); }} className={`w-6 h-6 flex items-center justify-center rounded-lg transition-colors ${isSelected ? 'hover:bg-white/20' : 'hover:bg-green-50 dark:hover:bg-green-900/20'}`} title="Thêm"><span className={`material-symbols-outlined text-base ${isSelected ? 'text-white' : 'text-green-500'}`}>add</span></button>}
          {onEdit && <button onClick={(e) => { e.stopPropagation(); onEdit(); }} className={`w-6 h-6 flex items-center justify-center rounded-lg transition-colors ${isSelected ? 'hover:bg-white/20' : 'hover:bg-blue-50 dark:hover:bg-blue-900/20'}`} title="Sửa"><span className={`material-symbols-outlined text-base ${isSelected ? 'text-white' : 'text-blue-500'}`}>edit</span></button>}
          {onDelete && <button onClick={(e) => { e.stopPropagation(); onDelete(); }} className={`w-6 h-6 flex items-center justify-center rounded-lg transition-colors ${isSelected ? 'hover:bg-white/20' : 'hover:bg-red-50 dark:hover:bg-red-900/20'}`} title="Xóa"><span className={`material-symbols-outlined text-base ${isSelected ? 'text-white' : 'text-red-500'}`}>delete</span></button>}
        </div>
      </div>
      {isOpen && children && <div className="mt-0.5">{children}</div>}
    </div>
  );
}

// ── Seat Map Editor ──────────────────────────────────────────────────
function SeatMapEditor({ room, notify }) {
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [genRows, setGenRows] = useState(8);
  const [genCols, setGenCols] = useState(12);
  const [currentBrush, setCurrentBrush] = useState('STANDARD');

  const loadSeats = useCallback(async () => {
    setLoading(true);
    try {
      const r = await fetch(`${API}/seats?roomId=${room.roomId}`, { headers: getAuthHeaders() });
      const data = await r.json();
      setSeats(data.map(s => ({ ...s, seatType: s.seatTypeName || 'STANDARD' })));
      setDirty(false);
    } finally { setLoading(false); }
  }, [room.roomId]);

  useEffect(() => { loadSeats(); }, [loadSeats]);

  // Group by row
  const byRow = {};
  seats.forEach(s => { (byRow[s.seatRow] = byRow[s.seatRow] || []).push(s); });
  const sortedRows = Object.keys(byRow).sort();
  sortedRows.forEach(r => byRow[r].sort((a, b) => a.seatNumber - b.seatNumber));
  const maxCols = Math.max(0, ...seats.map(s => s.seatNumber));

  // Stats


  const handleGenerate = () => {
    const newSeats = [];
    for (let r = 0; r < genRows; r++) {
      const row = String.fromCharCode(65 + r);
      for (let c = 1; c <= genCols; c++) {
        newSeats.push({ roomId: room.roomId, seatRow: row, seatNumber: c, seatType: 'STANDARD', seatTypeId: SEAT_TYPE_ID.STANDARD, isActive: true });
      }
    }
    setSeats(newSeats);
    setDirty(true);
  };

  const handleSeatClick = (seatRow, seatNumber) => {
    setSeats(prev => prev.map(s => {
      if (s.seatRow === seatRow && s.seatNumber === seatNumber) return { ...s, seatType: currentBrush, seatTypeId: SEAT_TYPE_ID[currentBrush] };
      return s;
    }));
    setDirty(true);
  };

  const handleRemoveSeat = (seatRow, seatNumber) => {
    setSeats(prev => prev.filter(s => !(s.seatRow === seatRow && s.seatNumber === seatNumber)));
    setDirty(true);
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      // BATCH: 1 request thay vì N delete + M create
      const payload = seats.map(s => ({
        roomId: room.roomId,
        seatRow: s.seatRow,
        seatNumber: s.seatNumber,
        seatTypeId: s.seatTypeId || SEAT_TYPE_ID[s.seatType] || SEAT_TYPE_ID.STANDARD,
        isActive: s.isActive !== false,
      }));
      const res = await fetch(`${API}/seats/batch/${room.roomId}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
      });
      if (!res.ok) {
        let msg = `Lưu thất bại (${res.status})`;
        try {
          const text = await res.text();
          if (text) msg = text.length > 280 ? text.slice(0, 280) + '…' : text;
        } catch { /* ignore */ }
        throw new Error(msg);
      }
      notify('Lưu sơ đồ ghế thành công!', 'success');
      setDirty(false);
      loadSeats();
    } catch (err) {
      notify('Lưu thất bại: ' + err.message, 'error');
    } finally { setSaving(false); }
  };

  if (loading) {
    return <div className="flex items-center justify-center h-96"><div className="w-10 h-10 border-4 border-orange-200 border-t-orange-500 rounded-full animate-spin" /></div>;
  }

  return (
    <div className="space-y-6 font-['Space_Grotesk']">
      {/* Room header */}
      <div className="flex items-center justify-between flex-wrap gap-4">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-blue-500 to-violet-600 flex items-center justify-center shadow-lg shadow-blue-200 dark:shadow-blue-900/30">
            <span className="material-symbols-outlined text-white text-2xl">meeting_room</span>
          </div>
          <div>
            <h3 className="font-bold text-slate-800 dark:text-white text-xl">{room.name}</h3>
            <div className="flex items-center gap-2 mt-0.5">
              <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-violet-100 dark:bg-violet-900/30 text-violet-600 dark:text-violet-400">{room.screenType || '2D'}</span>
              <span className="text-xs text-slate-400 dark:text-slate-500 font-medium">{seats.length} ghế • {sortedRows.length} hàng × {maxCols} cột</span>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          {dirty && (
            <span className="text-xs font-bold text-amber-600 dark:text-amber-400 flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800">
              <span className="material-symbols-outlined text-sm">warning</span>Chưa lưu
            </span>
          )}
          <button onClick={handleSave} disabled={saving || !dirty}
            className={`flex items-center gap-2 px-6 py-2.5 rounded-xl text-sm font-bold transition-all duration-300 ${
              dirty
                ? 'bg-gradient-to-r from-orange-500 to-orange-600 text-white hover:shadow-lg hover:shadow-orange-200 dark:hover:shadow-orange-900/40 hover:scale-[1.02]'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-400 cursor-not-allowed'
            }`}>
            {saving ? <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" /> : <span className="material-symbols-outlined text-lg">save</span>}
            Lưu sơ đồ
          </button>
        </div>
      </div>


      {/* Brush selector */}
      <div className="bg-slate-50 dark:bg-slate-800/50 rounded-2xl p-4 space-y-3 border border-slate-100 dark:border-slate-800">
        <p className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Công cụ vẽ — Click ghế để đổi loại</p>
        <div className="flex items-center gap-2 flex-wrap">
          {SEAT_TYPES.map(t => (
            <button key={t} onClick={() => setCurrentBrush(t)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold border-2 transition-all duration-200 ${
                currentBrush === t
                  ? `${SEAT_COLORS[t].bg} ${SEAT_COLORS[t].border} ${SEAT_COLORS[t].text} shadow-md scale-105`
                  : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700 text-slate-500 dark:text-slate-400 hover:border-slate-300'
              }`}>
              <span className={`material-symbols-outlined text-sm`}>{SEAT_COLORS[t].icon}</span>
              {SEAT_COLORS[t].label}
            </button>
          ))}
          <div className="w-px h-8 bg-slate-200 dark:bg-slate-700 mx-1" />
          <button onClick={() => setCurrentBrush('REMOVE')}
            className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold border-2 transition-all duration-200 ${
              currentBrush === 'REMOVE' ? 'bg-red-50 dark:bg-red-900/20 border-red-300 dark:border-red-800 text-red-600 dark:text-red-400 shadow-md' : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700 text-slate-500 hover:border-slate-300'
            }`}>
            <span className="material-symbols-outlined text-sm">close</span>Xóa ghế
          </button>
        </div>
      </div>

      {/* Seat Canvas */}
      {seats.length > 0 ? (
        <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 md:p-8 shadow-sm">
          {/* Screen indicator */}
          <div className="flex flex-col items-center mb-10">
            <div className="w-2/3 h-2 bg-gradient-to-r from-transparent via-orange-400 to-transparent rounded-full mb-2 shadow-[0_0_20px_rgba(249,115,22,0.3)]" />
            <span className="text-[10px] font-bold text-slate-400 dark:text-slate-500 uppercase tracking-[0.4em]">Màn hình</span>
          </div>

          {/* Seat Grid — centered */}
          <div className="overflow-x-auto pb-4">
            <div className="flex flex-col items-center gap-1.5 min-w-fit mx-auto">
              {sortedRows.map(row => (
                <div key={row} className="flex items-center justify-center gap-1.5">
                  <span className="w-7 text-center text-xs font-bold text-slate-400 dark:text-slate-500 shrink-0 select-none">{row}</span>
                  <div className="flex items-center gap-1.5">
                    {Array.from({ length: maxCols }, (_, i) => i + 1).map(num => {
                      const seat = byRow[row]?.find(s => s.seatNumber === num);
                      if (!seat) return <div key={num} className="w-9 h-9 rounded-lg border-2 border-dashed border-slate-100 dark:border-slate-800 opacity-30" />;
                      const c = SEAT_COLORS[seat.seatType] || SEAT_COLORS.STANDARD;
                      return (
                        <button key={num}
                          onClick={() => currentBrush === 'REMOVE' ? handleRemoveSeat(row, num) : handleSeatClick(row, num)}
                          title={`${row}${num} — ${seat.seatType}`}
                          className={`w-9 h-9 rounded-lg border-2 ${c.bg} ${c.border} ${c.text} text-[11px] font-bold transition-all duration-150 hover:scale-110 hover:shadow-lg hover:z-10 relative`}>
                          {num}
                        </button>
                      );
                    })}
                  </div>
                  <span className="w-7 text-center text-xs font-bold text-slate-400 dark:text-slate-500 shrink-0 select-none">{row}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Legend */}
          <div className="flex items-center justify-center gap-6 mt-6 pt-5 border-t border-slate-100 dark:border-slate-800">
            {SEAT_TYPES.map(t => (
              <div key={t} className="flex items-center gap-2">
                <span className={`w-5 h-5 rounded-md border-2 ${SEAT_COLORS[t].bg} ${SEAT_COLORS[t].border}`} />
                <span className="text-[11px] font-semibold text-slate-500 dark:text-slate-400">{SEAT_COLORS[t].label}</span>
              </div>
            ))}
            <div className="flex items-center gap-2">
              <span className="w-5 h-5 rounded-md border-2 border-dashed border-slate-200 dark:border-slate-700 opacity-40" />
              <span className="text-[11px] font-semibold text-slate-400 dark:text-slate-500">Trống</span>
            </div>
          </div>

          {/* Quick Regenerate */}
          <div className="mt-6 pt-5 border-t border-slate-100 dark:border-slate-800 flex items-end gap-3 flex-wrap">
            <p className="text-[11px] font-bold text-slate-500 dark:text-slate-400 mr-auto flex items-center gap-1.5">
              <span className="material-symbols-outlined text-sm">auto_fix_high</span>Tạo lại lưới
            </p>
            <div>
              <label className="text-[10px] font-bold text-slate-400 block mb-0.5">Hàng</label>
              <input type="number" min={1} max={26} value={genRows} onChange={e => setGenRows(+e.target.value)} className={inputCls + ' !w-16 !py-1.5 !text-xs'} />
            </div>
            <div>
              <label className="text-[10px] font-bold text-slate-400 block mb-0.5">Cột</label>
              <input type="number" min={1} max={30} value={genCols} onChange={e => setGenCols(+e.target.value)} className={inputCls + ' !w-16 !py-1.5 !text-xs'} />
            </div>
            <button onClick={handleGenerate} className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-slate-800 dark:bg-slate-700 text-white text-xs font-bold hover:bg-slate-700 dark:hover:bg-slate-600 transition-colors">
              <span className="material-symbols-outlined text-sm">refresh</span>Reset
            </button>
          </div>
        </div>
      ) : (
        <div className="bg-white dark:bg-slate-900 rounded-2xl border-2 border-dashed border-slate-200 dark:border-slate-700 p-12 flex flex-col items-center gap-6">
          <div className="w-20 h-20 rounded-3xl bg-orange-50 dark:bg-orange-900/20 flex items-center justify-center">
            <span className="material-symbols-outlined text-orange-400 text-5xl">grid_view</span>
          </div>
          <div className="text-center">
            <h4 className="font-bold text-slate-700 dark:text-white text-lg">Phòng chưa có ghế</h4>
            <p className="text-sm text-slate-400 dark:text-slate-500 mt-1">Tạo nhanh sơ đồ ghế để bắt đầu cấu hình</p>
          </div>
          <div className="flex items-end gap-3">
            <div>
              <label className="text-xs font-bold text-slate-500 block mb-1.5">Số hàng</label>
              <input type="number" min={1} max={26} value={genRows} onChange={e => setGenRows(+e.target.value)} className={inputCls + ' !w-24'} />
            </div>
            <div>
              <label className="text-xs font-bold text-slate-500 block mb-1.5">Số cột</label>
              <input type="number" min={1} max={30} value={genCols} onChange={e => setGenCols(+e.target.value)} className={inputCls + ' !w-24'} />
            </div>
            <button onClick={handleGenerate} className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-gradient-to-r from-orange-500 to-orange-600 text-white text-sm font-bold hover:shadow-lg hover:shadow-orange-200 transition-all">
              <span className="material-symbols-outlined text-lg">auto_fix_high</span>Tạo lưới
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────
// MAIN PAGE
// ─────────────────────────────────────────────────────────────────────
export default function FacilitiesManagement() {
  const [toast, setToast] = useState(null);
  const notify = (msg, type = 'info') => setToast({ msg, type });

  const [locations, setLocations] = useState([]);
  const [cinemas, setCinemas] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [allSeats, setAllSeats] = useState([]);
  const [loadingTree, setLoadingTree] = useState(true);
  const [openLocations, setOpenLocations] = useState({});
  const [openCinemas, setOpenCinemas] = useState({});
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({});

  const loadTree = useCallback(async () => {
    setLoadingTree(true);
    try {
      const [lr, cr, rr, sr] = await Promise.all([
        fetch(`${API}/locations`, { headers: getAuthHeaders() }),
        fetch(`${API}/cinemas`, { headers: getAuthHeaders() }),
        fetch(`${API}/rooms`, { headers: getAuthHeaders() }),
        fetch(`${API}/seats`, { headers: getAuthHeaders() }),
      ]);
      setLocations(await lr.json());
      setCinemas(await cr.json());
      setRooms(await rr.json());
      setAllSeats(await sr.json());
    } finally { setLoadingTree(false); }
  }, []);

  useEffect(() => { loadTree(); }, [loadTree]);

  const cinemasOf = (locId) => cinemas.filter(c => c.locationId === locId);
  const roomsOf = (cinId) => rooms.filter(r => r.cinemaId === cinId);
  const seatsOfRoom = (rmId) => allSeats.filter(s => s.roomId === rmId);

  // CRUD
  const addLocation = () => { setForm({ name: '' }); setModal({ type: 'location', mode: 'add' }); };
  const editLocation = (loc) => { setForm({ name: loc.name }); setModal({ type: 'location', mode: 'edit', item: loc }); };
  const deleteLocation = async (loc) => {
    if (!window.confirm(`Xóa "${loc.name}"?`)) return;
    const r = await fetch(`${API}/locations/${loc.locationId}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (r.ok) { notify('Đã xóa!', 'success'); loadTree(); } else notify('Xóa thất bại', 'error');
  };
  const addCinema = (locId) => { setForm({ locationId: locId, name: '', address: '', hotline: '' }); setModal({ type: 'cinema', mode: 'add' }); };
  const editCinema = (cin) => { setForm({ locationId: cin.locationId, name: cin.name, address: cin.address, hotline: cin.hotline || '' }); setModal({ type: 'cinema', mode: 'edit', item: cin }); };
  const deleteCinema = async (cin) => {
    if (!window.confirm(`Xóa rạp "${cin.name}"?`)) return;
    const r = await fetch(`${API}/cinemas/${cin.cinemaId}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (r.ok) { notify('Đã xóa!', 'success'); loadTree(); } else notify('Xóa thất bại', 'error');
  };
  const addRoom = (cinId) => { setForm({ cinemaId: cinId, name: '', screenType: '2D' }); setModal({ type: 'room', mode: 'add' }); };
  const editRoom = (rm) => { setForm({ cinemaId: rm.cinemaId, name: rm.name, screenType: rm.screenType || '2D' }); setModal({ type: 'room', mode: 'edit', item: rm }); };
  const deleteRoom = async (rm) => {
    if (!window.confirm(`Xóa phòng "${rm.name}"?`)) return;
    const r = await fetch(`${API}/rooms/${rm.roomId}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (r.ok) { notify('Đã xóa!', 'success'); if (selectedRoom?.roomId === rm.roomId) setSelectedRoom(null); loadTree(); } else notify('Xóa thất bại', 'error');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const { type, mode, item } = modal;
    let url, method, body;
    if (type === 'location') {
      url = mode === 'edit' ? `${API}/locations/${item.locationId}` : `${API}/locations`;
      method = mode === 'edit' ? 'PUT' : 'POST';
      body = { name: form.name };
    } else if (type === 'cinema') {
      url = mode === 'edit' ? `${API}/cinemas/${item.cinemaId}` : `${API}/cinemas`;
      method = mode === 'edit' ? 'PUT' : 'POST';
      body = { locationId: Number(form.locationId), name: form.name, address: form.address, hotline: form.hotline };
    } else if (type === 'room') {
      url = mode === 'edit' ? `${API}/rooms/${item.roomId}` : `${API}/rooms`;
      method = mode === 'edit' ? 'PUT' : 'POST';
      body = { cinemaId: Number(form.cinemaId), name: form.name, screenType: form.screenType };
    }
    const r = await fetch(url, { method, headers: getAuthHeaders(), body: JSON.stringify(body) });
    if (r.ok) {
      notify(mode === 'edit' ? 'Cập nhật thành công!' : 'Thêm mới thành công!', 'success');
      setModal(null); loadTree();
    } else {
      try { const d = await r.json(); notify(d.message || 'Lỗi xảy ra', 'error'); } catch { notify('Lỗi xảy ra', 'error'); }
    }
  };

  const modalTitle = modal ? `${modal.mode === 'edit' ? 'Chỉnh sửa' : 'Thêm mới'} ${modal.type === 'location' ? 'Tỉnh/Thành' : modal.type === 'cinema' ? 'Cụm rạp' : 'Phòng chiếu'}` : '';

  return (
    <div className="min-h-screen bg-[#f8f9ff] dark:bg-slate-950 p-6 md:p-8 font-['Space_Grotesk']">
      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}

      {/* Header */}
      <div className="mb-8">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-orange-500 to-red-500 flex items-center justify-center shadow-lg shadow-orange-200 dark:shadow-orange-900/30">
            <span className="material-symbols-outlined text-white text-2xl">business</span>
          </div>
          <div>
            <h1 className="text-2xl font-black text-slate-800 dark:text-white">Cơ sở & Phòng chiếu</h1>
            <p className="text-sm text-slate-400 dark:text-slate-500 font-medium">Quản lý địa điểm, cụm rạp, phòng chiếu và sơ đồ ghế</p>
          </div>
        </div>
      </div>

      {/* Top Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-5 shadow-sm flex items-center gap-4 hover:shadow-md transition-shadow">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 flex items-center justify-center shadow-md shadow-blue-200 dark:shadow-blue-900/30">
            <span className="material-symbols-outlined text-white text-2xl">location_city</span>
          </div>
          <div>
            <p className="text-2xl font-black text-slate-800 dark:text-white">{locations.length}</p>
            <p className="text-xs text-slate-400 dark:text-slate-500 font-semibold">Tỉnh / Thành phố</p>
          </div>
        </div>
        <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-5 shadow-sm flex items-center gap-4 hover:shadow-md transition-shadow">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center shadow-md shadow-violet-200 dark:shadow-violet-900/30">
            <span className="material-symbols-outlined text-white text-2xl">stadium</span>
          </div>
          <div>
            <p className="text-2xl font-black text-slate-800 dark:text-white">{cinemas.length}</p>
            <p className="text-xs text-slate-400 dark:text-slate-500 font-semibold">Cụm rạp hoạt động</p>
          </div>
        </div>
        <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-5 shadow-sm flex items-center gap-4 hover:shadow-md transition-shadow">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-emerald-500 to-green-600 flex items-center justify-center shadow-md shadow-emerald-200 dark:shadow-emerald-900/30">
            <span className="material-symbols-outlined text-white text-2xl">chair</span>
          </div>
          <div>
            <p className="text-2xl font-black text-slate-800 dark:text-white">{allSeats.length.toLocaleString('vi-VN')}</p>
            <p className="text-xs text-slate-400 dark:text-slate-500 font-semibold">Tổng sức chứa (ghế)</p>
          </div>
        </div>
      </div>

      {/* Main 2-column */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* LEFT: Tree */}
        <div className="lg:col-span-5 xl:col-span-4">
          <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl rounded-2xl border border-slate-200/50 dark:border-slate-800/50 shadow-sm overflow-hidden sticky top-6">
            <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100 dark:border-slate-800">
              <h2 className="font-bold text-slate-700 dark:text-slate-200 flex items-center gap-2 text-sm">
                <span className="material-symbols-outlined text-lg text-orange-500">account_tree</span>
                Danh mục hệ thống
              </h2>
              <button onClick={addLocation} className="w-8 h-8 flex items-center justify-center rounded-xl bg-orange-50 dark:bg-orange-900/20 hover:bg-orange-100 dark:hover:bg-orange-900/30 transition-colors" title="Thêm tỉnh/thành">
                <span className="material-symbols-outlined text-orange-500 text-lg">add</span>
              </button>
            </div>

            <div className="p-3 max-h-[calc(100vh-280px)] overflow-y-auto space-y-0.5">
              {loadingTree ? (
                <div className="flex justify-center py-16"><div className="w-8 h-8 border-4 border-orange-200 border-t-orange-500 rounded-full animate-spin" /></div>
              ) : locations.length === 0 ? (
                <div className="py-16 text-center text-slate-400 dark:text-slate-500 text-sm">
                  <span className="material-symbols-outlined text-4xl block mb-2 opacity-50">location_off</span>
                  Chưa có dữ liệu.<br />Nhấn <b>+</b> để thêm tỉnh/thành.
                </div>
              ) : (
                locations.map(loc => (
                  <TreeNode key={loc.locationId} icon="location_city" label={loc.name}
                    badge={`${cinemasOf(loc.locationId).length}`}
                    isOpen={!!openLocations[loc.locationId]}
                    onToggle={() => setOpenLocations(p => ({ ...p, [loc.locationId]: !p[loc.locationId] }))}
                    onSelect={() => setOpenLocations(p => ({ ...p, [loc.locationId]: !p[loc.locationId] }))}
                    onAdd={() => addCinema(loc.locationId)} onEdit={() => editLocation(loc)} onDelete={() => deleteLocation(loc)}
                    level={0}>
                    {cinemasOf(loc.locationId).map(cin => (
                      <TreeNode key={cin.cinemaId} icon="stadium" label={cin.name}
                        badge={`${roomsOf(cin.cinemaId).length}`}
                        isOpen={!!openCinemas[cin.cinemaId]}
                        onToggle={() => setOpenCinemas(p => ({ ...p, [cin.cinemaId]: !p[cin.cinemaId] }))}
                        onSelect={() => setOpenCinemas(p => ({ ...p, [cin.cinemaId]: !p[cin.cinemaId] }))}
                        onAdd={() => addRoom(cin.cinemaId)} onEdit={() => editCinema(cin)} onDelete={() => deleteCinema(cin)}
                        level={1}>
                        {roomsOf(cin.cinemaId).map(rm => (
                          <TreeNode key={rm.roomId} icon="meeting_room" label={rm.name}
                            badge={`${seatsOfRoom(rm.roomId).length} ghế`}
                            isSelected={selectedRoom?.roomId === rm.roomId}
                            onSelect={() => setSelectedRoom(rm)}
                            onEdit={() => editRoom(rm)} onDelete={() => deleteRoom(rm)}
                            level={2} />
                        ))}
                      </TreeNode>
                    ))}
                  </TreeNode>
                ))
              )}
            </div>
          </div>
        </div>

        {/* RIGHT: Seat Map */}
        <div className="lg:col-span-7 xl:col-span-8">
          <div className="bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl rounded-2xl border border-slate-200/50 dark:border-slate-800/50 shadow-sm p-6 md:p-8 min-h-[500px]">
            {selectedRoom ? (
              <SeatMapEditor room={selectedRoom} notify={notify} key={selectedRoom.roomId} />
            ) : (
              <div className="flex flex-col items-center justify-center h-[500px] gap-5">
                <div className="w-24 h-24 rounded-3xl bg-slate-50 dark:bg-slate-800 flex items-center justify-center">
                  <span className="material-symbols-outlined text-slate-200 dark:text-slate-700 text-6xl">chair_alt</span>
                </div>
                <div className="text-center">
                  <h3 className="font-bold text-slate-500 dark:text-slate-400 text-lg">Chọn phòng chiếu</h3>
                  <p className="text-sm text-slate-400 dark:text-slate-500 mt-1 max-w-sm">
                    Click vào một phòng chiếu trong cây danh mục bên trái để xem và chỉnh sửa sơ đồ ghế
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* CRUD Modal */}
      {modal && (
        <Modal title={modalTitle} onClose={() => setModal(null)}>
          <form onSubmit={handleSubmit} className="space-y-4">
            {modal.type === 'location' && (
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Tên tỉnh / thành phố <span className="text-red-500">*</span></label>
                <input className={inputCls} value={form.name || ''} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="VD: Hồ Chí Minh" required />
              </div>
            )}
            {modal.type === 'cinema' && (
              <>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Tên cụm rạp <span className="text-red-500">*</span></label>
                  <input className={inputCls} value={form.name || ''} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="VD: StarCine Landmark" required />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Địa chỉ <span className="text-red-500">*</span></label>
                  <input className={inputCls} value={form.address || ''} onChange={e => setForm({ ...form, address: e.target.value })} placeholder="VD: Tầng 5, Landmark 81" required />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Hotline</label>
                  <input className={inputCls} value={form.hotline || ''} onChange={e => setForm({ ...form, hotline: e.target.value })} placeholder="VD: 1900 1234" />
                </div>
              </>
            )}
            {modal.type === 'room' && (
              <>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Tên phòng chiếu <span className="text-red-500">*</span></label>
                  <input className={inputCls} value={form.name || ''} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="VD: Phòng 1" required />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wide">Loại màn hình</label>
                  <select className={selectCls} value={form.screenType || '2D'} onChange={e => setForm({ ...form, screenType: e.target.value })}>
                    {SCREEN_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
              </>
            )}
            <div className="flex gap-3 pt-2">
              <button type="button" onClick={() => setModal(null)} className="flex-1 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 text-sm font-semibold hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors">Hủy</button>
              <button type="submit" className="flex-1 py-2.5 rounded-xl bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white text-sm font-bold transition-all">{modal.mode === 'edit' ? 'Cập nhật' : 'Thêm mới'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
