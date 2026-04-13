import { useState, useEffect, useCallback } from 'react';
import { getAuthHeaders, BASE_URL } from '../../utils/api';

const API = `${BASE_URL}/admin/showtimes`;

// ── Helpers ───────────────────────────────────────────────────────────────────
const inputCls  = 'w-full px-4 py-3 rounded-2xl border border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-100 transition-all font-medium';
const selectCls = inputCls + ' bg-white';

function Toast({ msg, type, onClose }) {
  useEffect(() => { const t = setTimeout(onClose, 3000); return () => clearTimeout(t); }, [onClose]);
  const colors = { success: 'bg-green-600', error: 'bg-red-600', info: 'bg-blue-600' };
  return (
    <div className="fixed bottom-6 right-6 z-[100] flex items-center gap-3 px-5 py-3.5 rounded-2xl text-white shadow-2xl text-sm font-bold border border-white/20 animate-in slide-in-from-bottom duration-300" 
         style={{ backgroundColor: colors[type] || colors.info }}>
      <span className="material-symbols-outlined text-lg">
        {type === 'success' ? 'check_circle' : type === 'error' ? 'error' : 'info'}
      </span>
      {msg}
      <button onClick={onClose} className="ml-2 opacity-70 hover:opacity-100">
        <span className="material-symbols-outlined text-base">close</span>
      </button>
    </div>
  );
}

