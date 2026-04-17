import { useState, useEffect, useCallback } from 'react';
import { getAuthHeaders, BASE_URL } from '../../utils/api';

const API = `${BASE_URL}/promotions`;

const inputCls = 'w-full px-4 py-3 rounded-2xl border border-slate-200 text-sm text-slate-800 focus:outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-100 transition-all font-medium';

function Toast({ msg, type, onClose }) {
  useEffect(() => { const t = setTimeout(onClose, 3000); return () => clearTimeout(t); }, [onClose]);
  const colors = { success: 'bg-green-600', error: 'bg-red-600', info: 'bg-blue-600' };
  return (
    <div className={`fixed bottom-6 right-6 z-[100] flex items-center gap-3 px-5 py-3.5 rounded-xl text-white shadow-xl text-sm font-medium animate-in slide-in-from-bottom border border-white/20 ${colors[type] || colors.info}`}>
      <span className="material-symbols-outlined text-lg">
        {type === 'success' ? 'check_circle' : type === 'error' ? 'error' : 'info'}
      </span>
      {msg}
      <button type="button" onClick={onClose} className="ml-2 opacity-70 hover:opacity-100">
        <span className="material-symbols-outlined text-base">close</span>
      </button>
    </div>
  );
}

function FormField({ label, required, children, icon }) {
  return (
    <div className="space-y-1.5">
      <label className="text-[11px] font-black text-slate-400 uppercase tracking-[1px] ml-1 flex items-center gap-1.5">
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
      <div className="bg-white rounded-[32px] shadow-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto font-['Space_Grotesk'] border border-slate-100">
        <div className="flex items-center justify-between px-8 py-6 border-b border-slate-50">
          <h3 className="font-black text-slate-800 text-xl tracking-tight uppercase tracking-tighter">
             <span className="text-orange-500 mr-2 opacity-50">#</span>
             {title}
          </h3>
          <button type="button" onClick={onClose} className="w-10 h-10 flex items-center justify-center rounded-2xl hover:bg-slate-100 transition-colors text-slate-400">
            <span className="material-symbols-outlined">close</span>
          </button>
        </div>
        <div className="p-8">{children}</div>
      </div>
    </div>
  );
}

function toDatetimeLocal(iso) {
  if (!iso) return '';
  const s = String(iso);
  return s.length >= 16 ? s.slice(0, 16) : s;
}

function normalizeValidTo(v) {
  if (!v) return null;
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(v)) return `${v}:00`;
  return v;
}

function formatValidTo(iso) {
  if (!iso) return '—';
  try {
    return new Date(iso).toLocaleString('vi-VN');
  } catch {
    return String(iso);
  }
}

function formatDiscount(p) {
  if (!p || p.discountValue == null) return '—';
  if (p.discountType === 'PERCENT') return `-${p.discountValue}%`;
  return `-${Number(p.discountValue).toLocaleString('vi-VN')}đ`;
}

