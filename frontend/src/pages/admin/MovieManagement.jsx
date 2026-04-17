import { useState, useEffect, useCallback } from 'react';
import { getAuthHeaders, BASE_URL } from '../../utils/api';
import { uploadToCloudinary } from '../../utils/cloudinary';
import { fetchCastMembers, fetchMovieDetail } from '../../services/movieService';

const API = `${BASE_URL}/movies`;

// ── Helpers ───────────────────────────────────────────────────────────────────
const inputCls = 'w-full px-4 py-3 rounded-2xl border border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-100 transition-all font-medium';
const selectCls = inputCls + ' bg-white';

const AGE_RATINGS = [
  { value: 'P', label: 'P - Mọi lứa tuổi', color: 'green' },
  { value: 'K', label: 'K - Dưới 13 (với PH)', color: 'blue' },
  { value: 'C13', label: 'C13 - Trên 13 tuổi', color: 'orange' },
  { value: 'C16', label: 'C16 - Trên 16 tuổi', color: 'red' },
  { value: 'C18', label: 'C18 - Trên 18 tuổi', color: 'slate' }
];

const MOVIE_STATUS = [
  { value: 'NOW_SHOWING', label: 'Đang chiếu', color: 'green' },
  { value: 'COMING_SOON', label: 'Sắp ra mắt', color: 'blue' },
  { value: 'STOPPED', label: 'Ngừng chiếu', color: 'slate' }
];

const MOJIBAKE_PATTERN = /(Ã.|Â.|Ä.|á»|áº|Æ|Ð|Ñ)/;

const normalizeVietnameseText = (value) => {
  if (typeof value !== 'string' || !value) return value;
  if (!MOJIBAKE_PATTERN.test(value)) return value;
  try {
    const bytes = Uint8Array.from(value, (char) => char.charCodeAt(0) & 0xff);
    const decoded = new TextDecoder('utf-8', { fatal: true }).decode(bytes);
    return decoded || value;
  } catch {
    return value;
  }
};

const normalizeGenrePayload = (genres) =>
  (genres || []).map((g) => ({ ...g, name: normalizeVietnameseText(g.name) }));

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
      <label className="text-[11px] font-black text-slate-400 uppercase tracking-widest ml-1 flex items-center gap-1.5 leading-none">
        {icon && <span className="material-symbols-outlined text-sm">{icon}</span>}
        {label}{required && <span className="text-red-500 ml-1">*</span>}
      </label>
      {children}
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-md p-4 animate-in fade-in duration-300">
      <style>{`
        .custom-scrollbar::-webkit-scrollbar { display: none; }
        .custom-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
      `}</style>
      <div className="bg-white rounded-[40px] shadow-2xl w-full max-w-2xl max-h-[92vh] overflow-y-auto custom-scrollbar border border-slate-100">
        <div className="flex items-center justify-between px-10 py-8 border-b border-slate-50 sticky top-0 bg-white z-10">
          <div className="flex items-center gap-4">
            <div className="w-3 h-10 bg-orange-500 rounded-full" />
            <h3 className="font-black text-slate-800 text-2xl uppercase tracking-tighter">{title}</h3>
          </div>
          <button onClick={onClose} className="w-12 h-12 flex items-center justify-center rounded-2xl hover:bg-slate-100 transition-colors text-slate-400 bg-slate-50 shadow-inner">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>
        <div className="p-10">{children}</div>
      </div>
    </div>
  );
}

