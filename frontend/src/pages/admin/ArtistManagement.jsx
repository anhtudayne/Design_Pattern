import { useState, useEffect, useCallback } from 'react';
import { getAuthHeaders, BASE_URL } from '../../utils/api';
import { uploadToCloudinary } from '../../utils/cloudinary';

const API = `${BASE_URL}/admin/metadata`;

// ── Helpers ───────────────────────────────────────────────────────────────────
const inputCls  = 'w-full px-3 py-2.5 rounded-lg border border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-orange-400 focus:ring-2 focus:ring-orange-100 transition-all';
const selectCls = inputCls + ' bg-white';

// ── Config per entity type ────────────────────────────────────────────────────
const TYPES = {
  castMembers: {
    label: 'Cast & Crew', labelSingle: 'nhân sự điện ảnh',
    icon: 'groups', color: 'blue',
    idField: 'id', endpoint: `${API}/cast-members`,
    fields: { fullName: true, bio: true, birthDate: false, nationality: false, imageUrl: false },
  },
  artists: {
    label: 'Nghệ sĩ âm nhạc', labelSingle: 'nghệ sĩ',
    icon: 'music_note', color: 'orange',
    idField: 'artistId', endpoint: `${API}/artists`,
    fields: { fullName: true, bio: true, birthDate: true, nationality: true, imageUrl: true },
  },
};

const NATIONALITIES = ['Việt Nam', 'Hàn Quốc', 'Mỹ', 'Anh', 'Pháp', 'Nhật Bản', 'Trung Quốc', 'Ấn Độ', 'Thái Lan', 'Úc', 'Canada', 'Khác'];

const BLANK_FORM = { fullName: '', bio: '', birthDate: '', nationality: '', imageUrl: '' };

