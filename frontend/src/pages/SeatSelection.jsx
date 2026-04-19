import { useState, useEffect, useMemo, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useBooking } from '../contexts/BookingContext';
import { fetchSeatStatuses, lockSeat, unlockSeat } from '../services/bookingService';

// ─── Seat type config (matches backend SeatStatusDTO.SeatStatus) ────
const SEAT_TYPES = {
  STANDARD: { label: 'Standard', colorIdle: 'bg-slate-100 hover:bg-slate-200 text-slate-500 border border-slate-200', colorActive: 'bg-gradient-to-br from-orange-500 to-red-500 text-white shadow-lg shadow-orange-500/40 scale-110 border-none' },
  VIP:      { label: 'VIP',      colorIdle: 'bg-indigo-100 hover:bg-indigo-200 text-indigo-600 border border-indigo-200', colorActive: 'bg-gradient-to-br from-orange-500 to-red-500 text-white shadow-lg shadow-orange-500/40 scale-110 border-none' },
  COUPLE:   { label: 'Couple',   colorIdle: 'bg-rose-100 hover:bg-rose-200 text-rose-500 border border-rose-200',       colorActive: 'bg-gradient-to-br from-orange-500 to-red-500 text-white shadow-lg shadow-orange-500/40 scale-110 border-none' },
  SOLD:     { label: 'Đã bán',   colorIdle: 'bg-slate-200 text-slate-400 opacity-50 cursor-not-allowed border border-slate-200', colorActive: '' },
  PENDING:  { label: 'Đang giữ', colorIdle: 'bg-yellow-100 text-yellow-600 opacity-70 cursor-not-allowed border border-yellow-200', colorActive: '' },
};

const MAX_SEATS = 8;
const COUNTDOWN_TIME = 10 * 60;

// ─── Stepper ────────────────────────────────────────────────────────
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

