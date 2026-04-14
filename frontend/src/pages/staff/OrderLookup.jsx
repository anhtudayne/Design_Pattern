import { useState, useEffect } from "react";
import { BASE_URL, getAuthHeaders } from "../../utils/api";

const formatMoney = (v) => new Intl.NumberFormat("vi-VN").format(v || 0) + "đ";

const STATUS_MAP = {
  PENDING: {
    label: "Chờ thanh toán",
    color: "bg-yellow-100 text-yellow-700 border-yellow-200",
    icon: "schedule",
  },
  CONFIRMED: {
    label: "Đã thanh toán",
    color: "bg-green-100 text-green-700 border-green-200",
    icon: "check_circle",
  },
  CANCELLED: {
    label: "Đã hủy",
    color: "bg-red-100 text-red-700 border-red-200",
    icon: "cancel",
  },
  REFUNDED: {
    label: "Đã hoàn tiền",
    color: "bg-blue-100 text-blue-700 border-blue-200",
    icon: "currency_exchange",
  },
};

// ══════════════════════════════════════════════════════════════════════
//  ORDER LOOKUP — Search & Manage Bookings with Modal Detail
// ══════════════════════════════════════════════════════════════════════
export default function OrderLookup() {
  const [query, setQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [results, setResults] = useState([]);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [loading, setLoading] = useState(false);

  // ── Fetch Bookings ───────────────────────────────────────────────────
  const fetchBookings = async (searchQuery = "") => {
    setSearching(true);
    try {
      const res = await fetch(
        `${BASE_URL}/booking/search?query=${encodeURIComponent(searchQuery)}`,
        { headers: getAuthHeaders() }
      );
      if (res.ok) {
        setResults(await res.json());
      } else {
        setResults([]);
      }
    } catch (e) {
      console.error(e);
      setResults([]);
    } finally {
      setSearching(false);
    }
  };

  useEffect(() => {
    fetchBookings(""); // Initial load
  }, []);

  const handleSearch = (e) => {
    e?.preventDefault();
    fetchBookings(query);
  };

  const handleAction = async (action, bookingId) => {
    try {
      const res = await fetch(`${BASE_URL}/booking/${bookingId}/${action}`, { 
        method: 'POST', 
        headers: getAuthHeaders() 
      });
      const msg = await res.text();
      alert(res.ok ? msg : `Lỗi: ${msg}`);
      if (res.ok) {
        fetchBookings(query);
        setSelectedBooking(null);
      }
    } catch (e) {
      alert('Lỗi kết nối máy chủ');
    }
  };

  // ══════════════════════════════════════════════════════════════════════
  //  MODAL COMPONENT
  // ══════════════════════════════════════════════════════════════════════
  const BookingDetailModal = ({ booking, onClose }) => {
    if (!booking) return null;
    const status = STATUS_MAP[booking.status] || STATUS_MAP.PENDING;
    
    const ticketTotal = booking.tickets?.reduce((s, t) => s + t.price, 0) || 0;
    const fnbTotal = booking.fnBLines?.reduce((s, f) => s + (f.unitPrice * f.quantity), 0) || 0;
    const totalAmount = ticketTotal + fnbTotal;

    return (
      <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-md animate-in fade-in duration-200">
        <div className="bg-white dark:bg-slate-900 w-full max-w-4xl max-h-[90vh] rounded-[2.5rem] shadow-2xl overflow-hidden flex flex-col animate-in zoom-in-95 duration-200 border border-white/20">
          
          {/* Header */}
          <div className="px-8 py-6 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-orange-500 shadow-lg shadow-orange-500/30 flex items-center justify-center text-white">
                <span className="material-symbols-outlined text-2xl">confirmation_number</span>
              </div>
              <div>
                <h2 className="text-xl font-black text-slate-800 dark:text-white uppercase">Đơn hàng #{booking.bookingId}</h2>
                <div className="flex items-center gap-3 mt-1">
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold border uppercase tracking-widest ${status.color}`}>
                    {status.label}
                  </span>
                  <p className="text-[11px] text-slate-400 font-bold uppercase tracking-widest">
                    Mã: <span className="text-slate-600 dark:text-slate-300">{booking.bookingCode}</span>
                  </p>
                </div>
              </div>
            </div>
            <button onClick={onClose} className="w-12 h-12 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 flex items-center justify-center transition-all">
              <span className="material-symbols-outlined text-slate-400">close</span>
            </button>
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto p-8 custom-scrollbar">
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
              
              {/* Left Column (Tickets & F&B) */}
              <div className="lg:col-span-2 space-y-8">
                {/* Tickets Section */}
                <section>
                  <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] mb-4 flex items-center gap-2">
                    <span className="material-symbols-outlined text-sm">chair</span> Danh sách ghế đặt
                  </h3>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    {booking.tickets?.map(ticket => (
                      <div key={ticket.ticketId} className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 flex items-center gap-4 hover:border-orange-500/30 transition-colors">
                        <div className="w-10 h-10 rounded-xl bg-white dark:bg-slate-700 flex flex-col items-center justify-center text-slate-400 border border-slate-100 dark:border-slate-600">
                          <span className="text-[9px] font-black leading-none">HÀNG</span>
                          <span className="text-sm font-black text-orange-500">{ticket.seatCode}</span>
                        </div>
                        <div>
                          <p className="text-xs font-black text-slate-800 dark:text-white">{ticket.seatType}</p>
                          <p className="text-[11px] text-orange-500 font-black">{formatMoney(ticket.price)}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </section>

                {/* F&B Section */}
                {booking.fnBLines?.length > 0 && (
                  <section>
                    <h3 className="text-[11px] font-black text-slate-400 uppercase tracking-[0.2em] mb-4 flex items-center gap-2">
                      <span className="material-symbols-outlined text-sm">fastfood</span> Dịch vụ đi kèm
                    </h3>
                    <div className="rounded-2xl border border-slate-100 dark:border-slate-800 overflow-hidden bg-white dark:bg-slate-800/30">
                      <table className="w-full text-left text-xs">
                        <thead>
                          <tr className="bg-slate-50 dark:bg-slate-800 text-slate-500">
                            <th className="px-5 py-3 font-black uppercase tracking-widest text-[9px]">Sản phẩm</th>
                            <th className="px-5 py-3 font-black uppercase tracking-widest text-[9px] text-center">SL</th>
                            <th className="px-5 py-3 font-black uppercase tracking-widest text-[9px] text-right">Đơn giá</th>
                            <th className="px-5 py-3 font-black uppercase tracking-widest text-[9px] text-right">Tổng</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-50 dark:divide-slate-800/50">
                          {booking.fnBLines.map(line => (
                            <tr key={line.fnbLineId} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/10 transition-colors">
                              <td className="px-5 py-4 font-bold text-slate-700 dark:text-slate-300">{line.itemName}</td>
                              <td className="px-5 py-4 text-center font-black text-slate-500">x{line.quantity}</td>
                              <td className="px-5 py-4 text-right text-slate-400">{formatMoney(line.unitPrice)}</td>
                              <td className="px-5 py-4 text-right font-black text-orange-500">{formatMoney(line.unitPrice * line.quantity)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </section>
                )}
              </div>

              {/* Right Column (Customer & Totals) */}
              <div className="space-y-6">
                <div className="p-6 rounded-3xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
                  <h3 className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-4">Thông tin khách hàng</h3>
                  <div className="space-y-3">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center">
                        <span className="material-symbols-outlined text-sm">person</span>
                      </div>
                      <p className="text-sm font-black text-slate-800 dark:text-white capitalize">{booking.customerName || "Khách vãng lai"}</p>
                    </div>
                    {booking.customerPhone && (
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center">
                          <span className="material-symbols-outlined text-sm">call</span>
                        </div>
                        <p className="text-xs font-bold text-slate-600 dark:text-slate-400">{booking.customerPhone}</p>
                      </div>
                    )}
                  </div>
                </div>

                <div className="p-6 rounded-3xl bg-slate-900 shadow-xl shadow-slate-900/20 text-white relative overflow-hidden">
                  <div className="absolute top-0 right-0 p-4 opacity-10">
                    <span className="material-symbols-outlined text-6xl">receipt</span>
                  </div>
                  <h3 className="text-[10px] font-black text-slate-500 uppercase tracking-widest mb-6 relative">Thanh toán</h3>
                  <div className="space-y-3 relative">
                    <div className="flex justify-between text-xs">
                      <span className="text-slate-400">Tổng tiền vé</span>
                      <span className="font-bold">{formatMoney(ticketTotal)}</span>
                    </div>
                    {fnbTotal > 0 && (
                      <div className="flex justify-between text-xs">
                        <span className="text-slate-400">Dịch vụ đi kèm</span>
                        <span className="font-bold">{formatMoney(fnbTotal)}</span>
                      </div>
                    )}
                    <div className="pt-4 mt-4 border-t border-slate-800 flex justify-between items-end">
                      <span className="text-[10px] font-black text-orange-500 uppercase tracking-[0.2em]">Cần thanh toán</span>
                      <span className="text-3xl font-black tracking-tighter">{formatMoney(totalAmount)}</span>
                    </div>
                  </div>
                </div>

                <p className="text-center text-[10px] text-slate-400 font-medium px-4">
                  Đơn hàng được tạo bởi hệ thống StarCine POS Terminal vào lúc {new Date(booking.createdAt).toLocaleString()}
                </p>
              </div>

            </div>
          </div>

          {/* Footer Actions */}
          <div className="px-8 py-6 border-t border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/30 flex items-center justify-end gap-3">
            <button 
              onClick={() => handleAction('print', booking.bookingId)}
              className="flex items-center gap-2 px-6 py-3.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-xs font-black text-slate-600 dark:text-slate-300 hover:border-orange-500 hover:text-orange-500 transition-all shadow-sm"
            >
              <span className="material-symbols-outlined">print</span>
              In vé
            </button>
            <button 
              onClick={() => handleAction('cancel', booking.bookingId)}
              className="flex items-center gap-2 px-8 py-3.5 rounded-2xl bg-red-500 text-white text-xs font-black hover:bg-red-600 transition-all shadow-lg shadow-red-500/30"
            >
              <span className="material-symbols-outlined">cancel</span>
              Hủy đơn hàng
            </button>
          </div>
          
        </div>
      </div>
    );
  };

  // ══════════════════════════════════════════════════════════════════════
  //  MAIN UI
  // ══════════════════════════════════════════════════════════════════════
  return (
    <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
      
      {/* Search Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-800 dark:text-white uppercase tracking-tight flex items-center gap-3">
            <span className="w-1.5 h-8 bg-orange-500 rounded-full"></span>
            Tra cứu đơn hàng
          </h1>
          <p className="text-xs text-slate-400 font-bold uppercase tracking-widest mt-1 ml-4 shadow-sm">Staff Management Terminal</p>
        </div>
      </div>

      {/* Search Bar */}
      <form onSubmit={handleSearch} className="flex gap-3">
        <div className="relative flex-1 group">
          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-orange-500 transition-colors">search</span>
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Nhập mã đơn hàng, SĐT hoặc Email khách hàng..."
            className="w-full pl-12 pr-4 py-4 rounded-[1.5rem] bg-white dark:bg-slate-900 border-2 border-slate-100 dark:border-slate-800 text-slate-800 dark:text-white font-bold focus:outline-none focus:border-orange-500 transition-all shadow-sm"
          />
        </div>
        <button 
          type="submit" 
          disabled={searching}
          className="px-8 py-4 rounded-[1.5rem] bg-gradient-to-r from-orange-500 to-red-600 text-white font-black text-xs uppercase tracking-[0.2em] shadow-lg shadow-orange-500/30 hover:shadow-orange-500/50 disabled:opacity-40 transition-all"
        >
          {searching ? "Đang tìm..." : "Tìm kiếm"}
        </button>
      </form>

      {/* Results List */}
      <div className="grid grid-cols-1 gap-3">
        {results.length > 0 ? (
          results.map((booking) => {
            const status = STATUS_MAP[booking.status] || STATUS_MAP.PENDING;
            return (
              <div
                key={booking.bookingId}
                onClick={() => setSelectedBooking(booking)}
                className="group bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-[1.5rem] p-5 flex items-center justify-between cursor-pointer hover:border-orange-500/50 hover:shadow-xl hover:shadow-orange-500/5 transition-all"
              >
                <div className="flex items-center gap-5">
                  <div className="w-14 h-14 rounded-2xl bg-slate-50 dark:bg-slate-800 flex items-center justify-center text-slate-400 group-hover:bg-orange-500 group-hover:text-white transition-all duration-300">
                    <span className="material-symbols-outlined">confirmation_number</span>
                  </div>
                  <div>
                    <h4 className="font-black text-slate-800 dark:text-white uppercase tracking-tight">ĐƠN #{booking.bookingId}</h4>
                    <div className="flex items-center gap-2 mt-1">
                      <span className={`text-[9px] px-2 py-0.5 rounded-full font-bold border uppercase tracking-widest ${status.color}`}>
                        {status.label}
                      </span>
                      <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest">
                        {booking.tickets?.length || 0} vé • {new Date(booking.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                   <div className="text-right hidden sm:block">
                      <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest">Khách hàng</p>
                      <p className="text-xs font-black text-slate-700 dark:text-white">{booking.customerName || "Vãng lai"}</p>
                   </div>
                   <span className="material-symbols-outlined text-slate-300 group-hover:text-orange-500 group-hover:translate-x-1 transition-all">chevron_right</span>
                </div>
              </div>
            );
          })
        ) : !searching && (
          <div className="text-center py-24 bg-slate-50/50 dark:bg-slate-800/10 rounded-[2.5rem] border-2 border-dashed border-slate-200 dark:border-slate-700">
            <span className="material-symbols-outlined text-6xl text-slate-200 mb-4 block">receipt_long</span>
            <p className="text-slate-400 font-bold uppercase tracking-widest text-xs">Không tìm thấy dữ liệu đơn hàng</p>
          </div>
        )}
      </div>

      {/* Detail Modal */}
      {selectedBooking && (
        <BookingDetailModal 
          booking={selectedBooking} 
          onClose={() => setSelectedBooking(null)} 
        />
      )}

    </div>
  );
}