// ── Components ────────────────────────────────────────────────────────────────
function Toast({ msg, type, onClose }) {
  useEffect(() => { const t = setTimeout(onClose, 3000); return () => clearTimeout(t); }, [onClose]);
  const colors = { success: 'bg-green-600', error: 'bg-red-600', info: 'bg-blue-600' };
  return (
    <div className={`fixed bottom-6 right-6 z-[100] flex items-center gap-3 px-5 py-3.5 rounded-xl text-white shadow-xl text-sm font-medium ${colors[type] || colors.info}`}>
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

function Avatar({ src, name, size = 'md' }) {
  const [err, setErr] = useState(false);
  const sizes = { sm: 'w-9 h-9 text-sm', md: 'w-12 h-12 text-base', lg: 'w-16 h-16 text-xl' };
  const initials = (name || '?').split(' ').slice(-2).map(w => w[0]).join('').toUpperCase();
  if (src && !err) {
    return <img src={src} alt={name} onError={() => setErr(true)} className={`${sizes[size]} rounded-xl object-cover flex-shrink-0 border border-slate-100`} />;
  }
  return (
    <div className={`${sizes[size]} rounded-xl bg-gradient-to-br from-orange-100 to-red-100 flex items-center justify-center flex-shrink-0 font-bold text-orange-500`}>
      {initials}
    </div>
  );
}

function FormField({ label, required, children }) {
  return (
    <div className="space-y-1.5">
      <label className="text-xs font-semibold text-slate-500 uppercase tracking-wide">
        {label}{required && <span className="text-red-500 ml-1">*</span>}
      </label>
      {children}
    </div>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100">
          <h3 className="font-bold text-slate-800 text-lg">{title}</h3>
          <button onClick={onClose} className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-slate-100 transition-colors">
            <span className="material-symbols-outlined text-slate-500">close</span>
          </button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}

// ── Image Upload Component ──────────────────────────────────────────────────
function ImageUpload({ folder = 'artists', value, onChange }) {
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState(value);

  // Sync preview when value changes externally (e.g. form reset or edit)
  useEffect(() => { setPreview(value); }, [value]);

  const handleUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Show local preview immediately
    const localUrl = URL.createObjectURL(file);
    setPreview(localUrl);
    setUploading(true);

    try {
      // Use the new utility for cleaner code
      const secureUrl = await uploadToCloudinary(file, folder);
      
      if (secureUrl) {
        onChange(secureUrl);
        setPreview(secureUrl);
      }
    } catch (err) {
      console.error('Upload failed:', err);
      // Fallback to previous value on failure
      setPreview(value);
      alert(err.message || 'Tải ảnh lên thất bại! Vui lòng thử lại.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-5 p-4 rounded-2xl border border-slate-100 bg-slate-50/50">
        <div className="w-20 h-20 rounded-2xl bg-white border border-slate-200 flex items-center justify-center overflow-hidden relative group shadow-sm">
          {preview ? (
            <>
              <img src={preview} alt="Preview" className="w-full h-full object-cover" />
              {uploading && (
                <div className="absolute inset-0 bg-black/40 flex items-center justify-center border-2 border-orange-400 rounded-2xl">
                  <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
                </div>
              )}
              <div className="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center pointer-events-none">
                 <span className="material-symbols-outlined text-white text-xl">photo_camera</span>
              </div>
            </>
          ) : (
            <div className="text-center">
              <span className="material-symbols-outlined text-slate-300 text-3xl">image</span>
            </div>
          )}
          <input 
            type="file" 
            accept="image/*" 
            onChange={handleUpload}
            className="absolute inset-0 opacity-0 cursor-pointer disabled:cursor-not-allowed"
            disabled={uploading}
          />
        </div>
        
        <div className="flex-1 space-y-1">
          <p className="text-sm font-bold text-slate-700">Ảnh đại diện</p>
          <p className="text-[11px] text-slate-400 font-medium">Bấm vào khung ảnh để tải tệp lên từ máy</p>
          <div className="flex gap-3 pt-1">
             <button 
               type="button" 
               className="relative overflow-hidden inline-flex items-center text-[11px] font-bold text-orange-500 hover:text-orange-600 transition-colors uppercase tracking-wider"
             >
               {uploading ? 'Đang tải lên...' : 'Chọn từ thiết bị'}
               <input 
                type="file" 
                accept="image/*" 
                onChange={handleUpload}
                className="absolute inset-0 opacity-0 cursor-pointer"
                disabled={uploading}
              />
             </button>
             {value && (
               <button 
                 type="button" 
                 onClick={() => { setPreview(''); onChange(''); }}
                 className="text-[11px] font-bold text-red-400 hover:text-red-500 transition-colors uppercase tracking-wider"
               >
                 Xóa ảnh
               </button>
             )}
          </div>
        </div>
      </div>
      
      <div className="relative group">
         <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg pointer-events-none">link</span>
         <input 
           className={inputCls + " pl-10 bg-white/50"} 
           value={value} 
           onChange={e => onChange(e.target.value)}
           placeholder="Hoặc dán URL ảnh trực tiếp vào đây..." 
         />
      </div>
    </div>
  );
}

// ── Person Form ───────────────────────────────────────────────────────────────
function PersonForm({ form, setForm, onSubmit, onClose, isEdit, typeCfg, loading }) {
  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="pt-2"></div>

      <FormField label="Họ và tên" required>
        <input className={inputCls} value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })}
          placeholder={`Tên ${typeCfg.labelSingle}...`} required />
      </FormField>

      {(typeCfg.fields?.birthDate || typeCfg.fields?.nationality) && (
        <div className="grid grid-cols-2 gap-3">
          {typeCfg.fields?.birthDate && (
            <FormField label="Ngày sinh">
              <input className={inputCls} type="date" value={form.birthDate}
                onChange={e => setForm({ ...form, birthDate: e.target.value })} />
            </FormField>
          )}
          {typeCfg.fields?.nationality && (
            <FormField label="Quốc tịch">
              <select className={selectCls} value={form.nationality} onChange={e => setForm({ ...form, nationality: e.target.value })}>
                <option value="">-- Chọn --</option>
                {NATIONALITIES.map(n => <option key={n} value={n}>{n}</option>)}
              </select>
            </FormField>
          )}
        </div>
      )}

      {typeCfg.fields?.imageUrl && (
        <FormField label="Ảnh đại diện">
          <ImageUpload
            folder={typeCfg.idField === 'artistId' ? 'artists' : 'cast-members'}
            value={form.imageUrl}
            onChange={val => setForm({ ...form, imageUrl: val })}
          />
        </FormField>
      )}

      <FormField label="Tiểu sử">
        <textarea className={inputCls + ' resize-none'} rows={4} value={form.bio}
          onChange={e => setForm({ ...form, bio: e.target.value })}
          placeholder="Một vài thông tin về nhân vật..." />
      </FormField>

      <div className="flex gap-3 pt-2">
        <button type="button" onClick={onClose}
          className="flex-1 py-2.5 rounded-xl border border-slate-200 text-slate-600 text-sm font-semibold hover:bg-slate-50 transition-colors">
          Hủy
        </button>
        <button type="submit" disabled={loading}
          className="flex-1 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 text-white text-sm font-bold transition-colors disabled:opacity-60">
          {loading ? 'Đang lưu...' : isEdit ? 'Cập nhật' : 'Thêm mới'}
        </button>
      </div>
    </form>
  );
}

