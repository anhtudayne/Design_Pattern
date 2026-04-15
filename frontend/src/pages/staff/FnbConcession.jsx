import { useState, useEffect, useMemo } from 'react';
import { fetchFnBItems, fetchFnBCategories } from '../../services/fnbService';
import { fetchCinemas } from '../../services/cinemaService';

const formatMoney = (v) => new Intl.NumberFormat('vi-VN').format(v || 0) + 'đ';

// ══════════════════════════════════════════════════════════════════════
//  F&B CONCESSION POS — Food & Drink Selling Screen
// ══════════════════════════════════════════════════════════════════════
export default function FnbConcession() {
  const [items, setItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [cinemas, setCinemas] = useState([]);
  const [selectedCinemaId, setSelectedCinemaId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeCategory, setActiveCategory] = useState('ALL');
  const [cart, setCart] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [paymentProcessing, setPaymentProcessing] = useState(false);
  const [paymentSuccess, setPaymentSuccess] = useState(false);

  useEffect(() => {
    fetchFnBCategories().then(setCategories).catch(console.error);
    fetchCinemas()
      .then((list) => {
        setCinemas(list);
        if (list?.length) setSelectedCinemaId(list[0].cinemaId);
      })
      .catch(console.error);
  }, []);

  useEffect(() => {
    if (selectedCinemaId == null) return;
    setCart([]);
    setLoading(true);
    fetchFnBItems(selectedCinemaId)
      .then(setItems)
      .catch((e) => console.error('Failed to load F&B data', e))
      .finally(() => setLoading(false));
  }, [selectedCinemaId]);

  // ── Filtered items ──────────────────────────────────────────────────
  const filteredItems = useMemo(() => {
    let result = items;
    if (activeCategory !== 'ALL') {
      result = result.filter(i => i.categoryId === parseInt(activeCategory));
    }
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(i => i.name?.toLowerCase().includes(q));
    }
    return result;
  }, [items, activeCategory, searchQuery]);

  // ── Cart helpers ────────────────────────────────────────────────────
  const addToCart = (item) => {
    setCart(prev => {
      const exists = prev.find(c => c.itemId === item.itemId);
      if (exists) {
        if (exists.quantity >= item.stockQuantity) return prev; // Limit to stock
        return prev.map(c => c.itemId === item.itemId ? { ...c, quantity: c.quantity + 1 } : c);
      }
      if (item.stockQuantity <= 0) return prev; // No stock to add
      return [...prev, { ...item, quantity: 1 }];
    });
  };

  const removeFromCart = (itemId) => {
    setCart(prev => {
      const item = prev.find(c => c.itemId === itemId);
      if (!item) return prev;
      if (item.quantity <= 1) return prev.filter(c => c.itemId !== itemId);
      return prev.map(c => c.itemId === itemId ? { ...c, quantity: c.quantity - 1 } : c);
    });
  };

  const clearCart = () => setCart([]);

  const cartTotal = useMemo(() => {
    return cart.reduce((sum, c) => sum + (c.price || 0) * c.quantity, 0);
  }, [cart]);

  const cartItemCount = useMemo(() => {
    return cart.reduce((sum, c) => sum + c.quantity, 0);
  }, [cart]);

  // ── Payment ─────────────────────────────────────────────────────────
  const handlePayment = async () => {
    if (cart.length === 0) return;
    setPaymentProcessing(true);
    await new Promise(r => setTimeout(r, 1200));
    setPaymentProcessing(false);
    setPaymentSuccess(true);
    setTimeout(() => {
      setPaymentSuccess(false);
      setCart([]);
    }, 2500);
  };

  // ── Hotkeys ─────────────────────────────────────────────────────────
  useEffect(() => {
    const handler = (e) => {
      if (e.key === 'Escape') clearCart();
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }, []);

  // ══════════════════════════════════════════════════════════════════════
  //  RENDER
  // ══════════════════════════════════════════════════════════════════════
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-[70vh]">
        <div className="w-12 h-12 border-4 border-orange-500/20 border-t-orange-500 rounded-full animate-spin"></div>
        <p className="mt-4 text-slate-400 font-bold text-xs uppercase tracking-widest">Đang tải danh mục F&B...</p>
      </div>
    );
  }

  if (paymentSuccess) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-green-500/90 backdrop-blur-lg">
        <div className="text-center text-white">
          <span className="material-symbols-outlined text-[120px] mb-4 block animate-bounce">check_circle</span>
          <h1 className="text-5xl font-black uppercase tracking-wider mb-4">Thanh Toán Thành Công!</h1>
          <p className="text-lg font-medium opacity-80">Đang chuẩn bị đơn mới...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex gap-4 h-[calc(100vh-8rem)]">
      {/* ════════════════════════════════════════════════════════════════
          LEFT: PRODUCT GRID (70%)
      ════════════════════════════════════════════════════════════════ */}
      <div className="flex-[7] flex flex-col min-w-0 overflow-hidden">

        {/* Chi nhánh + Search + Category Tabs */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 mb-4 flex-wrap">
          <div className="flex items-center gap-2 min-w-[200px]">
            <span className="material-symbols-outlined text-slate-400 text-lg">storefront</span>
            <select
              value={selectedCinemaId ?? ''}
              onChange={(e) => setSelectedCinemaId(Number(e.target.value))}
              className="flex-1 min-w-[180px] px-3 py-2.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 text-sm font-bold text-slate-800 dark:text-white focus:outline-none focus:border-orange-500"
            >
              {cinemas.map((c) => (
                <option key={c.cinemaId} value={c.cinemaId}>{c.name}</option>
              ))}
            </select>
          </div>
          {/* Search */}
          <div className="relative flex-1 max-w-sm">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-lg">search</span>
            <input
              type="text"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder="Tìm sản phẩm..."
              className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 text-sm font-medium text-slate-800 dark:text-white placeholder-slate-400 focus:outline-none focus:border-orange-500 transition-colors shadow-sm"
            />
          </div>

          {/* Category tabs */}
          <div className="flex items-center gap-1 flex-wrap">
            <button
              onClick={() => setActiveCategory('ALL')}
              className={`px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all ${
                activeCategory === 'ALL'
                  ? 'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-md shadow-orange-500/30'
                  : 'bg-white dark:bg-slate-900 text-slate-500 border border-slate-100 dark:border-slate-800 hover:border-orange-500/50'
              }`}
            >
              Tất cả
            </button>
            {categories.map(cat => (
              <button
                key={cat.categoryId}
                onClick={() => setActiveCategory(String(cat.categoryId))}
                className={`px-4 py-2 rounded-xl text-xs font-bold uppercase tracking-wider transition-all ${
                  activeCategory === String(cat.categoryId)
                    ? 'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-md shadow-orange-500/30'
                    : 'bg-white dark:bg-slate-900 text-slate-500 border border-slate-100 dark:border-slate-800 hover:border-orange-500/50'
                }`}
              >
                {cat.name}
              </button>
            ))}
          </div>
        </div>

        {/* Product Grid */}
        <div className="flex-1 overflow-y-auto pr-1">
          {filteredItems.length === 0 ? (
            <div className="text-center py-20">
              <span className="material-symbols-outlined text-5xl text-slate-300 mb-3 block">no_food</span>
              <p className="text-slate-400 font-bold">Không tìm thấy sản phẩm</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3">
              {filteredItems.map(item => {
                const inCart = cart.find(c => c.itemId === item.itemId);
                return (
                  <div
                    key={item.itemId}
                    className={`bg-white dark:bg-slate-900 rounded-2xl border shadow-sm hover:shadow-lg transition-all overflow-hidden group relative ${
                      inCart ? 'border-orange-500 shadow-orange-500/10' : 'border-slate-100 dark:border-slate-800'
                    }`}
                  >
                    {/* Product Image */}
                    <div className="aspect-square overflow-hidden bg-slate-50 dark:bg-slate-800 relative">
                      {item.imageUrl ? (
                        <img
                          alt={item.name}
                          className={`w-full h-full object-cover group-hover:scale-105 transition-transform duration-300 ${item.stockQuantity <= 0 ? 'grayscale opacity-50' : ''}`}
                          src={item.imageUrl}
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center">
                          <span className="material-symbols-outlined text-4xl text-slate-300">fastfood</span>
                        </div>
                      )}

                      {/* Stock badge */}
                      <div className={`absolute top-2 left-2 px-2 py-0.5 rounded-full text-[9px] font-black uppercase tracking-widest shadow-sm ${
                        item.stockQuantity > 0 ? 'bg-white/90 text-slate-600' : 'bg-red-500 text-white'
                      }`}>
                        {item.stockQuantity > 0 ? `Tồn: ${item.stockQuantity}` : 'Hết hàng'}
                      </div>

                      {/* Quantity badge */}
                      {inCart && (
                        <div className="absolute top-2 right-2 w-7 h-7 rounded-full bg-orange-500 text-white text-xs font-black flex items-center justify-center shadow-lg">
                          {inCart.quantity}
                        </div>
                      )}
                    </div>

                    {/* Product Info */}
                    <div className="p-3">
                      <h3 className="text-xs font-bold text-slate-800 dark:text-white leading-tight line-clamp-2 mb-1">{item.name}</h3>
                      <p className="text-sm font-black text-orange-500">{formatMoney(item.price)}</p>
                    </div>

                    {/* Quick Add/Remove Buttons */}
                    <div className="flex border-t border-slate-100 dark:border-slate-800">
                      <button
                        onClick={() => removeFromCart(item.itemId)}
                        disabled={!inCart}
                        className="flex-1 py-2.5 flex items-center justify-center text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors disabled:opacity-30 disabled:cursor-not-allowed border-r border-slate-100 dark:border-slate-800"
                      >
                        <span className="material-symbols-outlined text-xl">remove</span>
                      </button>
                      <button
                        onClick={() => addToCart(item)}
                        disabled={item.stockQuantity <= 0 || (inCart && inCart.quantity >= item.stockQuantity)}
                        className="flex-1 py-2.5 flex items-center justify-center text-slate-400 hover:text-green-500 hover:bg-green-50 dark:hover:bg-green-500/10 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
                      >
                        <span className="material-symbols-outlined text-xl">add</span>
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* ════════════════════════════════════════════════════════════════
          RIGHT: CART PANEL (30%)
      ════════════════════════════════════════════════════════════════ */}
      <div className="flex-[3] flex flex-col min-w-[280px] max-w-[380px] bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl shadow-lg overflow-hidden">
        {/* Cart Header */}
        <div className="px-5 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
          <h3 className="text-sm font-black text-slate-800 dark:text-white uppercase tracking-wider flex items-center gap-2">
            <span className="material-symbols-outlined text-orange-500 text-lg">shopping_cart</span>
            Giỏ hàng
            {cartItemCount > 0 && (
              <span className="bg-orange-500 text-white text-[10px] font-black px-2 py-0.5 rounded-full">{cartItemCount}</span>
            )}
          </h3>
          {cart.length > 0 && (
            <button onClick={clearCart} className="text-[10px] font-bold text-red-400 hover:text-red-500 uppercase tracking-widest transition-colors">
              Xóa tất cả
            </button>
          )}
        </div>

        {/* Cart Body */}
        <div className="flex-1 overflow-y-auto p-5">
          {cart.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-center">
              <span className="material-symbols-outlined text-4xl text-slate-200 mb-2">shopping_bag</span>
              <p className="text-xs text-slate-400 font-bold">Chưa có sản phẩm nào</p>
              <p className="text-[10px] text-slate-300 mt-1">Bấm [+] để thêm vào giỏ hàng</p>
            </div>
          ) : (
            <div className="space-y-3">
              {cart.map(item => (
                <div key={item.itemId} className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-3 border border-slate-100 dark:border-slate-800">
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                      <p className="text-xs font-bold text-slate-800 dark:text-white leading-tight truncate">{item.name}</p>
                      <p className="text-[10px] text-slate-400 mt-0.5">{formatMoney(item.price)} / món</p>
                    </div>
                    <p className="text-sm font-black text-orange-500 shrink-0">{formatMoney(item.price * item.quantity)}</p>
                  </div>
                  <div className="flex items-center gap-2 mt-2">
                    <button
                      onClick={() => removeFromCart(item.itemId)}
                      className="w-7 h-7 rounded-lg bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 text-slate-500 flex items-center justify-center hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors"
                    >
                      <span className="material-symbols-outlined text-base">remove</span>
                    </button>
                    <span className="w-8 text-center text-sm font-black text-slate-800 dark:text-white">{item.quantity}</span>
                    <button
                      onClick={() => addToCart(item)}
                      className="w-7 h-7 rounded-lg bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 text-slate-500 flex items-center justify-center hover:bg-green-50 hover:text-green-500 hover:border-green-200 transition-colors"
                    >
                      <span className="material-symbols-outlined text-base">add</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Cart Footer */}
        <div className="border-t border-slate-100 dark:border-slate-800 p-5 space-y-3">
          <div className="flex justify-between items-center">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">Tổng cộng</span>
            <span className="text-2xl font-black text-slate-800 dark:text-white tracking-tight">{formatMoney(cartTotal)}</span>
          </div>

          {/* Payment Buttons */}
          <div className="space-y-2">
            <button
              onClick={handlePayment}
              disabled={cart.length === 0 || paymentProcessing}
              className="w-full py-4 rounded-xl bg-gradient-to-r from-green-500 to-emerald-600 text-white font-black text-sm uppercase tracking-widest shadow-lg shadow-green-500/30 hover:shadow-green-500/50 disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
            >
              {paymentProcessing ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              ) : (
                <>
                  <span className="material-symbols-outlined text-lg">payments</span>
                  Tiền Mặt
                </>
              )}
            </button>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={handlePayment}
                disabled={cart.length === 0 || paymentProcessing}
                className="py-3 rounded-xl bg-gradient-to-r from-blue-500 to-cyan-600 text-white font-bold text-xs uppercase tracking-widest shadow-md disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-1.5"
              >
                <span className="material-symbols-outlined text-base">credit_card</span>
                Thẻ
              </button>
              <button
                onClick={handlePayment}
                disabled={cart.length === 0 || paymentProcessing}
                className="py-3 rounded-xl bg-gradient-to-r from-violet-500 to-purple-600 text-white font-bold text-xs uppercase tracking-widest shadow-md disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-1.5"
              >
                <span className="material-symbols-outlined text-base">qr_code_scanner</span>
                QR
              </button>
            </div>
          </div>

          <button
            onClick={clearCart}
            className="w-full py-2 rounded-lg text-xs font-bold text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-all uppercase tracking-widest"
          >
            Esc — Hủy đơn
          </button>
        </div>
      </div>
    </div>
  );
}
