import { useState, useEffect, useCallback } from 'react';
import { getAuthHeaders, BASE_URL } from '../../utils/api';
import { uploadToCloudinary } from '../../utils/cloudinary';

const API = `${BASE_URL}/fnb`;

// ── Helpers ───────────────────────────────────────────────────────────────────
const inputCls  = 'w-full px-3 py-2.5 rounded-lg border border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-orange-400 focus:ring-2 focus:ring-orange-100 transition-all';
const selectCls = inputCls + ' bg-white';

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
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto font-['Space_Grotesk']">
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

// ── Image Upload Component (Reused) ──────────────────────────────────────────
function ImageUpload({ folder = 'fnb', value, onChange }) {
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
      console.error('Upload failed:', err);
      setPreview(value);
      alert(err.message || 'Tải ảnh lên thất bại!');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-5 p-4 rounded-2xl border border-slate-100 bg-slate-50/50">
        <div className="w-20 h-20 rounded-2xl bg-white border border-slate-200 flex items-center justify-center overflow-hidden relative group shadow-sm transition-all hover:border-orange-200">
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
          <input type="file" accept="image/*" onChange={handleUpload} disabled={uploading} className="absolute inset-0 opacity-0 cursor-pointer disabled:cursor-not-allowed" />
        </div>
        <div className="flex-1 space-y-1">
          <p className="text-sm font-bold text-slate-700">Ảnh sản phẩm</p>
          <p className="text-[11px] text-slate-400 font-medium">Bấm vào khung ảnh để tải tệp lên</p>
          <div className="flex gap-3 pt-1">
             <button type="button" className="relative overflow-hidden inline-flex items-center text-[11px] font-bold text-orange-500 hover:text-orange-600 transition-colors uppercase tracking-wider">
               {uploading ? 'Đang tải lên...' : 'Chọn từ thiết bị'}
               <input type="file" accept="image/*" onChange={handleUpload} disabled={uploading} className="absolute inset-0 opacity-0 cursor-pointer" />
             </button>
             {value && (
               <button type="button" onClick={() => { setPreview(''); onChange(''); }} className="text-[11px] font-bold text-red-400 hover:text-red-500 transition-colors uppercase tracking-wider">Xóa ảnh</button>
             )}
          </div>
        </div>
      </div>
      <div className="relative group">
         <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg pointer-events-none">link</span>
         <input className={inputCls + " pl-10 bg-white/50"} value={value} onChange={e => onChange(e.target.value)} placeholder="Hoặc dán URL ảnh trực tiếp..." />
      </div>
    </div>
  );
}