// ── Person Tab (reused for actors / directors / artists) ──────────────────────
function PersonTab({ typeKey, notify }) {
  const cfg = TYPES[typeKey];
  const [list, setList]         = useState([]);
  const [loading, setLoading]   = useState(true);
  const [saving, setSaving]     = useState(false);
  const [modal, setModal]       = useState(null);
  const [form, setForm]         = useState(BLANK_FORM);
  const [search, setSearch]     = useState('');
  const [detail, setDetail]     = useState(null); // Detail panel

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const r = await fetch(cfg.endpoint, { headers: getAuthHeaders() });
      if (r.ok) setList(await r.json());
    } finally { setLoading(false); }
  }, [cfg.endpoint]);

  useEffect(() => { load(); }, [load]);

  const openAdd  = () => { setForm(BLANK_FORM); setModal('add'); };
  const openEdit = (item) => {
    setForm({
      fullName: item.fullName || '',
      bio: item.bio || '',
      birthDate: item.birthDate || '',
      nationality: item.nationality || '',
      imageUrl: item.imageUrl || '',
    });
    setModal({ edit: item });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    const isEdit = !!modal?.edit;
    const item = modal?.edit;
    const url = isEdit ? `${cfg.endpoint}/${item[cfg.idField]}` : cfg.endpoint;
    const method = isEdit ? 'PUT' : 'POST';
    try {
      const payload = cfg.fields?.birthDate ? form : { fullName: form.fullName, bio: form.bio };
      const r = await fetch(url, { method, headers: getAuthHeaders(), body: JSON.stringify(payload) });
      if (r.ok) {
        notify(isEdit ? 'Cập nhật thành công!' : 'Thêm mới thành công!', 'success');
        setModal(null); setDetail(null); load();
      } else {
        notify('Lỗi xảy ra khi lưu', 'error');
      }
    } finally { setSaving(false); }
  };

  const handleDelete = async (item) => {
    if (!window.confirm(`Xóa ${cfg.labelSingle} "${item.fullName}"?`)) return;
    const r = await fetch(`${cfg.endpoint}/${item[cfg.idField]}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (r.ok) { notify('Đã xóa!', 'success'); setDetail(null); load(); }
    else notify('Xóa thất bại', 'error');
  };

  const filtered = list.filter(p =>
    p.fullName?.toLowerCase().includes(search.toLowerCase()) ||
    p.nationality?.toLowerCase().includes(search.toLowerCase())
  );

  const colorMap = {
    blue:   { bg: 'bg-blue-50',   text: 'text-blue-600',   border: 'border-blue-100',   badge: 'bg-blue-100 text-blue-700'   },
    violet: { bg: 'bg-violet-50', text: 'text-violet-600', border: 'border-violet-100', badge: 'bg-violet-100 text-violet-700' },
    orange: { bg: 'bg-orange-50', text: 'text-orange-600', border: 'border-orange-100', badge: 'bg-orange-100 text-orange-700' },
  };
  const c = colorMap[cfg.color];

  return (
    <div className="flex gap-6">
      {/* Left: List */}
      <div className="flex-1 min-w-0">
        {/* Toolbar */}
        <div className="flex items-center gap-3 mb-5">
          <div className="flex-1 relative">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xl pointer-events-none">search</span>
            <input
              className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:border-orange-400 focus:ring-2 focus:ring-orange-100 transition-all"
              placeholder={`Tìm kiếm ${cfg.labelSingle}...`}
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>
          <button onClick={openAdd}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-orange-500 text-white text-sm font-bold hover:bg-orange-600 transition-colors shadow-md shadow-orange-100 whitespace-nowrap">
            <span className="material-symbols-outlined text-lg">add</span>
            Thêm {cfg.labelSingle}
          </button>
        </div>

        {loading ? (
          <div className="flex justify-center py-20"><div className="w-8 h-8 border-4 border-orange-200 border-t-orange-500 rounded-full animate-spin"/></div>
        ) : filtered.length === 0 ? (
          <div className="py-20 text-center text-slate-400">
            <span className="material-symbols-outlined text-5xl block mb-3">{cfg.icon}</span>
            <p className="font-medium">{search ? 'Không tìm thấy kết quả' : `Chưa có ${cfg.labelSingle} nào`}</p>
          </div>
        ) : (
          <div className="space-y-2">
            {filtered.map(person => {
              const isSelected = detail?.[cfg.idField] === person[cfg.idField];
              return (
                <div
                  key={person[cfg.idField]}
                  onClick={() => setDetail(isSelected ? null : person)}
                  className={`flex items-center gap-4 p-4 rounded-2xl border cursor-pointer transition-all ${
                    isSelected
                      ? `${c.border} ${c.bg} shadow-md`
                      : 'border-slate-100 bg-white hover:border-slate-200 hover:shadow-sm'
                  }`}
                >
                  <Avatar src={person.imageUrl} name={person.fullName} size="md" />

                  <div className="flex-1 min-w-0">
                    <p className={`font-bold text-slate-800 truncate ${isSelected ? c.text : ''}`}>{person.fullName}</p>
                    <div className="flex items-center gap-2 mt-0.5 flex-wrap">
                      {person.nationality && (
                        <span className="text-xs text-slate-400">{person.nationality}</span>
                      )}
                      {person.birthDate && (
                        <span className="text-xs text-slate-400">• {new Date(person.birthDate).getFullYear()}</span>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-1 flex-shrink-0">
                    <button
                      onClick={e => { e.stopPropagation(); openEdit(person); }}
                      className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-orange-50 hover:text-orange-500 text-slate-400 transition-all"
                      title="Sửa"
                    >
                      <span className="material-symbols-outlined text-lg">edit</span>
                    </button>
                    <button
                      onClick={e => { e.stopPropagation(); handleDelete(person); }}
                      className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-red-50 hover:text-red-500 text-slate-400 transition-all"
                      title="Xóa"
                    >
                      <span className="material-symbols-outlined text-lg">delete</span>
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        <p className="text-xs text-slate-400 text-right mt-3">
          {filtered.length} / {list.length} {cfg.labelSingle}
        </p>
      </div>

      {/* Right: Detail panel */}
      {detail && (
        <div className="w-72 flex-shrink-0">
          <div className={`rounded-2xl border ${c.border} ${c.bg} p-5 sticky top-4`}>
            {/* Avatar large */}
            <div className="flex flex-col items-center gap-3 mb-5 text-center">
              <Avatar src={detail.imageUrl} name={detail.fullName} size="lg" />
              <div>
                <p className={`font-black text-slate-800 text-lg leading-tight`}>{detail.fullName}</p>
                {detail.nationality && <p className="text-sm text-slate-400 mt-0.5">{detail.nationality}</p>}
              </div>
            </div>

            {/* Info rows */}
            <div className="space-y-3 border-t border-white/60 pt-4">
              {detail.birthDate && (
                <div className="flex items-center gap-2 text-sm">
                  <span className="material-symbols-outlined text-slate-400 text-lg">cake</span>
                  <span className="text-slate-600">{new Date(detail.birthDate).toLocaleDateString('vi-VN')}</span>
                </div>
              )}
              {detail.nationality && (
                <div className="flex items-center gap-2 text-sm">
                  <span className="material-symbols-outlined text-slate-400 text-lg">flag</span>
                  <span className="text-slate-600">{detail.nationality}</span>
                </div>
              )}
              {detail.bio && (
                <div className="mt-3 pt-3 border-t border-white/60">
                  <p className="text-xs font-bold text-slate-400 uppercase tracking-wide mb-1.5">Tiểu sử</p>
                  <p className="text-sm text-slate-600 leading-relaxed line-clamp-6">{detail.bio}</p>
                </div>
              )}
            </div>

            {/* Actions */}
            <div className="flex gap-2 mt-5 pt-4 border-t border-white/60">
              <button onClick={() => openEdit(detail)}
                className="flex-1 flex items-center justify-center gap-1 py-2 rounded-xl bg-white hover:bg-orange-50 text-slate-600 hover:text-orange-600 text-xs font-bold transition-all border border-slate-200">
                <span className="material-symbols-outlined text-sm">edit</span> Sửa
              </button>
              <button onClick={() => handleDelete(detail)}
                className="flex-1 flex items-center justify-center gap-1 py-2 rounded-xl bg-white hover:bg-red-50 text-slate-600 hover:text-red-500 text-xs font-bold transition-all border border-slate-200">
                <span className="material-symbols-outlined text-sm">delete</span> Xóa
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal */}
      {modal && (
        <Modal
          title={modal.edit ? `Chỉnh sửa ${cfg.labelSingle}` : `Thêm ${cfg.labelSingle} mới`}
          onClose={() => setModal(null)}
        >
          <PersonForm
            form={form} setForm={setForm}
            onSubmit={handleSubmit} onClose={() => setModal(null)}
            isEdit={!!modal.edit} typeCfg={cfg} loading={saving}
          />
        </Modal>
      )}
    </div>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────
const TABS = [
  { key: 'castMembers', label: 'Cast & Crew',      icon: 'groups',      color: 'blue'   },
  { key: 'artists',     label: 'Nghệ sĩ âm nhạc',  icon: 'music_note',  color: 'orange' },
];

export default function ArtistManagement() {
  const [activeTab, setActiveTab] = useState('castMembers');
  const [toast, setToast] = useState(null);

  const notify = (msg, type = 'info') => setToast({ msg, type });

  const tabColors = {
    blue:   'bg-blue-500 text-white shadow-lg shadow-blue-100',
    violet: 'bg-violet-500 text-white shadow-lg shadow-violet-100',
    orange: 'bg-orange-500 text-white shadow-lg shadow-orange-100',
  };
  const hoverColors = {
    blue:   'hover:text-blue-600 hover:bg-blue-50',
    violet: 'hover:text-violet-600 hover:bg-violet-50',
    orange: 'hover:text-orange-600 hover:bg-orange-50',
  };

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-8">
      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}

      {/* Header */}
      <div className="mb-8 flex items-center gap-4">
        <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-violet-400 to-pink-400 flex items-center justify-center shadow-lg shadow-violet-100">
          <span className="material-symbols-outlined text-white text-2xl">groups</span>
        </div>
        <div>
          <h1 className="text-2xl font-black text-slate-800">Quản lý Nghệ sĩ</h1>
          <p className="text-sm text-slate-400">Quản lý diễn viên, đạo diễn và nghệ sĩ âm nhạc cho các bộ phim</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-7 flex-wrap">
        {TABS.map(tab => {
          const isActive = activeTab === tab.key;
          return (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold transition-all duration-200 ${
                isActive
                  ? tabColors[tab.color]
                  : `text-slate-500 bg-white border border-slate-200 ${hoverColors[tab.color]}`
              }`}
            >
              <span className="material-symbols-outlined text-lg">{tab.icon}</span>
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* Content */}
      <PersonTab key={activeTab} typeKey={activeTab} notify={notify} />
    </div>
  );
}