// ─── Main component ─────────────────────────────────────────────────
export default function SeatSelection() {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useSelector((state) => state.auth);
  const { bookingSelection, setBookingSeats } = useBooking();
  const { movie, cinema, showtime } = bookingSelection;

  const [loading, setLoading] = useState(true);
  const [apiSeats, setApiSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [timeLeft, setTimeLeft] = useState(COUNTDOWN_TIME);
  const [toast, setToast] = useState(null);
  /** Seat đang gọi API lock/unlock để tránh double-click */
  const [lockingSeatId, setLockingSeatId] = useState(null);

  const leavingForwardRef = useRef(false);
  const timerExpiredRef = useRef(false);
  const selectedSeatsRef = useRef([]);
  const showtimeRef = useRef(showtime);

  const showToast = useCallback((msg) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  }, []);

  useEffect(() => {
    selectedSeatsRef.current = selectedSeats;
  }, [selectedSeats]);

  useEffect(() => {
    showtimeRef.current = showtime;
  }, [showtime]);

  useEffect(() => {
    timerExpiredRef.current = false;
    setTimeLeft(COUNTDOWN_TIME);
  }, [showtime?.showtimeId]);

  // Redirect if no showtime selected
  useEffect(() => {
    if (!showtime) navigate('/movies');
  }, [showtime, navigate]);

  // Khóa ghế Redis cần userId — giống bước thanh toán
  useEffect(() => {
    if (!showtime) return;
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/booking/seats' } });
    }
  }, [showtime, isAuthenticated, navigate]);

  // Fetch seats using BookingController: GET /api/booking/seats/{showtimeId}
  // Returns SeatStatusDTO[] with { seatId, seatRow, seatNumber, seatType, totalPrice, status(VACANT/SOLD/PENDING) }
  useEffect(() => {
    if (!showtime) return;
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }
    const loadSeats = async () => {
      try {
        const data = await fetchSeatStatuses(showtime.showtimeId);
        setApiSeats(data);
      } catch (err) {
        console.error('Failed to fetch seats', err);
        showToast('Không thể tải sơ đồ ghế. Vui lòng thử lại sau.');
      } finally {
        setLoading(false);
      }
    };
    loadSeats();
  }, [showtime, isAuthenticated, showToast]);

  // Countdown timer — hết giờ thì giải phóng khóa Redis rồi về danh sách phim
  useEffect(() => {
    if (timeLeft <= 0) {
      if (!timerExpiredRef.current) {
        timerExpiredRef.current = true;
        const st = showtimeRef.current;
        const seats = selectedSeatsRef.current;
        if (st?.showtimeId && seats.length > 0) {
          Promise.allSettled(
            seats.map((s) => unlockSeat(st.showtimeId, s.seatId))
          ).catch(() => {});
        }
        showToast('Hết thời gian giữ ghế. Vui lòng chọn lại.');
        setTimeout(() => navigate('/movies'), 2500);
      }
      return;
    }
    const timer = setInterval(() => setTimeLeft((prev) => prev - 1), 1000);
    return () => clearInterval(timer);
  }, [timeLeft, navigate, showToast]);

  // Rời trang không qua "Tiếp theo" → mở khóa các ghế đã giữ (TTL Redis vẫn có nhưng tránh treo chỗ user đã bỏ)
  useEffect(() => {
    return () => {
      if (leavingForwardRef.current) return;
      const st = showtimeRef.current;
      const seats = selectedSeatsRef.current;
      if (!st?.showtimeId || !seats?.length) return;
      seats.forEach((seat) => {
        unlockSeat(st.showtimeId, seat.seatId).catch(() => {});
      });
    };
  }, []);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleSeatClick = async (seat) => {
    if (seat.status !== 'VACANT') return;
    if (!showtime?.showtimeId || !user?.id) {
      navigate('/login', { state: { from: '/booking/seats' } });
      return;
    }

    const isSelected = selectedSeats.some((s) => s.seatId === seat.seatId);

    if (isSelected) {
      setLockingSeatId(seat.seatId);
      try {
        await unlockSeat(showtime.showtimeId, seat.seatId);
        setSelectedSeats((prev) => prev.filter((s) => s.seatId !== seat.seatId));
      } catch (err) {
        console.error('unlockSeat failed', err);
        showToast(err?.message || 'Không thể bỏ chọn ghế. Thử lại.');
      } finally {
        setLockingSeatId(null);
      }
      return;
    }

    if (selectedSeats.length >= MAX_SEATS) {
      showToast(`Bạn chỉ có thể chọn tối đa ${MAX_SEATS} ghế`);
      return;
    }

    setLockingSeatId(seat.seatId);
    try {
      await lockSeat(showtime.showtimeId, seat.seatId, user.id);
      setSelectedSeats((prev) => {
        if (prev.length >= MAX_SEATS) return prev;
        if (prev.some((s) => s.seatId === seat.seatId)) return prev;
        return [...prev, seat];
      });
    } catch (err) {
      console.error('lockSeat failed', err);
      showToast(err?.message || 'Ghế đã có người giữ hoặc đã bán.');
    } finally {
      setLockingSeatId(null);
    }
  };

  // Group seats by row (backend field: seatRow)
  const seatRows = useMemo(() => {
    const rows = {};
    if (!apiSeats.length) return rows;
    apiSeats.forEach(seat => {
      if (!rows[seat.seatRow]) rows[seat.seatRow] = [];
      rows[seat.seatRow].push(seat);
    });
    return Object.fromEntries(
      Object.entries(rows)
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([row, seats]) => [row, seats.sort((a, b) => a.seatNumber - b.seatNumber)])
    );
  }, [apiSeats]);

  const seatTypeNameOf = (seat) => {
    if (!seat) return 'STANDARD';
    if (typeof seat.seatType === 'string') return seat.seatType;
    return seat.seatType?.name || 'STANDARD';
  };

  const seatSurchargeOf = (seat) => {
    if (!seat) return 0;
    const s = seat.seatType?.priceSurcharge;
    const n = Number(s);
    return Number.isFinite(n) ? n : 0;
  };

  const seatPriceOf = (seat) => {
    const base = Number(showtime?.basePrice) || 0;
    const typeSurcharge = seatSurchargeOf(seat);
    // Backward-compatible: if backend still sends totalPrice, prefer it.
    const total = Number(seat?.totalPrice);
    if (Number.isFinite(total) && total > 0) return total;
    return base + typeSurcharge;
  };

  const totalPrice = useMemo(() => {
    return selectedSeats.reduce((sum, seat) => sum + seatPriceOf(seat), 0);
  }, [selectedSeats, showtime?.basePrice]);

  const handleNext = () => {
    if (selectedSeats.length === 0) {
      showToast('Vui lòng chọn ít nhất 1 ghế');
      return;
    }
    leavingForwardRef.current = true;
    setBookingSeats(selectedSeats.map(s => ({
      seatId: s.seatId,
      seatRow: s.seatRow,       // backend field name
      rowName: s.seatRow,       // alias for SnackSelection/Payment compatibility
      seatNumber: s.seatNumber,
      seatType: seatTypeNameOf(s),
      seatTypeSurcharge: seatSurchargeOf(s),
      totalPrice: seatPriceOf(s),
    })));
    navigate('/booking/snacks');
  };

  if (!showtime) return null;
  if (!isAuthenticated || !user?.id) return null;

  return (
    <main className="pt-44 pb-20 bg-slate-50 dark:bg-slate-950 min-h-screen">
      <div className="max-w-[1440px] mx-auto px-6 md:px-10">
        {toast && (
          <div className="fixed top-28 left-1/2 -translate-x-1/2 z-50 bg-slate-900 border border-slate-700 text-white px-8 py-4 rounded-3xl shadow-2xl font-black text-sm flex items-center gap-3 animate-[zoomIn_0.3s_ease-out]">
            <span className="material-symbols-outlined text-orange-500 text-xl">info</span>
            {toast}
          </div>
        )}

        <Stepper active={1} />

        {/* Timer Bar */}
        <div className="max-w-4xl mx-auto mb-10 flex items-center justify-between bg-white dark:bg-slate-900 px-8 py-4 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-xl">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-orange-500">timer</span>
            <span className="text-xs font-black uppercase text-slate-400 tracking-widest">Thời gian giữ ghế</span>
          </div>
          <span className="text-2xl font-black text-slate-800 dark:text-white font-mono">{formatTime(timeLeft)}</span>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start">
          {/* LEFT: Seat Map */}
          <div className="lg:col-span-8">
            <div className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-[2.5rem] shadow-2xl p-8 md:p-12 min-h-[600px] flex flex-col items-center justify-center relative">
              {loading ? (
                <div className="flex flex-col items-center">
                  <div className="w-12 h-12 border-4 border-orange-500/20 border-t-orange-500 rounded-full animate-spin mb-4"></div>
                  <p className="text-slate-400 font-bold uppercase text-xs tracking-widest">Đang tải sơ đồ ghế...</p>
                </div>
              ) : (
                <>
                  <div className="flex flex-col items-center mb-12 w-full">
                    <div className="w-4/5 h-3 rounded-full bg-gradient-to-b from-orange-400 to-orange-100/20 shadow-[0_4px_20px_rgba(249,115,22,0.3)] mb-4" />
                    <span className="text-[11px] font-black text-slate-400 uppercase tracking-[0.5em]">Màn Hình / Screen</span>
                  </div>

                  <div className="overflow-x-auto w-full pb-8">
                    <div className="min-w-fit mx-auto flex flex-col gap-1.5">
                      {(() => {
                        const entries = Object.entries(seatRows);
                        const maxCols = Math.max(0, ...apiSeats.map(s => s.seatNumber));
                        return entries.map(([row, seats]) => (
                          <div key={row} className="flex items-center justify-center gap-1.5">
                            <span className="w-7 text-center text-[10px] font-black text-slate-400 shrink-0">{row}</span>
                            <div className="flex items-center gap-1.5">
                              {Array.from({ length: maxCols }, (_, i) => i + 1).map(num => {
                                const seat = seats.find(s => s.seatNumber === num);
                                if (!seat) return <div key={num} className="w-9 h-9 md:w-10 md:h-10 shrink-0" />;

                                const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                                const isBusy = lockingSeatId === seat.seatId;
                                let typeKey;
                                const typeName = seatTypeNameOf(seat);
                                if (isSelected) typeKey = typeName;
                                else if (seat.status === 'VACANT') typeKey = typeName;
                                else typeKey = seat.status;

                                const colors = isSelected
                                  ? SEAT_TYPES[typeName]?.colorActive || ''
                                  : (SEAT_TYPES[typeKey]?.colorIdle || 'bg-slate-200');

                                return (
                                  <button
                                    key={seat.seatId}
                                    type="button"
                                    onClick={() => handleSeatClick(seat)}
                                    disabled={seat.status !== 'VACANT' || isBusy || (lockingSeatId !== null && !isSelected)}
                                    className={`w-9 h-9 md:w-10 md:h-10 rounded-xl flex items-center justify-center text-[10px] font-black transition-all duration-200 border-2 shrink-0 ${
                                      isSelected ? 'border-transparent' : 'border-slate-100 hover:border-slate-300'
                                    } ${colors}`}
                                  >
                                    {isBusy ? (
                                      <span className="inline-block w-3 h-3 border-2 border-orange-500 border-t-transparent rounded-full animate-spin" aria-hidden />
                                    ) : isSelected ? (
                                      <span className="material-symbols-outlined text-sm font-black">check</span>
                                    ) : seat.status === 'VACANT' ? (
                                      seat.seatNumber
                                    ) : (
                                      ''
                                    )}
                                  </button>
                                );
                              })}
                            </div>
                            <span className="w-7 text-center text-[10px] font-black text-slate-400 shrink-0">{row}</span>
                          </div>
                        ));
                      })()}
                    </div>
                  </div>

                  {/* Legend */}
                  <div className="flex flex-wrap justify-center gap-6 pt-10 mt-8 border-t-2 border-slate-50 dark:border-slate-800 w-full">
                    {Object.entries(SEAT_TYPES).map(([key, item]) => (
                      <div key={key} className="flex items-center gap-3">
                        <div className={`w-6 h-6 rounded-lg ${item.colorIdle || item.colorActive}`} />
                        <span className="text-[10px] font-black text-slate-500 uppercase tracking-wider">{item.label}</span>
                      </div>
                    ))}
                  </div>
                </>
              )}
            </div>
          </div>

          {/* RIGHT: Sidebar */}
          <div className="lg:col-span-4">
            <div className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-[2.5rem] shadow-2xl p-10 sticky top-32">
              <div className="flex gap-5 pb-8 border-b-2 border-slate-50 dark:border-slate-800">
                <div className="w-24 aspect-[2/3] rounded-2xl overflow-hidden shadow-2xl shrink-0 border border-slate-100 dark:border-slate-700">
                  <img alt={movie?.title} className="w-full h-full object-cover"
                    src={movie?.posterUrl && movie.posterUrl.startsWith('http') ? movie.posterUrl : `https://lh3.googleusercontent.com/aida-public/${movie?.posterUrl}`} />
                </div>
                <div className="space-y-2">
                  <h3 className="font-black text-slate-800 dark:text-white text-lg leading-tight uppercase tracking-tight line-clamp-2">{movie?.title}</h3>
                  <div className="flex gap-2 flex-wrap">
                    <span className="px-2.5 py-1 rounded-lg text-[10px] font-black text-white bg-red-600 shadow-lg shadow-red-500/30">{movie?.ageRating}</span>
                    <span className="text-xs text-slate-500 font-bold">{showtime?.screenType} • {movie?.durationMinutes} phút</span>
                  </div>
                </div>
              </div>

              <div className="py-8 space-y-5 border-b-2 border-slate-50 dark:border-slate-800">
                <div className="flex items-start gap-4">
                  <div className="w-10 h-10 rounded-2xl bg-orange-50 dark:bg-orange-500/10 flex items-center justify-center shrink-0 border border-orange-100 dark:border-orange-500/20">
                    <span className="material-symbols-outlined text-orange-500 text-xl font-bold">theaters</span>
                  </div>
                  <div>
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-0.5">Rạp chiếu</p>
                    <p className="text-sm font-black text-slate-800 dark:text-white">{cinema?.name}</p>
                  </div>
                </div>
                <div className="flex items-start gap-4">
                  <div className="w-10 h-10 rounded-2xl bg-cyan-50 dark:bg-cyan-500/10 flex items-center justify-center shrink-0 border border-cyan-100 dark:border-cyan-500/20">
                    <span className="material-symbols-outlined text-cyan-500 text-xl font-bold">calendar_month</span>
                  </div>
                  <div>
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-0.5">Suất chiếu</p>
                    <p className="text-sm font-black text-slate-800 dark:text-white">
                      {showtime?.startTime?.split('T')[1]?.substring(0, 5)} • {showtime?.startTime ? new Date(showtime.startTime).toLocaleDateString('vi-VN') : ''}
                    </p>
                  </div>
                </div>
              </div>

              <div className="py-8 border-b-2 border-slate-50 dark:border-slate-800">
                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-4 flex items-center gap-2">
                  <span className="material-symbols-outlined text-base font-black text-slate-400">event_seat</span>
                  Ghế đã chọn ({selectedSeats.length}/{MAX_SEATS})
                </p>
                {selectedSeats.length === 0 ? (
                  <p className="text-sm text-slate-300 font-black italic tracking-tight">Chưa có ghế nào được chọn...</p>
                ) : (
                  <div className="flex flex-wrap gap-2.5">
                    {selectedSeats.map(seat => (
                      <button key={seat.seatId} type="button" disabled={lockingSeatId === seat.seatId}
                        onClick={() => handleSeatClick(seat)}
                        className="px-3.5 py-1.5 rounded-xl text-xs font-black flex items-center gap-1.5 border-2 bg-slate-50 text-slate-700 border-slate-200 hover:bg-white transition-all disabled:opacity-60">
                        {seat.seatRow}{seat.seatNumber}
                        <span className="material-symbols-outlined text-base">close</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>

              <div className="pt-8 space-y-6">
                <div className="flex justify-between items-end">
                  <div>
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Tổng tạm tính</p>
                    <span className={`text-4xl font-black tracking-tighter ${selectedSeats.length > 0 ? 'text-slate-800 dark:text-white' : 'text-slate-200'}`}>
                      {totalPrice > 0 ? totalPrice.toLocaleString('vi-VN') + 'đ' : '—'}
                    </span>
                  </div>
                </div>
                <button onClick={handleNext}
                  className={`w-full py-5 rounded-[1.5rem] font-black text-base uppercase tracking-widest flex items-center justify-center gap-3 transition-all duration-300 ${
                    selectedSeats.length > 0
                      ? 'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-xl shadow-orange-500/30 hover:shadow-orange-500/50 hover:scale-[1.03]'
                      : 'bg-slate-200 dark:bg-slate-800 text-slate-400 cursor-not-allowed opacity-50'
                  }`}>
                  <span className="material-symbols-outlined font-black">arrow_forward</span>
                  TIẾP THEO: BẮP NƯỚC
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
