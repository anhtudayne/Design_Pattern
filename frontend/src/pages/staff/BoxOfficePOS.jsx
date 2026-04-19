import { useState, useEffect, useMemo, useRef } from 'react';
import { useSelector } from 'react-redux';
import { selectCurrentUser } from '../../store/authSlice';
import { fetchPublicShowtimes } from '../../services/showtimeService';
import { fetchCinemas } from '../../services/cinemaService';
import { fetchSeatStatuses, calculatePrice } from '../../services/bookingService';
import { fetchFnBItems } from '../../services/fnbService';
import { BASE_URL, getAuthHeaders } from '../../utils/api';
import { payMoMo, payVnpay } from '../../services/paymentService';
import { PosCommandInvoker, AddSeatCommand, RemoveSeatCommand, AddFnbCommand, RemoveFnbCommand } from '../../patterns/posCommands';

// ── Seat palette ────────────────────────────────────────────────────
const SEAT_STYLES = {
  STANDARD: { idle: 'bg-slate-100 hover:bg-slate-200 text-slate-600 border border-slate-200', active: 'bg-gradient-to-br from-orange-400 to-red-500 text-white shadow-lg shadow-orange-500/30 scale-105 border-none' },
  VIP:      { idle: 'bg-indigo-100 hover:bg-indigo-200 text-indigo-600 border border-indigo-200', active: 'bg-gradient-to-br from-orange-400 to-red-500 text-white shadow-lg shadow-orange-500/30 scale-105 border-none' },
  COUPLE:   { idle: 'bg-rose-100 hover:bg-rose-200 text-rose-500 border border-rose-200',       active: 'bg-gradient-to-br from-orange-400 to-red-500 text-white shadow-lg shadow-orange-500/30 scale-105 border-none' },
  SOLD:     { idle: 'bg-red-200/60 text-red-300 cursor-not-allowed border border-red-200/40 line-through', active: '' },
  PENDING:  { idle: 'bg-yellow-100 text-yellow-500 cursor-not-allowed border border-yellow-200', active: '' },
};

const formatMoney = (v) => new Intl.NumberFormat('vi-VN').format(v || 0) + 'đ';
const toLocalDateString = (date = new Date()) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
};