function FormField({ label, required, children, icon }) {
  return (
    <div className="space-y-1.5">
      <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest ml-1 flex items-center gap-1.5">
        {icon && <span className="material-symbols-outlined text-sm">{icon}</span>}
        {label}{required && <span className="text-red-500 ml-1">*</span>}
      </label>
      {children}
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4 animate-in fade-in duration-300">
      <div className="bg-white rounded-[32px] shadow-2xl w-full max-w-xl max-h-[90vh] overflow-y-auto font-['Space_Grotesk'] border border-slate-100">
        <div className="flex items-center justify-between px-8 py-6 border-b border-slate-50">
          <h3 className="font-black text-slate-800 text-xl uppercase tracking-tighter flex items-center gap-3">
             <div className="w-2 h-8 bg-orange-500 rounded-full" />
             {title}
          </h3>
          <button onClick={onClose} className="w-10 h-10 flex items-center justify-center rounded-2xl hover:bg-slate-100 transition-colors text-slate-400">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>
        <div className="p-8">{children}</div>
      </div>
    </div>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function ShowtimeManagement() {
  const [list, setList]       = useState([]);
  const [movies, setMovies]     = useState([]);
  const [cinemas, setCinemas]   = useState([]);
  const [rooms, setRooms]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [saving, setSaving]     = useState(false);
  const [modal, setModal]       = useState(null);
  const [toast, setToast]       = useState(null);
  const [search, setSearch]     = useState('');

  const [form, setForm] = useState({
    movieId: '',
    cinemaId: '',
    roomId: '',
    startTime: '',
    basePrice: 50000
  });

  const notify = (msg, type = 'info') => setToast({ msg, type });

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [rShowtimes, rMovies, rRooms, rCinemas] = await Promise.all([
        fetch(API, { headers: getAuthHeaders() }),
        fetch(`${BASE_URL}/movies`, { headers: getAuthHeaders() }),
        fetch(`${BASE_URL}/rooms`, { headers: getAuthHeaders() }).catch(() => fetch(`${BASE_URL}/public/rooms`, { headers: getAuthHeaders() })),
        fetch(`${BASE_URL}/public/cinemas`)
      ]);

      if (rShowtimes.ok) setList(await rShowtimes.json());
      if (rMovies.ok) setMovies(await rMovies.json());
      
      // Attempt to get rooms (this endpoint might vary depending on project state)
      if (rRooms && rRooms.ok) {
        const roomData = await rRooms.json();
        setRooms(roomData);
      }
      else {
        // Fallback or handle error - here we expect it exists
        console.warn("Failed to load rooms");
      }
      if (rCinemas.ok) setCinemas(await rCinemas.json());
    } catch (err) {
      console.error(err);
      notify('Lỗi khi tải dữ liệu', 'error');
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { loadAll(); }, [loadAll]);

  const openAdd = () => {
    const defaultCinemaId = rooms[0]?.cinemaId || rooms[0]?.cinema?.cinemaId || '';
    const defaultRoom = rooms.find(r => (r.cinemaId || r.cinema?.cinemaId) === defaultCinemaId);
    setForm({
      movieId: movies[0]?.movieId || '',
      cinemaId: defaultCinemaId,
      roomId: defaultRoom?.roomId || '',
      startTime: '',
      basePrice: 50000
    });
    setModal('add');
  };

  const openEdit = (s) => {
    const room = rooms.find(r => r.roomId === s.roomId);
    setForm({
      movieId: s.movieId,
      cinemaId: room?.cinemaId || room?.cinema?.cinemaId || '',
      roomId: s.roomId,
      startTime: s.startTime.substring(0, 16), // Format for datetime-local
      basePrice: s.basePrice
    });
    setModal({ edit: s });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    const isEdit = !!modal?.edit;
    const url = isEdit ? `${API}/${modal.edit.showtimeId}` : API;
    const method = isEdit ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method,
        headers: getAuthHeaders(),
        body: JSON.stringify({
          movieId: Number(form.movieId),
          roomId: Number(form.roomId),
          startTime: form.startTime,
          basePrice: Number(form.basePrice)
        })
      });
      if (res.ok) {
        notify(isEdit ? 'Cập nhật lịch chiếu thành công!' : 'Tạo lịch chiếu thành công!', 'success');
        setModal(null);
        loadAll();
      } else {
        const err = await res.json().catch(() => ({ message: 'Lỗi máy chủ' }));
        notify(err.message, 'error');
      }
    } finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Xóa lịch chiếu này?")) return;
    const res = await fetch(`${API}/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (res.ok) { notify('Đã xóa!', 'success'); loadAll(); }
  };

  const filtered = list.filter(s => {
    const movie = movies.find(m => m.movieId === s.movieId);
    return movie?.title.toLowerCase().includes(search.toLowerCase());
  });
  const filteredRooms = rooms.filter(r => (r.cinemaId || r.cinema?.cinemaId) === Number(form.cinemaId));

  return (
    <div className="min-h-screen bg-[#FDFDFD] p-6 md:p-10 font-['Space_Grotesk'] antialiased">
      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}

      <div className="max-w-7xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-8 mb-12">
          <div className="flex items-center gap-6">
            <div className="w-16 h-16 rounded-[28px] bg-slate-900 flex items-center justify-center shadow-2xl shadow-slate-200">
               <span className="material-symbols-outlined text-orange-500 text-3xl">event_available</span>
            </div>
            <div>
               <h1 className="text-4xl font-black text-slate-800 tracking-tighter uppercase">Lịch chiếu</h1>
               <p className="text-sm text-slate-400 font-bold uppercase tracking-[2px] mt-1">Quản lý thời gian, rạp chiếu và bảng giá vé</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
             <div className="relative group">
                <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-orange-500 transition-colors">search</span>
                <input 
                   className="w-full md:w-80 pl-12 pr-4 py-3 rounded-2xl border border-slate-200 bg-white/50 backdrop-blur-sm focus:outline-none focus:border-orange-500 focus:ring-4 focus:ring-orange-100 transition-all font-bold text-sm"
                   placeholder="Tìm theo tên phim..."
                   value={search}
                   onChange={e => setSearch(e.target.value)}
                />
             </div>
             <button onClick={openAdd} className="flex items-center gap-2 px-6 py-3.5 rounded-2xl bg-orange-600 text-white text-sm font-black hover:bg-orange-700 transition-all shadow-xl shadow-orange-100 active:scale-95 uppercase tracking-wider">
               <span className="material-symbols-outlined">add_circle</span>
               Lên lịch mới
             </button>
          </div>
        </div>

        {loading ? (
             <div className="flex flex-col items-center justify-center py-40 gap-4">
                <div className="w-12 h-12 border-4 border-slate-100 border-t-orange-500 rounded-full animate-spin" />
                <p className="text-xs font-black text-slate-300 uppercase tracking-widest">Đang tải lịch chiếu từ hệ thống...</p>
             </div>
        ) : filtered.length === 0 ? (
          <div className="py-32 text-center bg-white rounded-[48px] border-4 border-dashed border-slate-50 shadow-inner">
             <span className="material-symbols-outlined text-7xl text-slate-100 block mb-6">theater_comedy</span>
             <p className="text-xl font-black text-slate-600 capitalize">Chưa có lịch chiếu nào được thiết lập</p>
             <p className="text-sm text-slate-400 mt-2 font-medium">Bấm "Lên lịch mới" để bắt đầu sắp xếp các suất chiếu cho phim.</p>
          </div>
        ) : (
          <div className="bg-white rounded-[40px] border border-slate-100 shadow-sm overflow-hidden overflow-x-auto">
            <table className="w-full text-left border-collapse min-w-[900px]">
              <thead>
                <tr className="bg-slate-50/50">
                  <th className="px-8 py-6 text-[11px] font-black text-slate-400 uppercase tracking-[2px]">Thông tin Phim</th>
                  <th className="px-8 py-6 text-[11px] font-black text-slate-400 uppercase tracking-[2px]">Phòng & Rạp</th>
                  <th className="px-8 py-6 text-[11px] font-black text-slate-400 uppercase tracking-[2px]">Thời gian</th>
                  <th className="px-8 py-6 text-[11px] font-black text-slate-400 uppercase tracking-[2px]">Giá vé & Phụ thu</th>
                  <th className="px-8 py-6 text-[11px] font-black text-slate-400 uppercase tracking-[2px] text-right">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {filtered.map(s => {
                  const m = movies.find(m => m.movieId === s.movieId);
                  const r = rooms.find(r => r.roomId === s.roomId);
                  const start = new Date(s.startTime);
                  return (
                    <tr key={s.showtimeId} className="group hover:bg-slate-50/30 transition-all active:bg-orange-50/20">
                      <td className="px-8 py-6">
                        <div className="flex items-center gap-4">
                           <div className="w-12 h-16 rounded-xl bg-slate-100 overflow-hidden shadow-sm">
                              {m?.posterUrl && <img src={m.posterUrl} className="w-full h-full object-cover" />}
                           </div>
                           <div>
                              <p className="font-black text-slate-800 tracking-tight leading-tight mb-1 uppercase text-sm group-hover:text-orange-600 transition-colors">{m?.title || 'Phim đã xóa'}</p>
                              <span className="px-2 py-0.5 rounded-lg bg-slate-100 text-[10px] font-black text-slate-500 uppercase">{m?.language || 'N/A'}</span>
                           </div>
                        </div>
                      </td>
                      <td className="px-8 py-6">
                         <p className="font-bold text-slate-700 tracking-tighter">{r?.name || `Phòng ${s.roomId}`}</p>
                         <p className="text-[11px] text-slate-400 font-bold uppercase mt-0.5 tracking-tight">{r?.cinema?.name || 'Chi nhánh StarCine'}</p>
                      </td>
                      <td className="px-8 py-6">
                         <div className="flex items-center gap-2 mb-1">
                            <span className="material-symbols-outlined text-orange-500 text-sm">calendar_today</span>
                            <span className="text-sm font-black text-slate-700">{start.toLocaleDateString('vi-VN')}</span>
                         </div>
                         <div className="flex items-center gap-2">
                            <span className="material-symbols-outlined text-blue-500 text-sm">schedule</span>
                            <span className="text-xs font-bold text-slate-500">{start.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} - {new Date(s.endTime).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}</span>
                         </div>
                      </td>
                      <td className="px-8 py-6">
                         <p className="font-black text-slate-800 tracking-tight">{s.basePrice.toLocaleString('vi-VN')}đ</p>
                         {s.surcharge > 0 && (
                            <p className="text-[10px] font-bold text-red-500 uppercase tracking-tighter mt-0.5">+ {s.surcharge.toLocaleString('vi-VN')}đ Phụ thu cuối tuần</p>
                         )}
                      </td>
                      <td className="px-8 py-6 text-right">
                         <div className="flex justify-end gap-2">
                            <button onClick={() => openEdit(s)} className="w-10 h-10 rounded-2xl bg-white border border-slate-100 text-slate-400 hover:text-orange-500 hover:bg-orange-50 hover:border-orange-100 transition-all flex items-center justify-center shadow-sm">
                               <span className="material-symbols-outlined text-lg">edit</span>
                            </button>
                            <button onClick={() => handleDelete(s.showtimeId)} className="w-10 h-10 rounded-2xl bg-white border border-slate-100 text-slate-400 hover:text-red-500 hover:bg-red-50 hover:border-red-100 transition-all flex items-center justify-center shadow-sm">
                               <span className="material-symbols-outlined text-lg">delete</span>
                            </button>
                         </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}

        {modal && (
          <Modal title={modal.edit ? 'Sửa lịch chiếu' : 'Tạo lịch chiếu mới'} onClose={() => setModal(null)}>
            <form onSubmit={handleSubmit} className="space-y-6">
               <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                  <FormField label="Chọn Phim" required icon="movie">
                     <select className={selectCls} value={form.movieId} onChange={e => setForm({ ...form, movieId: Number(e.target.value) })} required>
                        <option value="">-- Chọn phim --</option>
                        {movies.map(m => <option key={m.movieId} value={m.movieId}>{m.title}</option>)}
                     </select>
                  </FormField>
                  <FormField label="Chọn Chi nhánh" required icon="storefront">
                     <select
                       className={selectCls}
                       value={form.cinemaId}
                       onChange={e => {
                         const selectedCinemaId = Number(e.target.value);
                         const nextRoom = rooms.find(r => (r.cinemaId || r.cinema?.cinemaId) === selectedCinemaId);
                         setForm({ ...form, cinemaId: selectedCinemaId, roomId: nextRoom?.roomId || '' });
                       }}
                       required
                     >
                        <option value="">-- Chọn chi nhánh --</option>
                        {cinemas.map(c => <option key={c.cinemaId} value={c.cinemaId}>{c.name}</option>)}
                     </select>
                  </FormField>
                  <FormField label="Chọn Phòng" required icon="meeting_room">
                     <select className={selectCls} value={form.roomId} onChange={e => setForm({ ...form, roomId: Number(e.target.value) })} required>
                        <option value="">-- Chọn phòng --</option>
                        {filteredRooms.map(r => (
                          <option key={r.roomId} value={r.roomId}>
                            {(r.cinema?.name || r.cinemaName || 'Chi nhánh chưa rõ')} - {r.name}
                          </option>
                        ))}
                     </select>
                  </FormField>
               </div>

               <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                  <FormField label="Giờ bắt đầu" required icon="event">
                     <input className={inputCls} type="datetime-local" value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })} required />
                  </FormField>
                  <FormField label="Giá vé cơ bản (VNĐ)" required icon="payments">
                     <input className={inputCls} type="number" step="5000" min="40000" value={form.basePrice} onChange={e => setForm({ ...form, basePrice: e.target.value })} required />
                  </FormField>
               </div>

               <div className="bg-orange-50/50 p-6 rounded-[28px] border border-orange-100 space-y-2">
                  <div className="flex items-center gap-2 mb-1">
                     <span className="material-symbols-outlined text-orange-500 text-sm">auto_awesome</span>
                     <p className="text-[11px] font-black text-orange-600 uppercase tracking-widest">Hệ thống tự động</p>
                  </div>
                  <p className="text-xs text-slate-500 font-medium leading-relaxed italic">
                     * Thời gian kết thúc sẽ được tự động tính dựa trên thời lượng phim cộng thêm 15 phút dọn phòng. <br/>
                     * Hệ thống sẽ tự động áp dụng phụ thu cuối tuần khi tới ngày suất chiếu diễn ra.
                  </p>
               </div>

               <div className="flex gap-4 pt-4">
                  <button type="button" onClick={() => setModal(null)} className="flex-1 py-4 rounded-2xl border border-slate-200 text-slate-500 text-sm font-black hover:bg-slate-50 transition-all uppercase tracking-wider font-['Space_Grotesk']">Đóng</button>
                  <button type="submit" disabled={saving} className="flex-1 py-4 rounded-2xl bg-slate-900 text-white text-sm font-black hover:bg-black transition-all shadow-xl shadow-slate-200 active:scale-95 disabled:opacity-50 uppercase tracking-tighter">
                     {saving ? 'Đang lưu...' : modal.edit ? 'Cập nhật lịch' : 'Tạo lịch chiếu'}
                  </button>
               </div>
            </form>
          </Modal>
        )}
      </div>
    </div>
  );
}
