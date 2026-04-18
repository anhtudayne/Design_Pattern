import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBooking } from '../contexts/BookingContext';
import { fetchFnBItems } from '../services/fnbService';

// ─── Stepper component ──────────────────────────────────────────────
function Stepper({ active }) {
  const steps = ['Chọn Ghế', 'Bắp Nước', 'Thanh Toán'];
  return (
    <div className="flex items-center justify-center gap-0 mb-14">
      {steps.map((step, i) => {
        const idx = i + 1;
        const isDone = idx < active;
        const isCurrent = idx === active;
        return (
          <div key={step} className="flex items-center">
            <div className="flex flex-col items-center gap-2">
              <div className={`w-11 h-11 rounded-full flex items-center justify-center font-black text-sm transition-all ${
                isDone    ? 'bg-green-500 text-white shadow-lg shadow-green-500/30' :
                isCurrent ? 'bg-gradient-to-br from-orange-500 to-red-500 text-white shadow-lg shadow-orange-500/40' :
                            'bg-slate-100 text-slate-400'
              }`}>
                {isDone ? <span className="material-symbols-outlined text-lg">check</span> : idx}
              </div>
              <span className={`text-xs font-black uppercase tracking-wider whitespace-nowrap ${
                isCurrent ? 'text-orange-500' : isDone ? 'text-green-500' : 'text-slate-400'
              }`}>{step}</span>
            </div>
            {i < steps.length - 1 && (
              <div className={`w-16 md:w-28 h-0.5 mb-5 mx-2 rounded-full ${isDone ? 'bg-green-400' : 'bg-slate-200'}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}

export default function SnackSelection() {
  const navigate = useNavigate();
  const { bookingSelection, setBookingSnacks } = useBooking();
  const { movie, cinema, showtime, selectedSeats } = bookingSelection;

  const [loading, setLoading] = useState(true);
  const [fnbItems, setFnbItems] = useState([]);
  const [selectedFnB, setSelectedFnB] = useState({}); // { itemId: quantity }

  const getAvailableStock = (item) => {
    if (!item || item.isActive === false) return 0;
    return Number.POSITIVE_INFINITY;
  };

  // Redirect if no seats selected
  useEffect(() => {
    if (!selectedSeats || selectedSeats.length === 0) {
      navigate('/movies');
    }
  }, [selectedSeats, navigate]);

  // Fetch F&B theo rạp của suất đã chọn
  useEffect(() => {
    const loadFnB = async () => {
      setLoading(true);
      try {
        const data = await fetchFnBItems();
        setFnbItems(data);
      } catch (err) {
        console.error('Failed to fetch F&B items', err);
      } finally {
        setLoading(false);
      }
    };
    loadFnB();
  }, []);

  const updateQuantity = (itemId, delta) => {
    setSelectedFnB(prev => {
      const item = fnbItems.find(i => i.itemId === itemId);
      const stock = getAvailableStock(item);
      const current = prev[itemId] || 0;
      const next = Math.max(0, current + delta);
      const capped = Math.min(next, stock);
      return { ...prev, [itemId]: capped };
    });
  };

  // Use totalPrice from backend (already calculated in SeatSelection)
  const seatTotal = useMemo(() => {
    return (selectedSeats || []).reduce((sum, seat) => sum + (Number(seat.totalPrice) || 0), 0);
  }, [selectedSeats]);

  // Calculate Snack Total (BigDecimal-safe)
  const snackTotal = useMemo(() => {
    return Object.entries(selectedFnB).reduce((sum, [id, qty]) => {
      const item = fnbItems.find(i => i.itemId === parseInt(id));
      return sum + (item ? Number(item.price) * qty : 0);
    }, 0);
  }, [selectedFnB, fnbItems]);

  const grandTotal = seatTotal + snackTotal;

  // Category was removed from F&B model, keep one flat list for selection.
  const combos = [];
  const singles = fnbItems;

  const handleNext = () => {
    const snackList = Object.entries(selectedFnB)
      .filter(([, qty]) => qty > 0)
      .map(([id, qty]) => {
        const item = fnbItems.find(i => i.itemId === parseInt(id));
        return {
          itemId: item.itemId,    // Backend expects itemId
          name: item.name,
          unitPrice: Number(item.price),
          quantity: qty,
          imageUrl: item.imageUrl,
        };
      });
    setBookingSnacks(snackList);
    navigate('/booking/payment');
  };

  if (!movie || loading) {
    return (
      <div className="pt-44 flex flex-col items-center justify-center min-h-screen bg-slate-50 dark:bg-slate-950">
        <div className="w-16 h-16 border-4 border-orange-500/20 border-t-orange-500 rounded-full animate-spin"></div>
        <p className="mt-4 text-slate-400 font-bold uppercase tracking-widest text-xs">Đang tải bắp nước...</p>
      </div>
    );
  }

  return (
    <main className="pt-44 pb-20 bg-slate-50 dark:bg-slate-950 min-h-screen">
      <div className="max-w-[1440px] mx-auto px-6 md:px-10">

        <Stepper active={2} />

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start">

          {/* LEFT: F&B Selection */}
          <div className="lg:col-span-8 space-y-12">

            {combos.length > 0 && (
              <section>
                <div className="flex items-center justify-between mb-8">
                  <h2 className="text-3xl font-black text-slate-800 dark:text-white tracking-tight uppercase flex items-center gap-3">
                    <span className="w-2 h-8 bg-gradient-to-b from-orange-500 to-red-500 rounded-full"></span>
                    Combo Ưu Đãi
                  </h2>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {combos.map(item => (
                    <div key={item.itemId} className="bg-white dark:bg-slate-900 border-2 border-slate-100 dark:border-slate-800 p-6 rounded-2xl flex gap-6 hover:shadow-xl transition-all group">
                      <div className="w-32 h-32 rounded-xl overflow-hidden flex-shrink-0 border border-slate-100 dark:border-slate-700">
                        <img alt={item.name} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" 
                          src={item.imageUrl && item.imageUrl.startsWith('http') ? item.imageUrl : `https://lh3.googleusercontent.com/aida-public/${item.imageUrl}`} 
                        />
                      </div>
                      <div className="flex flex-col justify-between flex-grow">
                        <div>
                          <h3 className="font-black text-xl text-slate-800 dark:text-white mb-1">{item.name}</h3>
                          <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed line-clamp-2">{item.description}</p>
                          <p className={`text-xs font-bold mt-2 ${item.isActive !== false ? 'text-emerald-600' : 'text-red-500'}`}>
                            {item.isActive !== false ? 'Đang bán' : 'Ngừng bán'}
                          </p>
                        </div>
                        <div className="flex items-center justify-between mt-4">
                          <span className="font-black text-orange-500 text-lg">{Number(item.price).toLocaleString('vi-VN')}đ</span>
                          <div className="flex items-center bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl p-1">
                            <button onClick={() => updateQuantity(item.itemId, -1)} className="w-8 h-8 flex items-center justify-center hover:bg-white dark:hover:bg-slate-700 rounded-lg transition-colors"><span className="material-symbols-outlined text-sm text-slate-500">remove</span></button>
                            <span className="w-10 text-center font-black text-slate-800 dark:text-white">{selectedFnB[item.itemId] || 0}</span>
                            <button
                              onClick={() => updateQuantity(item.itemId, 1)}
                              disabled={(selectedFnB[item.itemId] || 0) >= getAvailableStock(item)}
                              className="w-8 h-8 flex items-center justify-center bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-lg shadow-sm disabled:opacity-40 disabled:cursor-not-allowed"
                            ><span className="material-symbols-outlined text-sm">add</span></button>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {singles.length > 0 && (
              <section>
                <h2 className="text-3xl font-black text-slate-800 dark:text-white tracking-tight uppercase mb-8 flex items-center gap-3">
                  <span className="w-2 h-8 bg-gradient-to-b from-cyan-500 to-blue-500 rounded-full"></span>
                  Lẻ & Drink
                </h2>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
                  {singles.map(item => (
                    <div key={item.itemId} className="bg-white dark:bg-slate-900 border-2 border-slate-100 dark:border-slate-800 p-5 rounded-2xl group transition-all hover:shadow-xl">
                      <div className="aspect-square rounded-xl overflow-hidden mb-4 border border-slate-100 dark:border-slate-700">
                        <img alt={item.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" 
                          src={item.imageUrl && item.imageUrl.startsWith('http') ? item.imageUrl : `https://lh3.googleusercontent.com/aida-public/${item.imageUrl}`} 
                        />
                      </div>
                      <h4 className="font-black text-lg text-slate-800 dark:text-white mb-2 line-clamp-1">{item.name}</h4>
                      <p className={`text-xs font-bold mb-2 ${item.isActive !== false ? 'text-emerald-600' : 'text-red-500'}`}>
                        {item.isActive !== false ? 'Đang bán' : 'Ngừng bán'}
                      </p>
                      <div className="flex items-center justify-between">
                        <span className="font-black text-orange-500">{Number(item.price).toLocaleString('vi-VN')}đ</span>
                        {(selectedFnB[item.itemId] || 0) > 0 ? (
                          <div className="flex items-center bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl p-1">
                            <button onClick={() => updateQuantity(item.itemId, -1)} className="w-7 h-7 flex items-center justify-center hover:bg-white dark:hover:bg-slate-700 rounded-lg transition-colors"><span className="material-symbols-outlined text-sm text-slate-500">remove</span></button>
                            <span className="w-7 text-center font-black text-sm text-slate-800 dark:text-white">{selectedFnB[item.itemId]}</span>
                            <button
                              onClick={() => updateQuantity(item.itemId, 1)}
                              disabled={(selectedFnB[item.itemId] || 0) >= getAvailableStock(item)}
                              className="w-7 h-7 flex items-center justify-center bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-lg shadow-sm disabled:opacity-40 disabled:cursor-not-allowed"
                            ><span className="material-symbols-outlined text-sm">add</span></button>
                          </div>
                        ) : (
                          <button
                            onClick={() => updateQuantity(item.itemId, 1)}
                            disabled={getAvailableStock(item) <= 0}
                            className="w-9 h-9 flex items-center justify-center bg-slate-50 dark:bg-slate-800 text-slate-500 border-2 border-slate-200 dark:border-slate-700 rounded-xl hover:border-orange-500 hover:text-orange-500 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
                          >
                            <span className="material-symbols-outlined text-base">add</span>
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            )}
          </div>

          {/* RIGHT: Sidebar */}
          <aside className="lg:col-span-4">
            <div className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-[2.5rem] shadow-2xl p-10 sticky top-32">

              {/* Movie Info */}
              <div className="flex gap-5 pb-8 border-b-2 border-slate-50 dark:border-slate-800">
                <div className="w-20 h-28 rounded-2xl overflow-hidden shadow-lg shrink-0 border border-slate-100 dark:border-slate-700">
                  <img alt={movie.title} className="w-full h-full object-cover" 
                    src={movie.posterUrl && movie.posterUrl.startsWith('http') ? movie.posterUrl : `https://lh3.googleusercontent.com/aida-public/${movie.posterUrl}`} 
                  />
                </div>
                <div>
                  <h4 className="font-black text-slate-800 dark:text-white leading-tight uppercase line-clamp-2">{movie.title}</h4>
                  <p className="text-sm text-slate-500 mt-1 font-medium">{cinema?.name}</p>
                  <p className="text-sm font-black text-orange-500 mt-1">
                    {showtime?.startTime?.split('T')[1]?.substring(0, 5)} • {showtime?.startTime ? new Date(showtime.startTime).toLocaleDateString('vi-VN') : ''}
                  </p>
                </div>
              </div>

              {/* Seat Info — uses backend totalPrice */}
              <div className="flex justify-between text-sm py-4 px-5 bg-slate-50 dark:bg-slate-800 border border-slate-100 dark:border-slate-700 rounded-xl mt-6">
                <span className="text-slate-500 font-bold whitespace-nowrap overflow-hidden text-ellipsis mr-2">
                  Ghế: {(selectedSeats || []).map(s => `${s.seatRow || s.rowName}${s.seatNumber}`).join(', ')}
                </span>
                <span className="font-black text-slate-800 dark:text-white shrink-0">{seatTotal.toLocaleString('vi-VN')}đ</span>
              </div>

              {/* Selected F&B List */}
              <div className="mt-6 pb-6 border-b-2 border-slate-50 dark:border-slate-800">
                <h3 className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-4 flex items-center gap-2">
                  <span className="material-symbols-outlined text-base">fastfood</span>
                  Bắp nước đã chọn
                </h3>
                <div className="space-y-3">
                  {Object.entries(selectedFnB).map(([id, qty]) => {
                    if (qty === 0) return null;
                    const item = fnbItems.find(i => i.itemId === parseInt(id));
                    if (!item) return null;
                    return (
                      <div key={id} className="flex justify-between items-center group">
                        <div className="flex items-center gap-2 overflow-hidden">
                          <button onClick={() => updateQuantity(parseInt(id), -qty)} className="text-red-400 opacity-0 group-hover:opacity-100 transition-opacity">
                            <span className="material-symbols-outlined text-sm">close</span>
                          </button>
                          <span className="text-sm text-slate-600 dark:text-slate-300 font-bold line-clamp-1">{qty} x {item.name}</span>
                        </div>
                        <span className="text-sm font-black text-slate-800 dark:text-white shrink-0">{(Number(item.price) * qty).toLocaleString('vi-VN')}đ</span>
                      </div>
                    );
                  })}
                  {snackTotal === 0 && (
                    <p className="text-sm text-slate-300 font-black italic">Chưa chọn bắp nước...</p>
                  )}
                </div>
              </div>

              {/* Total */}
              <div className="pt-6 space-y-6">
                <div>
                  <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Tổng cộng tạm tính</p>
                  <p className="text-4xl font-black tracking-tighter text-slate-800 dark:text-white">{grandTotal.toLocaleString('vi-VN')}đ</p>
                </div>
                <button
                  onClick={handleNext}
                  className="w-full py-5 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-[1.5rem] font-black text-base uppercase tracking-widest flex items-center justify-center gap-3 shadow-xl shadow-orange-500/30 hover:shadow-orange-500/50 hover:scale-[1.03] transition-all"
                >
                  <span className="material-symbols-outlined font-black">arrow_forward</span>
                  TIẾP THEO: THANH TOÁN
                </button>
              </div>

              <p className="text-[10px] text-center text-slate-400 mt-4 leading-relaxed">
                Bằng cách nhấn tiếp theo, bạn đồng ý với Điều khoản sử dụng và Chính sách bảo mật của StarCine.
              </p>
            </div>
          </aside>
        </div>
      </div>
    </main>
  );
}