// ══════════════════════════════════════════════════════════════════════
//  BOX OFFICE POS — Main Ticket Selling Screen
// ══════════════════════════════════════════════════════════════════════
export default function BoxOfficePOS() {
  // ── Workflow State ──────────────────────────────────────────────────
  // Steps: 1=Chọn Rạp, 2=Chọn Phim, 3=Chọn Suất, 4=Chọn Ghế
  const [step, setStep] = useState(1);
  const [allShowtimes, setAllShowtimes] = useState([]);
  const [cinemas, setCinemas] = useState([]);
  const [loading, setLoading] = useState(true);

  // Step 1: Cinema
  const [selectedCinema, setSelectedCinema] = useState(null);
  // Step 2: Movie
  const [selectedMovie, setSelectedMovie] = useState(null);
  // Step 3: Showtime
  const [selectedShowtime, setSelectedShowtime] = useState(null);
  // Step 4: Seats
  const [seats, setSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [seatsLoading, setSeatsLoading] = useState(false);
  const [seatsError, setSeatsError] = useState(null);

  // Cart
  const [cartFnb, setCartFnb] = useState([]);
  const [fnbItems, setFnbItems] = useState([]);
  const [promoCode, setPromoCode] = useState('');
  const [priceBreakdown, setPriceBreakdown] = useState(null);
  const [showFnbPanel, setShowFnbPanel] = useState(false);
  const [paymentProcessing, setPaymentProcessing] = useState(false);
  const [momoProcessing, setMomoProcessing] = useState(false);
  const [vnpayProcessing, setVnpayProcessing] = useState(false);
  const [paymentSuccess, setPaymentSuccess] = useState(false);
  const [lastAction, setLastAction] = useState(null); // last command label for feedback
  const [toastVisible, setToastVisible] = useState(false);
  const [canUndo, setCanUndo] = useState(false);
  const [canRedo, setCanRedo] = useState(false);

  // Command Pattern Invoker
  const invokerRef = useRef(new PosCommandInvoker());
  const currentUser = useSelector(selectCurrentUser);

  // Helper to run + sync undo/redo state after any command
  const runCmd = (cmd) => {
    const label = invokerRef.current.execute(cmd);
    setLastAction(label);
    setToastVisible(true);
    setCanUndo(invokerRef.current.canUndo());
    setCanRedo(invokerRef.current.canRedo());
  };
  const handleUndo = () => {
    const label = invokerRef.current.undo();
    if (label) { setLastAction(label); setToastVisible(true); }
    setCanUndo(invokerRef.current.canUndo());
    setCanRedo(invokerRef.current.canRedo());
  };
  const handleRedo = () => {
    const label = invokerRef.current.redo();
    if (label) { setLastAction(label); setToastVisible(true); }
    setCanUndo(invokerRef.current.canUndo());
    setCanRedo(invokerRef.current.canRedo());
  };

  useEffect(() => {
    if (toastVisible) {
      const timer = setTimeout(() => setToastVisible(false), 5000);
      return () => clearTimeout(timer);
    }
  }, [toastVisible, lastAction]);

  // ── Load initial data (F&B theo rạp đã chọn — xem useEffect bên dưới) ──
  useEffect(() => {
    const load = async () => {
      try {
        const [showtimeData, cinemaData, movieData, roomData] = await Promise.all([
          fetchPublicShowtimes({}),
          fetchCinemas(),
          fetch(`${BASE_URL}/movies`, { headers: getAuthHeaders() }).then(r => (r.ok ? r.json() : [])),
          fetch(`${BASE_URL}/rooms`, { headers: getAuthHeaders() }).then(r => (r.ok ? r.json() : [])),
        ]);

        const movieById = new Map((movieData || []).map(m => [m.movieId, m]));
        const roomById = new Map((roomData || []).map(r => [r.roomId, r]));
        const cinemaById = new Map((cinemaData || []).map(c => [c.cinemaId, c]));

        const enrichedShowtimes = (showtimeData || []).map(st => {
          const movie = movieById.get(st.movieId);
          const room = roomById.get(st.roomId);
          const roomCinemaId = room?.cinemaId ?? room?.cinema?.cinemaId ?? null;
          const cinema = roomCinemaId != null ? cinemaById.get(roomCinemaId) : null;
          const showtimeId = st.showtimeId ?? st.id ?? null;

          return {
            ...st,
            showtimeId,
            cinemaId: st.cinemaId ?? roomCinemaId,
            cinemaName: st.cinemaName ?? cinema?.name ?? room?.cinemaName,
            roomName: st.roomName ?? room?.name,
            screenType: st.screenType ?? room?.screenType,
            movieTitle: st.movieTitle ?? movie?.title,
            moviePosterUrl: st.moviePosterUrl ?? movie?.posterUrl,
            movieAgeRating: st.movieAgeRating ?? movie?.ageRating,
            movieDurationMinutes: st.movieDurationMinutes ?? movie?.durationMinutes,
          };
        }).filter(st => Number.isFinite(Number(st.showtimeId)) && Number.isFinite(Number(st.movieId)));

        setAllShowtimes(enrichedShowtimes);
        setCinemas(cinemaData);
      } catch(e) {
        console.error('Failed to load data', e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  useEffect(() => {
    if (!selectedCinema?.cinemaId) {
      setFnbItems([]);
      return;
    }
    setCartFnb([]);
    fetchFnBItems(selectedCinema.cinemaId)
      .then(setFnbItems)
      .catch((e) => console.error('Failed to load F&B for cinema', e));
  }, [selectedCinema]);

  // ── Derive: movies from showtimes (today only, filtered by cinema) ──
  const todayStr = toLocalDateString();

  const movies = useMemo(() => {
    if (!selectedCinema) return [];
    const map = new Map();
    allShowtimes.forEach(st => {
      const stDate = st.startTime?.split('T')[0];
      if (stDate !== todayStr) return;
      if (st.cinemaId !== selectedCinema.cinemaId) return;
      if (!map.has(st.movieId)) {
        map.set(st.movieId, {
          movieId: st.movieId,
          title: st.movieTitle,
          posterUrl: st.moviePosterUrl,
          ageRating: st.movieAgeRating,
          durationMinutes: st.movieDurationMinutes,
        });
      }
    });
    return Array.from(map.values());
  }, [allShowtimes, selectedCinema, todayStr]);

  // ── Derive: showtimes for selected movie + cinema ───────────────────
  const movieShowtimes = useMemo(() => {
    if (!selectedMovie || !selectedCinema) return [];
    const now = Date.now();
    return allShowtimes
      .filter(st =>
        st.movieId === selectedMovie.movieId &&
        st.cinemaId === selectedCinema.cinemaId &&
        st.startTime?.split('T')[0] === todayStr &&
        new Date(st.startTime).getTime() >= now
      )
      .sort((a, b) => a.startTime.localeCompare(b.startTime));
  }, [allShowtimes, selectedMovie, selectedCinema, todayStr]);

  // ── Load seats when showtime is selected ────────────────────────────
  useEffect(() => {
    if (!selectedShowtime) return;
    const loadSeats = async () => {
      const normalizedShowtimeId = Number(selectedShowtime.showtimeId);
      if (!Number.isFinite(normalizedShowtimeId)) {
        setSeats([]);
        setSeatsError('Không tìm thấy mã suất chiếu hợp lệ.');
        return;
      }
      setSeatsLoading(true);
      setSeatsError(null);
      try {
        const data = await fetchSeatStatuses(normalizedShowtimeId);
        setSeats(data);
      } catch(e) {
        console.error('Failed to load seats', e);
        setSeatsError(e.message);
      } finally {
        setSeatsLoading(false);
      }
    };
    loadSeats();
  }, [selectedShowtime]);

  // ── Calculate price when seats change ───────────────────────────────
  useEffect(() => {
    if (selectedSeats.length === 0 || !selectedShowtime) {
      setPriceBreakdown(null);
      return;
    }
    const calc = async () => {
      try {
        const result = await calculatePrice({
          showtimeId: selectedShowtime.showtimeId,
          seatIds: selectedSeats.map(s => s.seatId),
          fnbs: cartFnb.map(f => ({ itemId: f.fnbItemId, quantity: f.quantity })),
          promoCode: promoCode || null,
        });
        setPriceBreakdown(result);
      } catch(e) {
        console.error('Price calc error', e);
      }
    };
    calc();
  }, [selectedSeats, selectedShowtime, cartFnb, promoCode]);

  // ── Handlers ────────────────────────────────────────────────────────
  const handleSelectCinema = (cinema) => {
    setSelectedCinema(cinema);
    setSelectedMovie(null);
    setSelectedShowtime(null);
    setSelectedSeats([]);
    setSeats([]);
    setStep(2);
  };

  const handleSelectMovie = (movie) => {
    setSelectedMovie(movie);
    setSelectedShowtime(null);
    setSelectedSeats([]);
    setSeats([]);
    setStep(3);
  };

  const handleSelectShowtime = (st) => {
    const normalizedShowtimeId = Number(st?.showtimeId);
    if (!Number.isFinite(normalizedShowtimeId)) {
      setSeatsError('Suất chiếu không hợp lệ, vui lòng chọn lại.');
      return;
    }
    if (st?.startTime && new Date(st.startTime).getTime() < Date.now()) {
      setSeats([]);
      setSelectedSeats([]);
      setSeatsError('Suất chiếu đã bắt đầu/kết thúc, vui lòng chọn suất khác.');
      return;
    }
    setSelectedShowtime({ ...st, showtimeId: normalizedShowtimeId });
    setSelectedSeats([]);
    setStep(4);
  };

  // Command Pattern: Toggle seat (Add or Remove)
  const handleToggleSeat = (seat) => {
    if (seat.status === 'SOLD' || seat.status === 'PENDING') return;
    const isSelected = selectedSeats.find(s => s.seatId === seat.seatId);
    if (isSelected) {
      runCmd(new RemoveSeatCommand(seat, setSelectedSeats));
    } else {
      if (selectedSeats.length >= 10) return;
      runCmd(new AddSeatCommand(seat, setSelectedSeats));
    }
  };

  // Command Pattern: Add F&B item
  const handleAddFnb = (item) => {
    const inCart = cartFnb.find(f => f.fnbItemId === item.fnbItemId);
    const currentQty = inCart?.quantity || 0;
    const stock = Math.max(0, Number(item.stockQuantity ?? 0));
    if (currentQty >= stock) return;
    runCmd(new AddFnbCommand(item, setCartFnb));
  };

  // Command Pattern: Remove F&B item
  const handleRemoveFnb = (itemId) => {
    const item = cartFnb.find(f => f.fnbItemId === itemId);
    if (!item) return;
    runCmd(new RemoveFnbCommand(itemId, item.name, setCartFnb));
  };

  /** Đồng bộ phương thức với khách web: MoMo → Tiền mặt → VNPay */

  const checkoutPayload = () => ({
    userId: currentUser?.id,
    showtimeId: selectedShowtime.showtimeId,
    seatIds: selectedSeats.map(s => s.seatId),
    fnbs: cartFnb.map(f => ({ itemId: f.fnbItemId, quantity: f.quantity })),
    promoCode: Number(priceBreakdown?.discountAmount || 0) > 0 ? (promoCode || null) : null,
  });

  /** MoMo: tạo đơn PENDING + link thanh toán (backend), mở tab mới cho khách quét / thanh toán */
  const handleMomoPayment = async () => {
    if (!selectedShowtime || selectedSeats.length === 0 || !currentUser?.id) return;
    setMomoProcessing(true);
    try {
      const payUrl = await payMoMo({ ...checkoutPayload(), paymentMethod: 'MOMO' });
      window.open(payUrl, '_blank', 'noopener,noreferrer');
      alert(
        'Đã mở trang thanh toán MoMo trong tab mới. Sau khi khách thanh toán thành công, hệ thống sẽ cập nhật vé theo cổng MoMo.'
      );
    } catch (e) {
      alert(e.message || 'Không tạo được link MoMo.');
    } finally {
      setMomoProcessing(false);
    }
  };

  /** Tiền mặt — /api/payment/staff/cash-checkout */
  const handleCashPayment = async () => {
    if (!selectedShowtime || selectedSeats.length === 0) return;
    setPaymentProcessing(true);
    try {
      const res = await fetch(`${BASE_URL}/payment/staff/cash-checkout`, {
        method: 'POST',
        headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
        body: JSON.stringify(checkoutPayload()),
      });
      if (!res.ok) {
        const err = await res.text();
        alert('Lỗi thanh toán: ' + err);
        setPaymentProcessing(false);
        return;
      }
      setPaymentProcessing(false);
      setPaymentSuccess(true);
      invokerRef.current.reset();
      setCanUndo(false); setCanRedo(false);
      setTimeout(() => resetAll(), 3000);
    } catch (e) {
      alert('Lỗi mạng: ' + e.message);
      setPaymentProcessing(false);
    }
  };

  const handleVnpayPayment = async () => {
    if (!selectedShowtime || selectedSeats.length === 0 || !currentUser?.id) return;
    setVnpayProcessing(true);
    try {
      const payUrl = await payVnpay({ ...checkoutPayload(), paymentMethod: 'VNPAY' });
      window.open(payUrl, '_blank', 'noopener,noreferrer');
      alert(
        'Đã mở cổng VNPay trong tab mới. Nếu backend chưa bật VNPay (vnpay.enabled), hãy cấu hình hoặc chọn tiền mặt.'
      );
    } catch (e) {
      alert(e.message || 'Không tạo được link VNPay.');
    } finally {
      setVnpayProcessing(false);
    }
  };

  const resetAll = () => {
    setStep(1);
    setSelectedCinema(null);
    setSelectedMovie(null);
    setSelectedShowtime(null);
    setSelectedSeats([]);
    setSeats([]);
    setCartFnb([]);
    setPromoCode('');
    setPriceBreakdown(null);
    setShowFnbPanel(false);
    setPaymentSuccess(false);
    setLastAction(null);
    setToastVisible(false);
    invokerRef.current.reset();
    setCanUndo(false);
    setCanRedo(false);
  };

  // ── Hotkeys: Escape = reset, Ctrl+Z = undo, Ctrl+Y = redo ──────────
  useEffect(() => {
    const handler = (e) => {
      // Fix: Dùng !document.activeElement.matches('input, textarea') để tránh nhận nhầm khi đang gõ text
      const isInputFocused = document.activeElement && ['INPUT', 'TEXTAREA'].includes(document.activeElement.tagName);
      if (isInputFocused) return;
      
      if (e.key === 'Escape') { resetAll(); return; }
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') { e.preventDefault(); handleUndo(); }
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'y') { e.preventDefault(); handleRedo(); }
    };
    window.addEventListener('keydown', handler);
    return () => window.removeEventListener('keydown', handler);
  }); // Bỏ dependency rỗng để closure cập nhật function handleUndo mới nhất, giúp Hotkey không bị kẹt data cũ.

  // ── Seat grid builder ───────────────────────────────────────────────
  const seatGrid = useMemo(() => {
    if (seats.length === 0) return [];
    const rows = {};
    seats.forEach(s => {
      if (!rows[s.seatRow]) rows[s.seatRow] = [];
      rows[s.seatRow].push(s);
    });
    return Object.entries(rows)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([row, s]) => ({ row, seats: s.sort((a, b) => a.seatNumber - b.seatNumber) }));
  }, [seats]);

  // ══════════════════════════════════════════════════════════════════════
  //  RENDER
  // ══════════════════════════════════════════════════════════════════════
  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center h-[70vh]">
        <div className="w-12 h-12 border-4 border-orange-500/20 border-t-orange-500 rounded-full animate-spin"></div>
        <p className="mt-4 text-slate-400 font-bold text-xs uppercase tracking-widest">Đang tải dữ liệu POS...</p>
      </div>
    );
  }

  // ── Payment Success Overlay ─────────────────────────────────────────
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
          LEFT: WORKSPACE (70%)
      ════════════════════════════════════════════════════════════════ */}
      <div className="flex-[7] flex flex-col min-w-0 overflow-y-auto pr-1">

        {/* Floating Toast Notification cho Undo/Redo (Kiểu Gmail) */}
        {toastVisible && lastAction && (
          <div className="fixed bottom-10 left-1/2 -translate-x-1/2 z-50 flex animate-[bounce-up_0.3s_ease-out]">
            <div className="bg-slate-800 text-white px-5 py-3.5 rounded-full shadow-2xl flex items-center gap-6 border border-slate-700/50">
              <span className="text-sm font-medium">{lastAction}</span>
              <div className="flex items-center gap-4 border-l border-slate-600 pl-4">
                {canUndo && (
                  <button onClick={handleUndo} className="text-orange-400 hover:text-orange-300 font-bold text-sm tracking-wide transition-colors uppercase">
                    Hoàn tác (Ctrl+Z)
                  </button>
                )}
                <button onClick={() => setToastVisible(false)} className="text-slate-400 hover:text-white transition-colors">
                  <span className="material-symbols-outlined text-lg block">close</span>
                </button>
              </div>
            </div>
            <style>{`@keyframes bounce-up { 0% { transform: translate(-50%, 20px); opacity: 0; } 100% { transform: translate(-50%, 0); opacity: 1; } }`}</style>
          </div>
        )}

        {/* Breadcrumb / Step Indicator */}
        <div className="flex items-center gap-2 mb-4 text-xs font-bold text-slate-400 uppercase tracking-widest flex-wrap">
          <button onClick={resetAll} className={`transition-colors ${step >= 1 ? 'text-orange-500' : ''}`}>
            <span className="material-symbols-outlined text-sm align-middle mr-1">store</span>Chọn Rạp
          </button>
          {step >= 2 && (
            <>
              <span className="material-symbols-outlined text-sm">chevron_right</span>
              <button onClick={() => { setStep(2); setSelectedMovie(null); setSelectedShowtime(null); setSelectedSeats([]); setSeats([]); }} className={`transition-colors ${step >= 2 ? 'text-orange-500' : ''}`}>
                <span className="material-symbols-outlined text-sm align-middle mr-1">movie</span>Chọn Phim
              </button>
            </>
          )}
          {step >= 3 && (
            <>
              <span className="material-symbols-outlined text-sm">chevron_right</span>
              <button onClick={() => { setStep(3); setSelectedShowtime(null); setSelectedSeats([]); setSeats([]); }} className={`transition-colors ${step >= 3 ? 'text-orange-500' : ''}`}>
                <span className="material-symbols-outlined text-sm align-middle mr-1">schedule</span>Suất Chiếu
              </button>
            </>
          )}
          {step >= 4 && (
            <>
              <span className="material-symbols-outlined text-sm">chevron_right</span>
              <span className="text-orange-500">
                <span className="material-symbols-outlined text-sm align-middle mr-1">event_seat</span>Chọn Ghế
              </span>
            </>
          )}
        </div>

        {/* ────────────────────────────────────────────────────────
            STEP 1: Cinema Selection
        ──────────────────────────────────────────────────────── */}
        {step === 1 && (
          <div>
            <h2 className="text-lg font-black text-slate-800 dark:text-white uppercase tracking-tight mb-4 flex items-center gap-2">
              <span className="w-1.5 h-6 bg-orange-500 rounded-full"></span>
              Chọn rạp chiếu phim
              <span className="text-xs font-medium text-slate-400 normal-case tracking-normal ml-1">({cinemas.length} rạp)</span>
            </h2>

            {cinemas.length === 0 ? (
              <div className="text-center py-20">
                <span className="material-symbols-outlined text-5xl text-slate-300 mb-3 block">store_off</span>
                <p className="text-slate-400 font-bold">Không tìm thấy rạp nào</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                {cinemas.map(cinema => {
                  // Count today's showtimes for this cinema
                  const todayShowtimeCount = allShowtimes.filter(
                    st => st.cinemaId === cinema.cinemaId && st.startTime?.split('T')[0] === todayStr
                  ).length;

                  return (
                    <button
                      key={cinema.cinemaId}
                      onClick={() => handleSelectCinema(cinema)}
                      className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm hover:shadow-xl hover:border-orange-500/50 hover:-translate-y-1 transition-all text-left p-6 group"
                    >
                      <div className="flex items-start gap-4">
                        <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-orange-500 to-red-500 flex items-center justify-center shadow-md shadow-orange-500/20 shrink-0 group-hover:scale-110 transition-transform">
                          <span className="material-symbols-outlined text-white text-xl">movie</span>
                        </div>
                        <div className="min-w-0">
                          <h3 className="text-sm font-black text-slate-800 dark:text-white uppercase tracking-tight leading-tight group-hover:text-orange-500 transition-colors">{cinema.name}</h3>
                          <p className="text-[11px] text-slate-400 font-medium mt-1 flex items-start gap-1 leading-snug">
                            <span className="material-symbols-outlined text-xs shrink-0 mt-0.5">location_on</span>
                            <span className="line-clamp-2">{cinema.address}</span>
                          </p>
                          <div className="flex items-center gap-3 mt-3">
                            <span className="text-[10px] font-bold text-slate-500 dark:text-slate-400 bg-slate-50 dark:bg-slate-800 px-2.5 py-1 rounded-lg">
                              {todayShowtimeCount} suất hôm nay
                            </span>
                            {cinema.hotline && (
                              <span className="text-[10px] font-bold text-slate-500 dark:text-slate-400 bg-slate-50 dark:bg-slate-800 px-2.5 py-1 rounded-lg flex items-center gap-1">
                                <span className="material-symbols-outlined text-[10px]">call</span>
                                {cinema.hotline}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* ────────────────────────────────────────────────────────
            STEP 2: Movie Grid
        ──────────────────────────────────────────────────────── */}
        {step === 2 && selectedCinema && (
          <div>
            <h2 className="text-lg font-black text-slate-800 dark:text-white uppercase tracking-tight mb-4 flex items-center gap-2">
              <span className="w-1.5 h-6 bg-orange-500 rounded-full"></span>
              Phim đang chiếu — {selectedCinema.name}
              <span className="text-xs font-medium text-slate-400 normal-case tracking-normal ml-1">({movies.length} phim)</span>
            </h2>

            {movies.length === 0 ? (
              <div className="text-center py-20">
                <span className="material-symbols-outlined text-5xl text-slate-300 mb-3 block">movie_off</span>
                <p className="text-slate-400 font-bold">Không có phim nào chiếu hôm nay tại rạp này</p>
              </div>
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 xl:grid-cols-6 gap-3">
                {movies.map(movie => (
                  <button
                    key={movie.movieId}
                    onClick={() => handleSelectMovie(movie)}
                    className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all text-left group overflow-hidden"
                  >
                    <div className="aspect-[2/3] overflow-hidden rounded-t-2xl relative">
                      <img
                        alt={movie.title}
                        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                        src={movie.posterUrl?.startsWith('http') ? movie.posterUrl : `https://lh3.googleusercontent.com/aida-public/${movie.posterUrl}`}
                      />
                      {movie.ageRating && (
                        <span className="absolute top-2 left-2 px-2 py-0.5 rounded text-[9px] font-black text-white bg-red-600 uppercase">{movie.ageRating}</span>
                      )}
                    </div>
                    <div className="p-3">
                      <h3 className="text-xs font-black text-slate-800 dark:text-white uppercase tracking-tight leading-tight line-clamp-2">{movie.title}</h3>
                      <p className="text-[10px] text-slate-400 font-medium mt-1">{movie.durationMinutes} phút</p>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* ────────────────────────────────────────────────────────
            STEP 3: Showtimes
        ──────────────────────────────────────────────────────── */}
        {step === 3 && selectedMovie && (
          <div>
            <div className="flex items-center gap-4 mb-5">
              <img
                alt={selectedMovie.title}
                className="w-16 h-24 rounded-xl object-cover shadow-md"
                src={selectedMovie.posterUrl?.startsWith('http') ? selectedMovie.posterUrl : `https://lh3.googleusercontent.com/aida-public/${selectedMovie.posterUrl}`}
              />
              <div>
                <h2 className="text-lg font-black text-slate-800 dark:text-white uppercase tracking-tight">{selectedMovie.title}</h2>
                <p className="text-xs text-slate-400 font-medium">{selectedMovie.durationMinutes} phút • {selectedMovie.ageRating}</p>
              </div>
            </div>

            <h3 className="text-sm font-black text-slate-600 dark:text-slate-300 uppercase tracking-widest mb-3 flex items-center gap-2">
              <span className="w-1.5 h-5 bg-cyan-500 rounded-full"></span>
              Chọn suất chiếu
            </h3>

            {movieShowtimes.length === 0 ? (
              <div className="text-center py-16 text-slate-400 font-bold">Không có suất chiếu hôm nay cho phim này</div>
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                {movieShowtimes.map(st => {
                  const time = st.startTime?.split('T')[1]?.substring(0, 5);
                  return (
                    <button
                      key={st.showtimeId}
                      onClick={() => handleSelectShowtime(st)}
                      className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl p-5 shadow-sm hover:shadow-lg hover:border-orange-500/50 hover:-translate-y-1 transition-all text-center group"
                    >
                      <p className="text-2xl font-black text-slate-800 dark:text-white tracking-tight group-hover:text-orange-500 transition-colors">{time}</p>
                      <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest mt-1">{st.roomName || `Phòng ${st.roomId}`}</p>
                      <p className="text-[10px] text-slate-400 mt-0.5">{st.screenType || '2D'}</p>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* ────────────────────────────────────────────────────────
            STEP 4: Seat Map
        ──────────────────────────────────────────────────────── */}
        {step === 4 && selectedShowtime && (
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <h2 className="text-sm font-black text-slate-800 dark:text-white uppercase tracking-tight flex items-center gap-2">
                  <span className="w-1.5 h-5 bg-orange-500 rounded-full"></span>
                  Sơ đồ ghế — {selectedShowtime.roomName || `Phòng ${selectedShowtime.roomId}`}
                </h2>
                <span className="text-xs text-slate-400 font-medium">
                  ({selectedSeats.length}/10 ghế đã chọn)
                </span>
              </div>
              {/* Legend */}
              <div className="hidden md:flex items-center gap-4 text-[10px] font-bold text-slate-400 uppercase">
                <span className="flex items-center gap-1"><span className="w-4 h-4 rounded bg-slate-100 border border-slate-200"></span> Trống</span>
                <span className="flex items-center gap-1"><span className="w-4 h-4 rounded bg-gradient-to-br from-orange-400 to-red-500"></span> Đang chọn</span>
                <span className="flex items-center gap-1"><span className="w-4 h-4 rounded bg-red-200/60"></span> Đã bán</span>
                <span className="flex items-center gap-1"><span className="w-4 h-4 rounded bg-indigo-100 border border-indigo-200"></span> VIP</span>
              </div>
            </div>

            {/* Screen indicator */}
            <div className="w-3/4 mx-auto h-2 bg-gradient-to-r from-transparent via-orange-400 to-transparent rounded-full mb-1 opacity-60"></div>
            <p className="text-center text-[9px] text-slate-400 font-bold uppercase tracking-[0.3em] mb-6">Màn hình</p>

            {seatsLoading ? (
              <div className="flex justify-center py-16">
                <div className="w-10 h-10 border-4 border-orange-500/20 border-t-orange-500 rounded-full animate-spin"></div>
              </div>
            ) : seatsError ? (
              <div className="text-center py-16">
                <span className="material-symbols-outlined text-4xl text-red-400 mb-2">error</span>
                <p className="text-red-500 font-bold">{seatsError}</p>
                <button 
                  onClick={() => { setSelectedShowtime({...selectedShowtime}); }}
                  className="mt-4 px-4 py-2 rounded-lg bg-slate-100 text-slate-600 font-bold text-xs hover:bg-slate-200"
                >
                  Thử lại
                </button>
              </div>
            ) : (
              <div className="space-y-1.5 max-w-3xl mx-auto">
                {seatGrid.map(({ row, seats: rowSeats }) => (
                  <div key={row} className="flex items-center gap-1.5">
                    <span className="w-6 text-center text-xs font-black text-slate-400">{row}</span>
                    <div className="flex-1 flex justify-center gap-1">
                      {rowSeats.map(seat => {
                        const isSelected = selectedSeats.some(s => s.seatId === seat.seatId);
                        const seatType = seat.status === 'SOLD' ? 'SOLD' : seat.status === 'PENDING' ? 'PENDING' : (seat.seatType || 'STANDARD');
                        const style = SEAT_STYLES[seatType] || SEAT_STYLES.STANDARD;
                        const canSelect = seat.status !== 'SOLD' && seat.status !== 'PENDING';

                        return (
                          <button
                            key={seat.seatId}
                            onClick={() => handleToggleSeat(seat)}
                            disabled={!canSelect}
                            title={`${row}${seat.seatNumber} • ${seatType}${seat.totalPrice ? ' • ' + formatMoney(seat.totalPrice) : ''}`}
                            className={`w-8 h-8 rounded-lg text-[10px] font-bold transition-all duration-150 ${
                              isSelected ? style.active : style.idle
                            }`}
                          >
                            {seat.seatNumber}
                          </button>
                        );
                      })}
                    </div>
                    <span className="w-6 text-center text-xs font-black text-slate-400">{row}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* ════════════════════════════════════════════════════════════════
          RIGHT: CART PANEL (30%)
      ════════════════════════════════════════════════════════════════ */}
      <div className="flex-[3] flex flex-col min-w-[280px] max-w-[380px] bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl shadow-lg overflow-hidden">
        {/* Cart Header */}
        <div className="px-5 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center bg-slate-50/50">
          <h3 className="text-sm font-black text-slate-800 dark:text-white uppercase tracking-wider flex items-center gap-2">
            <span className="material-symbols-outlined text-orange-500 text-lg">shopping_cart</span>
            Đơn hàng
          </h3>
          <div className="flex bg-slate-200/50 dark:bg-slate-800 p-1 rounded-xl">
            <button onClick={handleUndo} disabled={!canUndo} className="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-white hover:text-orange-500 hover:shadow-sm transition-all disabled:opacity-30 disabled:hover:bg-transparent" title="Hoàn tác (Ctrl+Z)">
              <span className="material-symbols-outlined text-sm font-bold">undo</span>
            </button>
            <button onClick={handleRedo} disabled={!canRedo} className="w-8 h-8 rounded-lg flex items-center justify-center text-slate-500 hover:bg-white hover:text-orange-500 hover:shadow-sm transition-all disabled:opacity-30 disabled:hover:bg-transparent" title="Làm lại (Ctrl+Y)">
              <span className="material-symbols-outlined text-sm font-bold">redo</span>
            </button>
          </div>
        </div>

        {/* Cart Body */}
        <div className="flex-1 overflow-y-auto p-5 space-y-4">
          {/* Cinema info */}
          {selectedCinema && (
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-3 border border-slate-100 dark:border-slate-800">
              <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Rạp</p>
              <p className="text-sm font-bold text-slate-800 dark:text-white leading-tight">{selectedCinema.name}</p>
            </div>
          )}

          {/* Movie info */}
          {selectedMovie && (
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-3 border border-slate-100 dark:border-slate-800">
              <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Phim</p>
              <p className="text-sm font-bold text-slate-800 dark:text-white leading-tight">{selectedMovie.title}</p>
            </div>
          )}

          {/* Showtime info */}
          {selectedShowtime && (
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-3 border border-slate-100 dark:border-slate-800">
              <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1">Suất chiếu</p>
              <p className="text-sm font-bold text-orange-500">
                {selectedShowtime.startTime?.split('T')[1]?.substring(0, 5)} — {selectedShowtime.roomName || `Phòng ${selectedShowtime.roomId}`}
              </p>
            </div>
          )}

          {/* Selected seats */}
          {selectedSeats.length > 0 && (
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-3 border border-slate-100 dark:border-slate-800">
              <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-2">Ghế ({selectedSeats.length})</p>
              <div className="flex flex-wrap gap-1.5">
                {selectedSeats.map(s => (
                  <span key={s.seatId} className="px-2.5 py-1 rounded-lg bg-orange-100 dark:bg-orange-500/10 text-orange-600 text-xs font-bold">
                    {s.seatRow}{s.seatNumber}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* F&B */}
          {cartFnb.length > 0 && (
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-3 border border-slate-100 dark:border-slate-800">
              <p className="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-2">F&B</p>
              {cartFnb.map(f => (
                <div key={f.fnbItemId} className="flex items-center justify-between py-1 text-xs">
                  <span className="font-medium text-slate-700 dark:text-slate-300 truncate max-w-[120px]">{f.name}</span>
                  <div className="flex items-center gap-2">
                    <button onClick={() => handleRemoveFnb(f.fnbItemId)} className="w-5 h-5 rounded bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 flex items-center justify-center text-sm font-bold hover:bg-red-100 hover:text-red-500 transition-colors">−</button>
                    <span className="font-bold text-slate-800 dark:text-white w-4 text-center">{f.quantity}</span>
                    <button
                      onClick={() => handleAddFnb(f)}
                      disabled={f.quantity >= Math.max(0, Number(f.stockQuantity ?? 0))}
                      className="w-5 h-5 rounded bg-slate-200 dark:bg-slate-700 text-slate-600 dark:text-slate-300 flex items-center justify-center text-sm font-bold hover:bg-green-100 hover:text-green-500 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
                    >+</button>
                    <span className="font-bold text-slate-500 w-16 text-right">{formatMoney(f.price * f.quantity)}</span>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Add F&B button */}
          {selectedSeats.length > 0 && (
            <button
              onClick={() => setShowFnbPanel(!showFnbPanel)}
              className="w-full py-2.5 rounded-xl border-2 border-dashed border-slate-200 dark:border-slate-700 text-slate-400 text-xs font-bold uppercase tracking-widest hover:border-orange-500 hover:text-orange-500 transition-colors flex items-center justify-center gap-2"
            >
              <span className="material-symbols-outlined text-base">add_circle</span>
              {showFnbPanel ? 'Đóng F&B' : 'Thêm Bắp/Nước'}
            </button>
          )}

          {/* F&B Quick Panel */}
          {showFnbPanel && (
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-xl p-3 border border-slate-100 dark:border-slate-800 space-y-2 max-h-48 overflow-y-auto">
              {fnbItems.map(item => (
                <button
                  key={item.fnbItemId}
                  onClick={() => handleAddFnb(item)}
                  disabled={Math.max(0, Number(item.stockQuantity ?? 0)) <= 0}
                  className="w-full flex items-center justify-between p-2 rounded-lg hover:bg-white dark:hover:bg-slate-800 transition-colors text-left"
                >
                  <div className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-orange-500 text-base">fastfood</span>
                    <div className="min-w-0">
                      <span className="text-xs font-medium text-slate-700 dark:text-slate-300 truncate max-w-[120px] block">{item.name}</span>
                      <span className={`text-[10px] font-bold ${(item.stockQuantity || 0) > 0 ? 'text-emerald-600' : 'text-red-500'}`}>
                        {(item.stockQuantity || 0) > 0 ? `Còn ${item.stockQuantity}` : 'Hết hàng'}
                      </span>
                    </div>
                  </div>
                  <span className="text-xs font-bold text-orange-500">{formatMoney(item.price)}</span>
                </button>
              ))}
            </div>
          )}

          {/* Promo code */}
          {selectedSeats.length > 0 && (
            <div className="flex gap-2">
              <input
                type="text"
                value={promoCode}
                onChange={e => setPromoCode(e.target.value)}
                placeholder="Mã khuyến mãi..."
                className="flex-1 px-3 py-2 rounded-lg bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-xs font-medium text-slate-800 dark:text-white placeholder-slate-400 focus:outline-none focus:border-orange-500 transition-colors"
              />
              <button className="px-3 py-2 rounded-lg bg-orange-50 dark:bg-orange-500/10 text-orange-500 text-xs font-bold hover:bg-orange-100 transition-colors">
                Áp dụng
              </button>
            </div>
          )}
        </div>

        {/* Cart Footer: Total + Payment Buttons */}
        <div className="border-t border-slate-100 dark:border-slate-800 p-5 space-y-3">
          {/* Total */}
          <div className="flex justify-between items-center">
            <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">Tổng cộng</span>
            <span className="text-2xl font-black text-slate-800 dark:text-white tracking-tight">
              {priceBreakdown ? formatMoney(priceBreakdown.finalTotal) : '0đ'}
            </span>
          </div>

          {/* Breakdown */}
          {priceBreakdown && (
            <div className="text-[10px] text-slate-400 space-y-0.5">
              <div className="flex justify-between"><span>Vé:</span><span>{formatMoney(priceBreakdown.ticketTotal)}</span></div>
              {priceBreakdown.fnbTotal > 0 && <div className="flex justify-between"><span>F&B:</span><span>{formatMoney(priceBreakdown.fnbTotal)}</span></div>}
              {priceBreakdown.discountAmount > 0 && <div className="flex justify-between text-green-500"><span>Giảm:</span><span>-{formatMoney(priceBreakdown.discountAmount)}</span></div>}
            </div>
          )}

          {/* Payment: MoMo → Tiền mặt → VNPay (đồng bộ với /booking/payment) */}
          <div className="space-y-2">
            <button
              type="button"
              onClick={handleMomoPayment}
              disabled={selectedSeats.length === 0 || paymentProcessing || momoProcessing || vnpayProcessing || !currentUser?.id}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-pink-500 to-rose-600 text-white font-black text-xs uppercase tracking-widest shadow-lg shadow-pink-500/25 hover:shadow-pink-500/40 disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
            >
              {momoProcessing ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              ) : (
                <>
                  <span className="material-symbols-outlined text-lg">account_balance_wallet</span>
                  MoMo
                </>
              )}
            </button>
            <button
              type="button"
              onClick={handleCashPayment}
              disabled={selectedSeats.length === 0 || paymentProcessing || momoProcessing || vnpayProcessing}
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
              disabled={selectedSeats.length === 0 || paymentProcessing || momoProcessing || vnpayProcessing || !currentUser?.id}
              className="w-full py-3.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-700 text-white font-black text-xs uppercase tracking-widest shadow-md shadow-blue-600/25 disabled:opacity-40 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
            >
              {vnpayProcessing ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
              ) : (
                <>
                  <span className="material-symbols-outlined text-lg">credit_card</span>
                  VNPay
                </>
              )}
            </button>
          </div>

          {/* Reset */}
          <button
            onClick={resetAll}
            className="w-full py-2 rounded-lg text-xs font-bold text-slate-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-all uppercase tracking-widest"
          >
            Esc — Hủy đơn
          </button>
        </div>
      </div>
    </div>
  );
}
