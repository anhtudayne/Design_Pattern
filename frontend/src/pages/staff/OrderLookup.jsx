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
//  ORDER LOOKUP — Search & Manage Bookings
// ══════════════════════════════════════════════════════════════════════
export default function OrderLookup() {
  const [query, setQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [results, setResults] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [isDefaultList, setIsDefaultList] = useState(true); // Nhận biết đang xem list mặc định hay kết quả search
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [tickets, setTickets] = useState([]);
  const [loadingTickets, setLoadingTickets] = useState(false);

  // ── Initial Load ────────────────────────────────────────────────────
  const fetchBookings = async (searchQuery = "") => {
    setSearching(true);
    setSelectedBooking(null);
    setTickets([]);

    try {
      const res = await fetch(
        `${BASE_URL}/booking/search?query=${encodeURIComponent(searchQuery)}`,
        {
          headers: getAuthHeaders(),
        },
      );

      if (res.ok) {
        const bookingData = await res.json();
        const mappedResults = bookingData
          .map((b) => ({
            bookingId: b.bookingId,
            tickets: b.tickets || [],
            status: b.status || "PAID",
            createdAt: b.createdAt || new Date().toISOString(),
          }))
          .sort((a, b) => b.bookingId - a.bookingId); // Sắp xếp giảm dần (mới nhất lên đầu)

        setResults(mappedResults);
        setHasSearched(true);
      } else {
        setResults([]);
      }
    } catch (e) {
      console.error("Fetch error:", e);
      setResults([]);
    } finally {
      setSearching(false);
    }
  };

  useEffect(() => {
    fetchBookings(""); // Tải tất cả đơn khi vừa vào trang
  }, []);

  // ── Search handler ──────────────────────────────────────────────────
  const handleSearch = async (e) => {
    e?.preventDefault();
    if (!query.trim()) {
      setIsDefaultList(true);
      fetchBookings("");
      return;
    }

    setIsDefaultList(false);
    fetchBookings(query.trim());
  };

  // ── View booking detail ─────────────────────────────────────────────
  const handleSelectBooking = async (booking) => {
    setSelectedBooking(booking);
    setLoadingTickets(true);
    try {
      const res = await fetch(
        `${BASE_URL}/tickets/booking/${booking.bookingId}`,
        {
          headers: getAuthHeaders(),
        },
      );
      if (res.ok) {
        const data = await res.json();
        setTickets(data);
      }
    } catch (e) {
      console.error("Failed to load tickets:", e);
    } finally {
      setLoadingTickets(false);
    }
  };

  // ── Hotkeys ─────────────────────────────────────────────────────────
  useEffect(() => {
    const handler = (e) => {
      if (e.key === "Escape") {
        setSelectedBooking(null);
        setTickets([]);
      }
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, []);

  // ══════════════════════════════════════════════════════════════════════
  //  RENDER
  // ══════════════════════════════════════════════════════════════════════
  return (
    <div className="max-w-5xl mx-auto">
      {/* Search Header */}
      <div className="mb-8">
        <h1 className="text-xl font-black text-slate-800 dark:text-white uppercase tracking-tight mb-1 flex items-center gap-2">
          <span className="w-1.5 h-7 bg-orange-500 rounded-full"></span>
          Tra cứu đơn hàng
        </h1>
        <p className="text-xs text-slate-400 font-medium ml-4">
          Tìm kiếm theo mã đơn hàng (Booking ID), số điện thoại hoặc email
        </p>
      </div>

      {/* Search Bar */}
      <form onSubmit={handleSearch} className="mb-8">
        <div className="flex gap-3">
          <div className="relative flex-1">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 text-xl">
              search
            </span>
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Nhập mã đơn hàng, số điện thoại hoặc email..."
              autoFocus
              className="w-full pl-12 pr-4 py-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 text-base font-medium text-slate-800 dark:text-white placeholder-slate-400 focus:outline-none focus:border-orange-500 focus:shadow-lg focus:shadow-orange-500/10 transition-all shadow-sm"
            />
          </div>
          <button
            type="submit"
            disabled={searching}
            className="px-8 py-4 rounded-2xl bg-gradient-to-r from-orange-500 to-red-500 text-white font-black text-sm uppercase tracking-widest shadow-lg shadow-orange-500/30 hover:shadow-orange-500/50 disabled:opacity-40 transition-all flex items-center gap-2"
          >
            {searching ? (
              <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
            ) : (
              <>
                <span className="material-symbols-outlined text-lg">
                  search
                </span>
                Tìm kiếm
              </>
            )}
          </button>
        </div>
      </form>

      {/* Results / Empty State */}
      {!hasSearched && isDefaultList && results.length === 0 ? (
        <div className="text-center py-20">
          <span className="material-symbols-outlined text-7xl text-slate-200 dark:text-slate-700 mb-4 block">
            receipt_long
          </span>
          <h2 className="text-lg font-black text-slate-400 uppercase tracking-tight mb-2">
            Chưa có đơn hàng nào trong hệ thống
          </h2>
          <p className="text-sm text-slate-300 dark:text-slate-600">
            Hỗ trợ tìm kiếm theo mã đơn hàng
          </p>

          {/* Quick tips */}
          <div className="mt-10 grid grid-cols-1 sm:grid-cols-3 gap-4 max-w-2xl mx-auto">
            {[
              {
                icon: "confirmation_number",
                title: "Mã đơn hàng",
                desc: "Nhập ID booking (VD: 1, 2, 3...)",
              },
              {
                icon: "phone",
                title: "Số điện thoại",
                desc: "Tìm theo SĐT khách hàng",
              },
              { icon: "email", title: "Email", desc: "Tìm theo email đăng ký" },
            ].map((tip) => (
              <div
                key={tip.title}
                className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 p-5 shadow-sm"
              >
                <span className="material-symbols-outlined text-2xl text-orange-500 mb-2 block">
                  {tip.icon}
                </span>
                <p className="text-xs font-black text-slate-800 dark:text-white uppercase tracking-tight mb-1">
                  {tip.title}
                </p>
                <p className="text-[11px] text-slate-400">{tip.desc}</p>
              </div>
            ))}
          </div>
        </div>
      ) : results.length === 0 ? (
        <div className="text-center py-20">
          <span className="material-symbols-outlined text-6xl text-slate-300 mb-3 block">
            search_off
          </span>
          <p className="text-slate-400 font-bold">
            Không tìm thấy kết quả cho "{query}"
          </p>
          <p className="text-xs text-slate-300 mt-2">
            Hãy kiểm tra lại thông tin và thử lại
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">
            {isDefaultList
              ? `Danh sách đơn hàng gần đây (${results.length})`
              : `Tìm thấy ${results.length} kết quả`}
          </p>

          {results.map((booking) => {
            const status = STATUS_MAP[booking.status] || STATUS_MAP.PENDING;
            const isSelected = selectedBooking?.bookingId === booking.bookingId;

            return (
              <div key={booking.bookingId} className="space-y-0">
                {/* Booking Card */}
                <button
                  onClick={() => handleSelectBooking(booking)}
                  className={`w-full bg-white dark:bg-slate-900 rounded-2xl border shadow-sm hover:shadow-lg transition-all text-left p-6 ${
                    isSelected
                      ? "border-orange-500 shadow-orange-500/10"
                      : "border-slate-100 dark:border-slate-800"
                  }`}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-start gap-4">
                      <div className="w-12 h-12 rounded-xl bg-slate-50 dark:bg-slate-800 flex items-center justify-center shrink-0">
                        <span className="material-symbols-outlined text-orange-500 text-xl">
                          confirmation_number
                        </span>
                      </div>
                      <div>
                        <div className="flex items-center gap-3 mb-1">
                          <h3 className="text-sm font-black text-slate-800 dark:text-white uppercase tracking-tight">
                            Đơn #{booking.bookingId}
                          </h3>
                          <span
                            className={`px-2.5 py-0.5 rounded-lg text-[10px] font-bold border ${status.color} flex items-center gap-1`}
                          >
                            <span className="material-symbols-outlined text-xs">
                              {status.icon}
                            </span>
                            {status.label}
                          </span>
                        </div>
                        <p className="text-[11px] text-slate-400">
                          {booking.tickets?.length || 0} vé
                        </p>
                      </div>
                    </div>

                    <span className="material-symbols-outlined text-slate-300 text-xl">
                      {isSelected ? "expand_less" : "expand_more"}
                    </span>
                  </div>
                </button>

                {/* Ticket Details (Expanded) */}
                {isSelected && (
                  <div className="bg-slate-50 dark:bg-slate-800/50 rounded-b-2xl border border-t-0 border-slate-100 dark:border-slate-800 p-6 -mt-3 pt-8 space-y-4">
                    {loadingTickets ? (
                      <div className="flex justify-center py-8">
                        <div className="w-8 h-8 border-4 border-orange-500/20 border-t-orange-500 rounded-full animate-spin"></div>
                      </div>
                    ) : tickets.length === 0 ? (
                      <p className="text-center text-slate-400 text-sm py-4">
                        Không có vé nào
                      </p>
                    ) : (
                      <>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          {tickets.map((ticket) => (
                            <div
                              key={ticket.ticketId}
                              className="bg-white dark:bg-slate-900 rounded-xl border border-slate-100 dark:border-slate-800 p-4 shadow-sm"
                            >
                              <div className="flex items-center gap-2 mb-2">
                                <span className="material-symbols-outlined text-orange-500 text-base">
                                  local_activity
                                </span>
                                <span className="text-xs font-black text-slate-800 dark:text-white uppercase">
                                  Vé #{ticket.ticketId}
                                </span>
                              </div>
                              <div className="space-y-1 text-[11px] text-slate-500">
                                <p className="flex items-center gap-1.5">
                                  <span className="material-symbols-outlined text-xs text-slate-400">
                                    event_seat
                                  </span>
                                  Ghế:{" "}
                                  <span className="font-bold text-slate-700 dark:text-slate-300">
                                    {ticket.seatRow}
                                    {ticket.seatNumber}
                                  </span>
                                  <span className="text-[10px] text-slate-400">
                                    ({ticket.seatType || "Standard"})
                                  </span>
                                </p>
                                <p className="flex items-center gap-1.5">
                                  <span className="material-symbols-outlined text-xs text-slate-400">
                                    payments
                                  </span>
                                  Giá:{" "}
                                  <span className="font-bold text-orange-500">
                                    {formatMoney(ticket.price)}
                                  </span>
                                </p>
                              </div>
                            </div>
                          ))}
                        </div>

                        {/* Actions */}
                        <div className="flex flex-wrap gap-2 pt-4 border-t border-slate-200 dark:border-slate-700">
                          <button
                            onClick={async () => {
                              try {
                                const res = await fetch(
                                  `${BASE_URL}/booking/${booking.bookingId}/print`,
                                  { method: "POST", headers: getAuthHeaders() },
                                );
                                const msg = await res.text();
                                alert(
                                  res.ok
                                    ? "Chỉ thị In vé đã được gửi!"
                                    : `Lỗi: ${msg}`,
                                );
                              } catch (e) {
                                alert("Lỗi mạng");
                              }
                            }}
                            className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-xs font-bold text-slate-600 dark:text-slate-300 hover:border-orange-500 hover:text-orange-500 transition-all shadow-sm"
                          >
                            <span className="material-symbols-outlined text-base">
                              print
                            </span>
                            In lại vé
                          </button>
                          <button
                            onClick={async () => {
                              try {
                                const res = await fetch(
                                  `${BASE_URL}/booking/${booking.bookingId}/cancel`,
                                  { method: "POST", headers: getAuthHeaders() },
                                );
                                const msg = await res.text();
                                alert(res.ok ? msg : `Lỗi: ${msg}`);
                                if (res.ok) handleSearch(); // refresh
                              } catch (e) {
                                alert("Lỗi mạng");
                              }
                            }}
                            className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 text-xs font-bold text-red-500 hover:bg-red-100 transition-all shadow-sm"
                          >
                            <span className="material-symbols-outlined text-base">
                              cancel
                            </span>
                            Hủy đơn
                          </button>
                        </div>
                      </>
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