export default function VoucherManagement() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modal, setModal] = useState(null);
  const [toast, setToast] = useState(null);
  const [search, setSearch] = useState('');

  const [form, setForm] = useState({
    code: '',
    discountType: 'PERCENT',
    discountValue: 10,
    validTo: ''
  });

  const notify = (msg, type = 'info') => setToast({ msg, type });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const r = await fetch(API, { headers: getAuthHeaders() });
      if (r.ok) setList(await r.json());
      else notify('Không thể tải danh sách khuyến mãi', 'error');
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const openAdd = () => {
    const d = new Date();
    d.setFullYear(d.getFullYear() + 1);
    const pad = (n) => String(n).padStart(2, '0');
    const defaultValid = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    setForm({ code: '', discountType: 'PERCENT', discountValue: 10, validTo: defaultValid });
    setModal('add');
  };

  const openEdit = (p) => {
    setForm({
      code: p.code,
      discountType: p.discountType || 'PERCENT',
      discountValue: p.discountValue,
      validTo: toDatetimeLocal(p.validTo)
    });
    setModal({ edit: p });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    const isEdit = !!modal?.edit;
    const vt = normalizeValidTo(form.validTo);
    if (!vt) {
      notify('Vui lòng chọn thời hạn hiệu lực.', 'error');
      setSaving(false);
      return;
    }
    try {
      if (isEdit) {
        const r = await fetch(`${API}/${modal.edit.id}`, {
          method: 'PUT',
          headers: getAuthHeaders(),
          body: JSON.stringify({
            discountType: form.discountType,
            discountValue: Number(form.discountValue),
            validTo: vt
          })
        });
        if (r.ok) {
          notify('Cập nhật thành công!', 'success');
          setModal(null);
          load();
        } else {
          const err = await r.json().catch(() => ({}));
          notify(err.message || 'Lỗi khi cập nhật', 'error');
        }
      } else {
        const r = await fetch(API, {
          method: 'POST',
          headers: getAuthHeaders(),
          body: JSON.stringify({
            code: form.code.trim().toUpperCase(),
            discountType: form.discountType,
            discountValue: Number(form.discountValue),
            validTo: vt
          })
        });
        if (r.ok) {
          notify('Tạo khuyến mãi thành công!', 'success');
          setModal(null);
          load();
        } else {
          const err = await r.json().catch(() => ({}));
          notify(err.message || 'Lỗi khi tạo mới', 'error');
        }
      }
    } finally { setSaving(false); }
  };

  const handleDelete = async (id, code) => {
    if (!window.confirm(`Xóa mã khuyến mãi "${code}"?`)) return;
    const r = await fetch(`${API}/${id}`, { method: 'DELETE', headers: getAuthHeaders() });
    if (r.ok) { notify('Đã xóa.', 'success'); load(); }
    else notify('Xóa thất bại (có thể đang được dùng bởi đơn đặt).', 'error');
  };

  const filtered = list.filter((p) => p.code && p.code.toLowerCase().includes(search.toLowerCase()));

  return (
    <div className="min-h-screen bg-slate-50/50 p-6 md:p-10 font-['Space_Grotesk'] antialiased">
      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}

      <div className="max-w-6xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-10">
          <div className="flex items-center gap-6">
            <div className="w-16 h-16 rounded-[24px] bg-gradient-to-tr from-orange-400 via-red-500 to-pink-500 flex items-center justify-center shadow-2xl shadow-orange-200 rotate-3">
              <span className="material-symbols-outlined text-white text-3xl">local_offer</span>
            </div>
            <div>
              <h1 className="text-3xl font-black text-slate-800 tracking-tighter flex items-center gap-3 uppercase">Khuyến mãi</h1>
              <p className="text-sm text-slate-400 font-bold uppercase tracking-widest mt-0.5">Quản lý mã giảm giá (lưu trong database)</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="relative group flex-1 md:w-64">
              <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-xl transition-colors group-focus-within:text-orange-500">search</span>
              <input
                className="w-full pl-12 pr-4 py-3 rounded-2xl border border-slate-200 bg-white text-sm font-bold focus:outline-none focus:border-orange-400 focus:ring-4 focus:ring-orange-100 transition-all placeholder:font-medium"
                placeholder="Tìm mã..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
            <button type="button" onClick={openAdd}
              className="flex items-center gap-2 px-6 py-3 rounded-2xl bg-slate-900 text-white text-sm font-black hover:bg-black transition-all shadow-xl shadow-slate-100 active:scale-95 uppercase tracking-wider">
              <span className="material-symbols-outlined text-sm text-orange-400">new_label</span>
              Thêm mới
            </button>
          </div>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-32 space-y-4">
             <div className="w-10 h-10 border-4 border-orange-200 border-t-orange-500 rounded-full animate-spin" />
             <p className="text-xs font-black text-slate-300 uppercase tracking-[2px]">Đang tải...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="py-24 text-center bg-white rounded-[40px] border-2 border-dashed border-slate-100 shadow-inner">
            <div className="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-6">
               <span className="material-symbols-outlined text-5xl text-slate-200">sell</span>
            </div>
            <p className="font-black text-slate-600 text-lg">{search ? 'Không tìm thấy mã phù hợp' : 'Chưa có khuyến mãi nào'}</p>
            <p className="text-sm text-slate-400 mt-2 font-medium">Bấm &quot;Thêm mới&quot; để tạo mã (PERCENT hoặc FIXED).</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filtered.map((p) => (
              <div key={p.id} className="group relative bg-white rounded-[32px] p-1 shadow-sm border border-slate-100 hover:border-orange-200 hover:shadow-2xl hover:shadow-orange-100/30 transition-all duration-500 overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-orange-50 rounded-full -mr-16 -mt-16 opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
                <div className="relative p-6 space-y-4">
                  <div className="flex justify-between items-start">
                    <div className="px-4 py-2 bg-orange-600 text-white rounded-2xl font-black text-sm tracking-tight shadow-lg shadow-orange-200">
                      {p.code}
                    </div>
                    <div className="flex items-center gap-1">
                       <button type="button" onClick={() => openEdit(p)} className="w-9 h-9 rounded-xl bg-slate-50 text-slate-400 hover:bg-orange-50 hover:text-orange-600 transition-all flex items-center justify-center">
                          <span className="material-symbols-outlined text-lg">edit</span>
                       </button>
                       <button type="button" onClick={() => handleDelete(p.id, p.code)} className="w-9 h-9 rounded-xl bg-slate-50 text-slate-400 hover:bg-red-50 hover:text-red-500 transition-all flex items-center justify-center">
                          <span className="material-symbols-outlined text-lg">delete</span>
                       </button>
                    </div>
                  </div>

                  <div className="space-y-3 pt-2">
                    <div className="flex flex-col">
                       <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Ưu đãi</span>
                       <p className="text-3xl font-black text-slate-800 tracking-tighter">{formatDiscount(p)}</p>
                    </div>
                    <div className="text-[11px] font-bold text-slate-500 uppercase tracking-tight">
                      Loại: <span className="text-slate-800">{p.discountType}</span>
                    </div>
                    <div className="py-3 border-y border-slate-50 border-dashed text-sm text-slate-600">
                      <span className="text-[10px] font-black text-slate-400 uppercase block mb-1">Hiệu lực đến</span>
                      {formatValidTo(p.validTo)}
                    </div>
                    <div className="flex items-center gap-2">
                       <div className="w-2 h-2 rounded-full bg-green-500" />
                       <span className="text-[11px] font-bold text-slate-400 uppercase tracking-tight">Lưu trong database</span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {modal && (
          <Modal title={modal.edit ? 'Sửa khuyến mãi' : 'Tạo khuyến mãi'} onClose={() => setModal(null)}>
            <form onSubmit={handleSubmit} className="space-y-6">
              <FormField label="Mã" required icon="key">
                <input
                  className={inputCls + (modal.edit ? ' bg-slate-50 text-slate-400' : '')}
                  value={form.code}
                  onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })}
                  placeholder="VD: WELCOME10"
                  readOnly={!!modal.edit}
                  required={!modal.edit}
                />
              </FormField>

              <div className="grid grid-cols-2 gap-4">
                <FormField label="Loại" required icon="category">
                  <select
                    className={inputCls + ' bg-white'}
                    value={form.discountType}
                    onChange={(e) => setForm({ ...form, discountType: e.target.value })}
                  >
                    <option value="PERCENT">PERCENT (%)</option>
                    <option value="FIXED">FIXED (VND)</option>
                  </select>
                </FormField>
                <FormField label={form.discountType === 'PERCENT' ? 'Giá trị %' : 'Số tiền (VND)'} required icon="payments">
                  <input
                    className={inputCls}
                    type="number"
                    min="0"
                    step={form.discountType === 'PERCENT' ? '1' : '1000'}
                    value={form.discountValue}
                    onChange={(e) => setForm({ ...form, discountValue: e.target.value })}
                    required
                  />
                </FormField>
              </div>

              <FormField label="Hiệu lực đến" required icon="schedule">
                <input
                  className={inputCls}
                  type="datetime-local"
                  value={form.validTo}
                  onChange={(e) => setForm({ ...form, validTo: e.target.value })}
                  required
                />
              </FormField>

              <div className="flex gap-4 pt-2">
                <button type="button" onClick={() => setModal(null)} className="flex-1 py-4 rounded-2xl border border-slate-200 text-slate-600 text-sm font-bold hover:bg-slate-50 transition-all uppercase tracking-wider">Hủy</button>
                <button type="submit" disabled={saving}
                  className="flex-1 py-4 rounded-2xl bg-orange-500 hover:bg-orange-600 text-white text-sm font-black transition-all shadow-lg shadow-orange-100 disabled:opacity-60 uppercase tracking-tighter">
                  {saving ? 'Đang lưu...' : modal.edit ? 'Cập nhật' : 'Tạo mới'}
                </button>
              </div>
            </form>
          </Modal>
        )}
      </div>
    </div>
  );
}