function ImageUpload({ folder = 'movies', value, onChange }) {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(value);

  useEffect(() => { setPreview(value); }, [value]);

  const handleUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setPreview(URL.createObjectURL(file));
    setUploading(true);
    try {
      const secureUrl = await uploadToCloudinary(file, folder);
      if (secureUrl) {
        onChange(secureUrl);
        setPreview(secureUrl);
      }
    } catch (err) {
      console.error(err);
      setPreview(value);
      alert(err.message || 'Tải ảnh lên thất bại!');
    } finally { setUploading(false); }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-7 p-6 rounded-[32px] border-2 border-dashed border-slate-100 bg-slate-50/30">
        <div className="w-28 h-40 rounded-2xl bg-white border border-slate-200 flex items-center justify-center overflow-hidden relative group shadow-lg transition-all hover:border-orange-300">
          {preview ? (
            <>
              <img src={preview} alt="Preview" className="w-full h-full object-cover" />
              {uploading && (
                <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                  <div className="w-8 h-8 border-4 border-white/20 border-t-white rounded-full animate-spin" />
                </div>
              )}
              <div className="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center pointer-events-none">
                <span className="material-symbols-outlined text-white text-3xl">photo_camera</span>
              </div>
            </>
          ) : (
            <span className="material-symbols-outlined text-slate-200 text-5xl">image</span>
          )}
          <input type="file" accept="image/*" onChange={handleUpload} disabled={uploading} className="absolute inset-0 opacity-0 cursor-pointer disabled:cursor-not-allowed" />
        </div>
        <div className="flex-1 space-y-2">
          <p className="text-lg font-black text-slate-800 tracking-tight">Poster phim</p>
          <p className="text-xs text-slate-400 font-medium leading-relaxed">Tải lên hình ảnh poster chất lượng cao. Khuyên dùng tỉ lệ 2:3 (VD: 600x900px).</p>
          <div className="flex gap-4 pt-2">
            <button type="button" className="relative h-10 px-5 rounded-xl inline-flex items-center text-xs font-black bg-slate-900 text-white hover:bg-black transition-all uppercase tracking-widest shadow-xl shadow-slate-100">
              {uploading ? 'Đang tải...' : 'Chọn từ máy'}
              <input type="file" accept="image/*" onChange={handleUpload} disabled={uploading} className="absolute inset-0 opacity-0 cursor-pointer" />
            </button>
            {value && (
              <button type="button" onClick={() => { setPreview(''); onChange(''); }} className="text-xs font-black text-red-400 hover:text-red-500 transition-colors uppercase tracking-widest">Xóa ảnh</button>
            )}
          </div>
        </div>
      </div>
      <div className="relative group">
        <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-orange-500 transition-colors">link</span>
        <input className={inputCls + " pl-12 bg-white/50 border-slate-100"} value={value} onChange={e => onChange(e.target.value)} placeholder="Hoặc dán URL ảnh trực tiếp..." />
      </div>
    </div>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function MovieManagement() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modal, setModal] = useState(null);
  const [genreModal, setGenreModal] = useState(false);
  const [toast, setToast] = useState(null);
  const [search, setSearch] = useState('');
  const [allGenres, setAllGenres] = useState([]);
  const [castMembers, setCastMembers] = useState([]);

  const [form, setForm] = useState({
    title: '', description: '', durationMinutes: 120, releaseDate: '', language: 'Tiếng Việt',
    ageRating: 'P', posterUrl: '', trailerUrl: '', status: 'COMING_SOON', genreIds: [],
    casts: [], // [{ castMemberId, roleType, roleName }]
  });

  const notify = (msg, type = 'info') => setToast({ msg, type });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [rMovies, rGenres] = await Promise.all([
        fetch(API, { headers: getAuthHeaders() }),
        fetch(`${BASE_URL}/admin/metadata/genres`, { headers: getAuthHeaders() })
      ]);
      if (rMovies.ok) {
        setList(await rMovies.json());
      } else {
        setList([]);
        notify('Khong tai duoc danh sach phim. Hay dang nhap lai tai khoan ADMIN/STAFF.', 'error');
      }
      if (rGenres.ok) {
        const genres = await rGenres.json();
        setAllGenres(normalizeGenrePayload(genres));
      }
      try {
        const cms = await fetchCastMembers(getAuthHeaders());
        setCastMembers(cms);
      } catch {
        setCastMembers([]);
      }
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const openAdd = () => {
    setForm({ 
      title: '', description: '', durationMinutes: 120, releaseDate: '', 
      language: 'Tiếng Việt', ageRating: 'P', posterUrl: '', 
      trailerUrl: '', status: 'COMING_SOON', genreIds: [],
      casts: []
    });
    setModal('add');
  };

  const openEdit = async (movie) => {
    // Luôn lấy dữ liệu thể loại mới nhất từ API chuyên biệt cho phim này
    const r = await fetch(`${BASE_URL}/movie-genres/${movie.movieId}`, { headers: getAuthHeaders() });
    const genres = r.ok ? await r.json() : [];

    let detail = movie;
    try {
      detail = await fetchMovieDetail(movie.movieId, getAuthHeaders());
    } catch { /* ignore */ }

    setForm({ 
      ...detail,
      genreIds: genres.map(g => g.genreId) || [],
      casts: (detail.casts || []).map(c => ({
        castMemberId: c.castMemberId,
        roleType: c.roleType,
        roleName: c.roleName || ''
      }))
    });
    setModal({ edit: movie });
  };

  const addCastRow = () => {
    setForm(prev => ({
      ...prev,
      casts: [...(prev.casts || []), { castMemberId: '', roleType: 'ACTOR', roleName: '' }]
    }));
  };

  const updateCastRow = (idx, patch) => {
    setForm(prev => ({
      ...prev,
      casts: (prev.casts || []).map((c, i) => i === idx ? { ...c, ...patch } : c)
    }));
  };

  const removeCastRow = (idx) => {
    setForm(prev => ({
      ...prev,
      casts: (prev.casts || []).filter((_, i) => i !== idx)
    }));
  };

  const toggleGenre = (genreId) => {
    setForm(prev => {
      const exists = prev.genreIds.includes(genreId);
      if (exists) return { ...prev, genreIds: prev.genreIds.filter(id => id !== genreId) };
      return { ...prev, genreIds: [...prev.genreIds, genreId] };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    const isEdit = !!modal?.edit;
    const url = isEdit ? `${API}/${modal.edit.movieId}` : API;
    const method = isEdit ? 'PUT' : 'POST';

    try {
      const r = await fetch(url, {
        method, headers: getAuthHeaders(),
        body: JSON.stringify({
          ...form,
          durationMinutes: Number(form.durationMinutes),
          casts: (form.casts || [])
            .filter(c => c.castMemberId && c.roleType)
            .map(c => ({
              castMemberId: Number(c.castMemberId),
              roleType: c.roleType,
              roleName: c.roleName || null,
            })),
        })
      });
      if (r.ok) {
        const savedMovie = await r.json();
        const mid = savedMovie.movieId;

        // Đồng bộ thể loại qua API bảng nối
        await fetch(`${BASE_URL}/movie-genres/${mid}`, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: JSON.stringify(form.genreIds)
        });

        notify(isEdit ? 'Cập nhật thành công!' : 'Thêm phim mới thành công!', 'success');
        setModal(null); load();
      } else {
        const err = await r.json();
        notify(err.message || 'Lỗi xảy ra', 'error');
      }
    } finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa phim này?")) return;
    const r = await fetch(`${API}/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (r.ok) { notify('Đã xóa phim!', 'success'); load(); }
  };

  const filtered = list.filter(m => m.title.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="min-h-screen bg-[#F8FAFC] p-6 md:p-10 antialiased">
      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}

      <div className="max-w-7xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-8 mb-12">
          <div className="flex items-center gap-7">
            <div className="w-16 h-16 rounded-[28px] bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-2xl shadow-indigo-100 rotate-2">
              <span className="material-symbols-outlined text-white text-3xl">movie_edit</span>
            </div>
            <div>
              <h1 className="text-4xl font-black text-slate-800 tracking-tighter uppercase leading-none mb-1">Kho phim</h1>
              <p className="text-sm text-slate-400 font-bold uppercase tracking-[2px]">Tải phim mới, cập nhật trailer và quản lý tình trạng rạp</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="relative group">
              <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-indigo-500 transition-colors">search</span>
              <input
                className="w-full md:w-80 pl-12 pr-6 py-3.5 rounded-[22px] border border-slate-100 bg-white/70 backdrop-blur-lg focus:outline-none focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 transition-all font-bold text-sm shadow-sm"
                placeholder="Tìm kiếm phim của bạn..."
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
            
            <button onClick={() => setGenreModal(true)} className="flex items-center justify-center w-12 h-12 rounded-[22px] bg-white text-slate-400 hover:text-indigo-500 hover:bg-slate-50 transition-all shadow-sm active:scale-95 border border-slate-100">
               <span className="material-symbols-outlined">category</span>
            </button>

            <button onClick={openAdd} className="flex items-center gap-2.5 h-12 px-7 rounded-[22px] bg-slate-900 text-white text-sm font-black hover:bg-black transition-all shadow-2xl shadow-slate-200 active:scale-95 uppercase tracking-wider">
              <span className="material-symbols-outlined text-lg text-indigo-400">add_circle</span>
              Thêm phim
            </button>
          </div>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-40 gap-4">
            <div className="w-14 h-14 border-4 border-slate-100 border-t-indigo-500 rounded-full animate-spin" />
            <p className="text-[11px] font-black text-slate-300 uppercase tracking-widest">Đang tải phim từ dữ liệu gốc...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="py-32 text-center bg-white rounded-[48px] border-4 border-dashed border-slate-50 shadow-inner">
            <span className="material-symbols-outlined text-8xl text-slate-100 block mb-6">theaters</span>
            <p className="text-xl font-black text-slate-600">Bạn chưa có phim nào trong rạp!</p>
            <p className="text-sm text-slate-400 mt-2 font-medium">Bấm "Thêm phim" để bắt đầu phát triển kho phim của bạn.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filtered.map(movie => {
              const status = MOVIE_STATUS.find(s => s.value === movie.status);
              const rating = AGE_RATINGS.find(r => r.value === movie.ageRating);
              return (
                <div key={movie.movieId} className="group flex flex-col bg-white rounded-[40px] p-2.5 border border-slate-100 hover:border-indigo-100 hover:shadow-[0_32px_64px_-16px_rgba(79,70,229,0.15)] transition-all duration-500">
                  <div className="aspect-[2/3] rounded-[32px] overflow-hidden relative shadow-sm">
                    {movie.posterUrl ? (
                      <img src={movie.posterUrl} alt={movie.title} className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
                    ) : (
                      <div className="w-full h-full bg-slate-50 flex items-center justify-center text-slate-200">
                        <span className="material-symbols-outlined text-6xl">movie</span>
                      </div>
                    )}

                        {/* Overlay Badges */}
                        <div className="absolute top-4 left-4 flex flex-col gap-2">
                           <span className={`px-3 py-1 rounded-xl text-[10px] font-black text-white uppercase tracking-widest shadow-lg ${
                              rating?.color === 'green' ? 'bg-green-500' : 
                              rating?.color === 'blue' ? 'bg-blue-500' : 
                              rating?.color === 'orange' ? 'bg-orange-500' : 
                              rating?.color === 'red' ? 'bg-red-500' : 'bg-slate-500'
                           }`}>
                              {movie.ageRating}
                           </span>
                        </div>

                        <div className="absolute top-4 right-4 flex flex-wrap justify-end gap-1.5 max-w-[70%]">
                           {movie.genres?.map(g => (
                             <span key={g.genreId} className="px-2 py-0.5 bg-black/40 backdrop-blur-md border border-white/20 rounded-lg text-[9px] font-black text-white uppercase tracking-tighter">
                                {g.name}
                             </span>
                           ))}
                        </div>

                    <div className="absolute inset-x-4 bottom-4 flex justify-between items-end">
                      <div className="px-3 py-1.5 rounded-2xl bg-white/95 backdrop-blur-md shadow-xl">
                        <p className="text-[10px] font-black text-slate-800 uppercase leading-none mb-0.5">{movie.durationMinutes} Phút</p>
                        <p className="text-[9px] font-bold text-slate-400 uppercase tracking-tighter">{movie.language}</p>
                      </div>
                    </div>

                    {/* Actions Overlay */}
                    <div className="absolute inset-0 bg-indigo-900/60 backdrop-blur-sm opacity-0 group-hover:opacity-100 transition-all duration-300 flex items-center justify-center gap-3">
                      <button onClick={() => openEdit(movie)} className="w-12 h-12 rounded-2xl bg-white text-indigo-600 hover:bg-indigo-600 hover:text-white transition-all shadow-xl active:scale-90 flex items-center justify-center">
                        <span className="material-symbols-outlined">edit</span>
                      </button>
                      <button onClick={() => handleDelete(movie.movieId)} className="w-12 h-12 rounded-2xl bg-white text-red-500 hover:bg-red-500 hover:text-white transition-all shadow-xl active:scale-90 flex items-center justify-center">
                        <span className="material-symbols-outlined">delete</span>
                      </button>
                    </div>
                  </div>

                  <div className="p-5 flex-1 flex flex-col">
                    <div className="flex-1">
                      <h3 className="text-lg font-black text-slate-800 tracking-tight leading-tight line-clamp-1 group-hover:text-indigo-600 transition-colors uppercase mb-1">{movie.title}</h3>
                      <p className="text-xs text-slate-400 font-bold uppercase tracking-widest mb-3">Khởi chiếu: {movie.releaseDate ? new Date(movie.releaseDate).toLocaleDateString('vi-VN') : 'Sớm'}</p>
                      <p className="text-[11px] text-slate-500 leading-relaxed font-medium line-clamp-3 mb-4">{movie.description || 'Chưa có tóm tắt phim.'}</p>
                    </div>
                    <div className="pt-4 border-t border-slate-50 flex items-center justify-between">
                      <div className="flex items-center gap-1.5 text-xs font-black uppercase tracking-tighter">
                        <span className={`w-2 h-2 rounded-full ${status?.color === 'green' ? 'bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.5)] animate-pulse' :
                            status?.color === 'blue' ? 'bg-blue-500 shadow-[0_0_8px_rgba(59,130,246,0.5)]' :
                              'bg-slate-400'
                          }`} />
                        <span className={
                          status?.color === 'green' ? 'text-green-600' :
                            status?.color === 'blue' ? 'text-blue-600' : 'text-slate-400'
                        }>{status?.label}</span>
                      </div>
                      {movie.trailerUrl && (
                        <a href={movie.trailerUrl} target="_blank" rel="noreferrer" className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 transition-all">
                          <span className="material-symbols-outlined text-base">smart_display</span>
                        </a>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {modal && (
          <Modal title={modal.edit ? 'Cập nhật phim' : 'Thêm phim mới'} onClose={() => setModal(null)}>
            <form onSubmit={handleSubmit} className="space-y-8">
              <ImageUpload folder="movies" value={form.posterUrl} onChange={url => setForm({ ...form, posterUrl: url })} />

              <FormField label="Tên phim" required icon="article">
                <input className={inputCls} value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="VD: Avengers: Endgame..." required />
              </FormField>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <FormField label="Thời lượng (Phút)" required icon="timer">
                  <input className={inputCls} type="number" min="1" value={form.durationMinutes} onChange={e => setForm({ ...form, durationMinutes: e.target.value })} required />
                </FormField>
                <FormField label="Ngôn ngữ" required icon="language">
                  <input className={inputCls} value={form.language} onChange={e => setForm({ ...form, language: e.target.value })} placeholder="VD: Tiếng Anh, Phụ đề Tiếng Việt..." required />
                </FormField>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <FormField label="Phân loại độ tuổi" required icon="safety_check">
                  <select className={selectCls} value={form.ageRating} onChange={e => setForm({ ...form, ageRating: e.target.value })} required>
                    {AGE_RATINGS.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
                  </select>
                </FormField>
                <FormField label="Trạng thái" required icon="info">
                  <select className={selectCls} value={form.status} onChange={e => setForm({ ...form, status: e.target.value })} required>
                    {MOVIE_STATUS.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
                  </select>
                </FormField>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <FormField label="Ngày khởi chiếu" required icon="calendar_month">
                  <input className={inputCls} type="date" value={form.releaseDate} onChange={e => setForm({ ...form, releaseDate: e.target.value })} required />
                </FormField>
                <FormField label="Trailer URL (Youtube)" icon="videocam">
                  <input className={inputCls} value={form.trailerUrl} onChange={e => setForm({ ...form, trailerUrl: e.target.value })} placeholder="https://youtube.com/..." />
                </FormField>
              </div>

              <FormField label="Mô tả phim" icon="notes">
                <textarea className={inputCls + " h-32 resize-none leading-relaxed"} value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Nội dung chính hoặc tóm tắt bộ phim..." />
              </FormField>

              <FormField label="Thể loại phim" icon="category">
                  <div className="flex flex-wrap gap-2 p-4 rounded-[24px] bg-slate-50 border border-slate-100">
                     {allGenres.map(g => {
                       const isSelected = form.genreIds.includes(g.genreId);
                       return (
                         <button
                           key={g.genreId}
                           type="button"
                           onClick={() => toggleGenre(g.genreId)}
                           className={`px-4 py-2 rounded-xl text-[11px] font-black uppercase tracking-wider transition-all border-2 
                             ${isSelected 
                               ? 'bg-gradient-to-r from-indigo-500 to-blue-600 border-indigo-500 text-white shadow-lg shadow-indigo-100 scale-105' 
                               : 'bg-white border-slate-100 text-slate-400 hover:border-slate-300 hover:text-slate-600 hover:bg-slate-50'}
                           `}
                         >
                            {g.name}
                         </button>
                       );
                     })}
                     {allGenres.length === 0 && <p className="text-[10px] text-slate-400 italic">Chưa có thể loại nào, hãy tạo thêm.</p>}
                  </div>
              </FormField>

              <FormField label="Cast & Crew" icon="groups">
                <div className="space-y-3">
                  {(form.casts || []).map((c, idx) => (
                    <div key={idx} className="grid grid-cols-1 md:grid-cols-12 gap-3 items-end bg-slate-50 border border-slate-100 rounded-[24px] p-4">
                      <div className="md:col-span-6">
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1 mb-1 block">CastMember</label>
                        <select className={selectCls} value={c.castMemberId} onChange={e => updateCastRow(idx, { castMemberId: e.target.value })}>
                          <option value="">-- Chọn --</option>
                          {castMembers.map(cm => (
                            <option key={cm.id} value={cm.id}>{cm.fullName}</option>
                          ))}
                        </select>
                      </div>
                      <div className="md:col-span-3">
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1 mb-1 block">Role type</label>
                        <select className={selectCls} value={c.roleType} onChange={e => updateCastRow(idx, { roleType: e.target.value })}>
                          <option value="ACTOR">ACTOR</option>
                          <option value="DIRECTOR">DIRECTOR</option>
                        </select>
                      </div>
                      <div className="md:col-span-2">
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1 mb-1 block">Role name</label>
                        <input className={inputCls} value={c.roleName || ''} onChange={e => updateCastRow(idx, { roleName: e.target.value })} placeholder="VD: Paul Atreides" />
                      </div>
                      <div className="md:col-span-1 flex justify-end">
                        <button type="button" onClick={() => removeCastRow(idx)} className="w-12 h-12 rounded-2xl bg-white border border-slate-200 text-red-400 hover:text-red-600 hover:bg-red-50 transition-all flex items-center justify-center shadow-sm">
                          <span className="material-symbols-outlined">delete</span>
                        </button>
                      </div>
                    </div>
                  ))}

                  <button type="button" onClick={addCastRow} className="w-full py-3 rounded-[22px] bg-slate-900 text-white text-xs font-black uppercase tracking-widest hover:bg-black transition-all flex items-center justify-center gap-2">
                    <span className="material-symbols-outlined text-lg text-indigo-400">add</span>
                    Thêm Cast/Crew
                  </button>

                  {castMembers.length === 0 && (
                    <p className="text-[11px] text-slate-400 font-bold italic">
                      Chưa có CastMember. Hãy tạo ở `Admin → Metadata → Cast members`.
                    </p>
                  )}
                </div>
              </FormField>

              <div className="flex gap-4 pt-6 border-t border-slate-50">
                <button type="button" onClick={() => setModal(null)} className="flex-1 py-4 rounded-[22px] border border-slate-200 text-slate-500 text-sm font-black hover:bg-slate-50 transition-all uppercase tracking-widest">Đóng</button>
                <button type="submit" disabled={saving} className="flex-1 py-4 rounded-[22px] bg-slate-900 text-white text-sm font-black hover:bg-black transition-all shadow-2xl shadow-indigo-100 active:scale-95 disabled:opacity-50 uppercase tracking-tighter">
                  {saving ? 'Đang lưu...' : modal.edit ? 'Cập nhật' : 'Đưa vào kho'}
                </button>
              </div>
            </form>
          </Modal>
        )}

        {genreModal && (
          <Modal title="Quản lý thể loại" onClose={() => setGenreModal(false)}>
            <div className="space-y-6">
               <div className="flex gap-4">
                  <input id="new-genre-input" className={inputCls} placeholder="Nhập tên thể loại mới..." />
                  <button 
                    onClick={async () => {
                      const name = document.getElementById('new-genre-input').value;
                      if (!name) return;
                      const r = await fetch(`${BASE_URL}/admin/metadata/genres`, { 
                        method: 'POST', headers: getAuthHeaders(), body: JSON.stringify({ name }) 
                      });
                      if (r.ok) { load(); document.getElementById('new-genre-input').value = ''; }
                    }}
                    className="px-6 rounded-2xl bg-indigo-600 text-white font-black text-xs uppercase tracking-widest hover:bg-indigo-700 transition"
                  >Thêm</button>
               </div>
               
               <div className="grid grid-cols-1 gap-2 max-h-80 overflow-y-auto custom-scrollbar pr-2">
                  {allGenres.map(g => (
                    <div key={g.genreId} className="flex items-center justify-between p-4 bg-slate-50 rounded-2xl border border-slate-100 group">
                       <span className="font-bold text-slate-700">{g.name}</span>
                       <button 
                         onClick={async () => {
                           if (!window.confirm("Xóa thể loại này?")) return;
                           const r = await fetch(`${BASE_URL}/admin/metadata/genres/${g.genreId}`, { 
                             method: 'DELETE', headers: getAuthHeaders() 
                           });
                           if (r.ok) load();
                         }}
                         className="w-10 h-10 rounded-xl bg-white text-red-400 opacity-0 group-hover:opacity-100 hover:text-red-600 hover:bg-red-50 transition-all flex items-center justify-center shadow-sm"
                       >
                          <span className="material-symbols-outlined text-xl">delete</span>
                       </button>
                    </div>
                  ))}
               </div>
            </div>
          </Modal>
        )}
      </div>
    </div>
  );
}