// ── Item Tab ──────────────────────────────────────────────────────────────────
function ItemTab({ notify }) {
  const [items, setItems]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [saving, setSaving]     = useState(false);
  const [modal, setModal]       = useState(null);
  const [form, setForm]         = useState({ name: '', description: '', price: 0, stockQuantity: 0, imageUrl: '', isActive: true });
  const [search, setSearch]     = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const r = await fetch(`${API}/items`, { headers: getAuthHeaders() });
      if (r.ok) setItems(await r.json());
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const openAdd = () => {
    setForm({
      name: '',
      description: '',
      price: 0,
      stockQuantity: 0,
      imageUrl: '',
      isActive: true,
    });
    setModal('add');
  };

  const openEdit = (item) => {
    setForm({
      name: item.name,
      description: item.description || '',
      price: item.price,
      stockQuantity: Number(item.stockQuantity || 0),
      imageUrl: item.imageUrl || '',
      isActive: item.isActive
    });
    setModal({ edit: item });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    const isEdit = !!modal?.edit;
    const url = isEdit ? `${API}/items/${modal.edit.itemId}` : `${API}/items`;
    try {
      const r = await fetch(url, {
        method: isEdit ? 'PUT' : 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({
          ...form,
          price: Number(form.price),
          stockQuantity: Number(form.stockQuantity),
        })
      });
      if (r.ok) {
        notify(isEdit ? 'Cập nhật thành công!' : 'Thêm mới thành công!', 'success');
        setModal(null);
        load();
      } else {
        const errorData = await r.json().catch(() => ({}));
        notify(errorData.message || 'Có lỗi xảy ra khi lưu', 'error');
      }
    } finally { setSaving(false); }
  };

  const handleDelete = async (item) => {
    if (!window.confirm(`Xóa sản phẩm "${item.name}"?`)) return;
    const r = await fetch(`${API}/items/${item.itemId}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (r.ok) { notify('Đã xóa!', 'success'); load(); }
    else notify('Xóa thất bại', 'error');
  };

  const filtered = items.filter(i => {
    const q = search.toLowerCase();
    return i.name.toLowerCase().includes(q);
  });

  return (
    <div className="space-y-5">
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
        <div className="flex-1 relative group">
          <span className="material-symbols-outlined absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 transition-colors group-focus-within:text-orange-500">search</span>
          <input className="w-full pl-11 pr-4 py-2.5 rounded-2xl border border-slate-200 bg-white text-sm focus:outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-100 transition-all font-medium" placeholder="Tìm theo tên..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <button onClick={openAdd} className="flex items-center gap-2 px-5 py-2.5 rounded-2xl bg-orange-500 text-white text-sm font-bold hover:bg-orange-600 transition-all shadow-lg shadow-orange-100 whitespace-nowrap active:scale-95">
          <span className="material-symbols-outlined text-lg">add</span>
          Thêm món mới
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-20"><div className="w-8 h-8 border-4 border-orange-200 border-t-orange-500 rounded-full animate-spin" /></div>
      ) : filtered.length === 0 ? (
        <div className="py-20 text-center text-slate-400 bg-white rounded-3xl border border-dashed border-slate-200">
          <span className="material-symbols-outlined text-5xl block mb-3 opacity-20">fastfood</span>
          <p className="font-bold text-slate-500">{search ? 'Không tìm thấy kết quả nào' : 'Bạn chưa có món ăn nào trong danh sách'}</p>
          <p className="text-xs mt-1">Bấm "Thêm món mới" để bắt đầu xây dựng menu của bạn.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filtered.map(item => (
            <div key={item.itemId} className="group bg-white rounded-3xl border border-slate-100 p-3 hover:border-orange-200 hover:shadow-xl hover:shadow-orange-100/30 transition-all cursor-default">
              <div className="aspect-[4/3] rounded-2xl bg-slate-50 overflow-hidden relative mb-3">
                {item.imageUrl ? (
                  <img src={item.imageUrl} alt={item.name} className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-slate-200">
                    <span className="material-symbols-outlined text-4xl">image</span>
                  </div>
                )}
                {!item.isActive && (
                  <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-[2px] flex items-center justify-center">
                    <span className="px-3 py-1 bg-white/20 border border-white/30 rounded-full text-[10px] font-black text-white uppercase tracking-widest">Ngừng kinh doanh</span>
                  </div>
                )}
                <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col gap-1.5 translate-x-2 group-hover:translate-x-0 transition-transform">
                  <button onClick={() => openEdit(item)} className="w-8 h-8 rounded-xl bg-white shadow-lg flex items-center justify-center text-orange-500 hover:bg-orange-500 hover:text-white transition-all"><span className="material-symbols-outlined text-sm">edit</span></button>
                  <button onClick={() => handleDelete(item)} className="w-8 h-8 rounded-xl bg-white shadow-lg flex items-center justify-center text-red-500 hover:bg-red-500 hover:text-white transition-all"><span className="material-symbols-outlined text-sm">delete</span></button>
                </div>
              </div>
              <div className="px-1">
                <div className="flex justify-between items-start gap-2 mb-1">
                  <h4 className="font-bold text-slate-800 line-clamp-1 group-hover:text-orange-600 transition-colors uppercase tracking-tight text-xs">{item.name}</h4>
                  <span className="flex-shrink-0 px-2 py-0.5 rounded-lg bg-slate-100 text-[10px] font-bold text-slate-500 uppercase tracking-tighter">F&B</span>
                </div>
                <p className="text-[11px] text-slate-400 line-clamp-2 min-h-[32px] font-medium leading-relaxed">{item.description || 'Chưa có mô tả cho sản phẩm này.'}</p>
                <div className="mt-3 pt-3 border-t border-slate-50 flex items-center justify-between">
                   <p className="font-black text-orange-500">{item.price.toLocaleString('vi-VN')}đ</p>
                   <div className="flex flex-col items-end gap-1">
                     <span className={`text-[10px] font-black uppercase tracking-widest ${(item.stockQuantity || 0) > 0 ? 'text-emerald-600' : 'text-red-500'}`}>
                       {(item.stockQuantity || 0) > 0 ? `Còn ${item.stockQuantity}` : 'Hết hàng'}
                     </span>
                     {item.isActive && <div className="flex items-center gap-1.5"><div className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse" /><span className="text-[10px] font-bold text-green-600 uppercase tracking-widest">Sẵn có</span></div>}
                   </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {modal && (
        <Modal title={modal.edit ? 'Sửa món ăn' : 'Thêm món ăn mới'} onClose={() => setModal(null)}>
          <form onSubmit={handleSubmit} className="space-y-4">
            <ImageUpload folder="fnb" value={form.imageUrl} onChange={val => setForm({ ...form, imageUrl: val })} />
            <FormField label="Tên sản phẩm" required>
              <input className={inputCls} value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="Ví dụ: Bắp rang bơ phô mai..." required />
            </FormField>
            <div className="grid grid-cols-2 gap-3">
              <FormField label="Giá bán (VNĐ)" required>
                <input className={inputCls} type="number" value={form.price} onChange={e => setForm({ ...form, price: Number(e.target.value) })} required />
              </FormField>
              <FormField label="Tồn kho" required>
                <input className={inputCls} type="number" min="0" value={form.stockQuantity} onChange={e => setForm({ ...form, stockQuantity: Number(e.target.value) })} required />
              </FormField>
            </div>
            <FormField label="Mô tả">
              <textarea className={inputCls + ' resize-none'} rows={3} value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Nêu các thành phần hoặc size của món..." />
            </FormField>
            <label className="flex items-center gap-2 cursor-pointer group">
              <div className="relative flex items-center">
                <input type="checkbox" className="sr-only peer" checked={form.isActive} onChange={e => setForm({ ...form, isActive: e.target.checked })} />
                <div className="w-10 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-orange-500" />
              </div>
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider group-hover:text-slate-700 transition-colors">Đang kinh doanh</span>
            </label>
            <div className="flex gap-3 pt-2">
              <button type="button" onClick={() => setModal(null)} className="flex-1 py-3 rounded-2xl border border-slate-200 text-slate-600 text-sm font-bold hover:bg-slate-50 transition-all font-['Space_Grotesk']">Hủy</button>
              <button type="submit" disabled={saving} className="flex-1 py-3 rounded-2xl bg-orange-500 hover:bg-orange-600 text-white text-sm font-black transition-all shadow-lg shadow-orange-100 disabled:opacity-60 font-['Space_Grotesk'] uppercase tracking-wider">{saving ? 'Đang lưu...' : modal.edit ? 'Cập nhật' : 'Tạo món'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function FnbManagement() {
  const [toast, setToast] = useState(null);

  const notify = (msg, type = 'info') => setToast({ msg, type });

  return (
    <div className="min-h-screen bg-[#F8FAFC] p-6 md:p-8 font-['Space_Grotesk'] antialiased">
      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}

      {/* Header */}
      <div className="mb-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="flex items-center gap-5">
          <div className="w-14 h-14 rounded-[22px] bg-gradient-to-br from-orange-400 to-red-500 flex items-center justify-center shadow-2xl shadow-orange-200/50">
            <span className="material-symbols-outlined text-white text-3xl">fastfood</span>
          </div>
          <div>
            <h1 className="text-3xl font-black text-slate-800 tracking-tight">Khu ẩm thực</h1>
            <p className="text-sm text-slate-400 font-medium">Quản lý thực đơn bắp nước và các combo ưu đãi cho rạp phim</p>
          </div>
        </div>

      </div>

      <div className="max-w-[1240px]">
        <ItemTab notify={notify} />
      </div>
    </div>
  );
}
