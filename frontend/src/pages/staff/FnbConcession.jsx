import { useState, useEffect, useMemo } from 'react';
import { fetchFnBItems } from '../../services/fnbService';

const formatMoney = (v) => new Intl.NumberFormat('vi-VN').format(v || 0) + 'đ';

// ══════════════════════════════════════════════════════════════════════
//  F&B CONCESSION POS — Food & Drink Selling Screen
// ══════════════════════════════════════════════════════════════════════
export default function FnbConcession() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cart, setCart] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [paymentProcessing, setPaymentProcessing] = useState(false);
  const [paymentSuccess, setPaymentSuccess] = useState(false);

  useEffect(() => {
    setCart([]);
    setLoading(true);
    fetchFnBItems()
      .then(setItems)
      .catch((e) => console.error('Failed to load F&B data', e))
      .finally(() => setLoading(false));
  }, []);

  // ── Filtered items ──────────────────────────────────────────────────
  const filteredItems = useMemo(() => {
    let result = items;
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(i => i.name?.toLowerCase().includes(q));
    }
    return result;
  }, [items, searchQuery]);

  // ── Cart helpers ────────────────────────────────────────────────────
  const addToCart = (item) => {
    setCart(prev => {
      const exists = prev.find(c => c.fnbItemId === item.fnbItemId);
      if (exists) {
        if (exists.quantity >= item.stockQuantity) return prev; // Limit to stock
        return prev.map(c => c.fnbItemId === item.fnbItemId ? { ...c, quantity: c.quantity + 1 } : c);
      }
      if (item.stockQuantity <= 0) return prev; // No stock to add
      return [...prev, { ...item, quantity: 1 }];
    });
  };

  const removeFromCart = (itemId) => {
    setCart(prev => {
      const item = prev.find(c => c.fnbItemId === itemId);
      if (!item) return prev;
      if (item.quantity <= 1) return prev.filter(c => c.fnbItemId !== itemId);
      return prev.map(c => c.fnbItemId === itemId ? { ...c, quantity: c.quantity - 1 } : c);
    });
  };

  const clearCart = () => setCart([]);

  const cartTotal = useMemo(() => {
    return cart.reduce((sum, c) => sum + (c.price || 0) * c.quantity, 0);
  }, [cart]);

  const cartItemCount = useMemo(() => {
    return cart.reduce((sum, c) => sum + c.quantity, 0);
  }, [cart]);

  // ── Payment (đồng bộ nhãn với khách & POS vé: MoMo → Tiền mặt → VNPay) ──
  const handleCashPayment = async () => {
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

  const handleMomoPayment = () => {
    alert(
      'Thanh toán MoMo cho đơn F&B độc lập chưa kết nối cổng. Vui lòng chọn Tiền mặt hoặc thanh toán kèm vé tại quầy POS vé.'
    );
  };

  const handleVnpayPayment = () => {
    alert('Cổng thanh toán VNPay đang được tích hợp. Vui lòng dùng Tiền mặt.');
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

        {/* Search */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-3 mb-4 flex-wrap">
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
                const inCart = cart.find(c => c.fnbItemId === item.fnbItemId);
                return (
                  <div
                    key={item.fnbItemId}
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
                        onClick={() => removeFromCart(item.fnbItemId)}
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
                <div key={item.fnbItemId} className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-3 border border-slate-100 dark:border-slate-800">
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                      <p className="text-xs font-bold text-slate-800 dark:text-white leading-tight truncate">{item.name}</p>
                      <p className="text-[10px] text-slate-400 mt-0.5">{formatMoney(item.price)} / món</p>
                    </div>
                    <p className="text-sm font-black text-orange-500 shrink-0">{formatMoney(item.price * item.quantity)}</p>
                  </div>
                  <div className="flex items-center gap-2 mt-2">
                    <button
                      onClick={() => removeFromCart(item.fnbItemId)}
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

          {/* Payment: MoMo → Tiền mặt → VNPay */}
          <div className="space-y-2">
            <button
              type="button"
              onClick={handleMomoPayment}
              disabled={cart.length === 0 || paymentProcessing}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-pink-500 to-rose-600 text-white font-black text-xs uppercase tracking-widest shadow-lg shadow-pink-500/25 hover:shadow-pink-500/40 disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
            >
              <span className="material-symbols-outlined text-lg">account_balance_wallet</span>
              MoMo
            </button>
            <button
              type="button"
              onClick={handleCashPayment}
              disabled={cart.length === 0 || paymentProcessing}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-green-500 to-emerald-600 text-white font-black text-xs uppercase tracking-widest shadow-lg shadow-green-500/30 hover:shadow-green-500/50 disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
            >
              {paymentProcessing ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              ) : (
                <>
                  <span className="material-symbols-outlined text-lg">payments</span>
                  Tiền mặt
                </>
              )}
            </button>
            <button
              type="button"
              onClick={handleVnpayPayment}
              disabled={cart.length === 0 || paymentProcessing}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-700 text-white font-black text-xs uppercase tracking-widest shadow-md shadow-blue-600/25 disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
            >
              <span className="material-symbols-outlined text-lg">credit_card</span>
              VNPay
            </button>
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
