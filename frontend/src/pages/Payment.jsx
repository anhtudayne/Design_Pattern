import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useBooking } from '../contexts/BookingContext';
import { calculatePrice } from '../services/bookingService';
import { demoCheckout } from '../services/paymentService';

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
              <div className={`w-16 md:w-28 h-0.5 mb-5 mx-2 rounded-full ${isDone || isCurrent ? 'bg-green-400' : 'bg-slate-200'}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}

export default function Payment() {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useSelector(state => state.auth);
  const { bookingSelection, setPriceBreakdown, setVoucherCode: setContextVoucher } = useBooking();
  const { movie, cinema, showtime, selectedSeats, selectedSnacks, priceBreakdown, voucherCode: savedVoucher } = bookingSelection;

  const [paymentMethod, setPaymentMethod] = useState('momo');
  const [loading, setLoading] = useState(false);
  const [voucherCode, setVoucherCode] = useState(savedVoucher || '');
  const [voucherLoading, setVoucherLoading] = useState(false);
  const [voucherMsg, setVoucherMsg] = useState('');
  const [demoQr, setDemoQr] = useState(null);

  // Local state cho thông tin người mua (khởi tạo từ user redux)
  const [buyerInfo, setBuyerInfo] = useState({
    fullName: user?.fullName || '',
    phoneNumber: user?.phoneNumber || '',
    email: user?.email || '',
  });

  // Cập nhật buyerInfo nếu user từ Redux thay đổi
  useEffect(() => {
    if (user) {
      setBuyerInfo({
        fullName: user.fullName || '',
        phoneNumber: user.phoneNumber || '',
        email: user.email || '',
      });
    }
  }, [user]);

  // Protect route
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/booking/payment' } });
    } else if (!selectedSeats || selectedSeats.length === 0) {
      navigate('/movies');
    }
  }, [isAuthenticated, selectedSeats, navigate]);

  // Fetch price breakdown from backend on mount
  useEffect(() => {
    if (!showtime || !selectedSeats || selectedSeats.length === 0) return;
    const fetchBreakdown = async () => {
      try {
        const body = {
          showtimeId: showtime.showtimeId,
          seatIds: selectedSeats.map(s => s.seatId),
          fnbs: (selectedSnacks || []).map(s => ({ itemId: s.itemId, quantity: s.quantity })),
          promoCode: voucherCode || null,
        };
        const data = await calculatePrice(body);
        setPriceBreakdown(data);
      } catch (err) {
        console.error('calculatePrice failed', err);
      }
    };
    fetchBreakdown();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showtime?.showtimeId, selectedSeats?.length]);

  // Use backend-calculated totalPrice for seats
  const seatTotal = useMemo(() => {
    return (selectedSeats || []).reduce((sum, seat) => sum + (Number(seat.totalPrice) || 0), 0);
  }, [selectedSeats]);

  const snackTotal = useMemo(() => {
    return (selectedSnacks || []).reduce((sum, item) => sum + (Number(item.unitPrice) * item.quantity), 0);
  }, [selectedSnacks]);

  // Display from backend if available, else fallback to frontend calculation
  const displayTotal = priceBreakdown?.finalTotal != null ? Number(priceBreakdown.finalTotal) : (seatTotal + snackTotal);
  const displayTicket = priceBreakdown?.ticketTotal != null ? Number(priceBreakdown.ticketTotal) : seatTotal;
  const displayFnb = priceBreakdown?.fnbTotal != null ? Number(priceBreakdown.fnbTotal) : snackTotal;
  const displayDiscount = priceBreakdown?.discountAmount != null ? Number(priceBreakdown.discountAmount) : 0;

  // Apply voucher → call calculatePrice with promo code
  const handleApplyVoucher = async () => {
    if (!voucherCode.trim()) return;
    setVoucherLoading(true);
    setVoucherMsg('');
    try {
      const body = {
        showtimeId: showtime.showtimeId,
        seatIds: selectedSeats.map(s => s.seatId),
        fnbs: (selectedSnacks || []).map(s => ({ itemId: s.itemId, quantity: s.quantity })),
        promoCode: voucherCode.trim(),
      };
      const data = await calculatePrice(body);
      setPriceBreakdown(data);
      setContextVoucher(voucherCode.trim());
      if (Number(data.discountAmount) > 0) {
        setVoucherMsg(`Giảm ${Number(data.discountAmount).toLocaleString('vi-VN')}đ!`);
      } else {
        setVoucherMsg('Mã không hợp lệ hoặc chưa đủ điều kiện.');
      }
    } catch (err) {
      setVoucherMsg('Mã không tồn tại hoặc đã hết hạn.');
    } finally {
      setVoucherLoading(false);
    }
  };

  const handlePayment = async () => {
    if (loading) return;
    if (!buyerInfo.fullName || !buyerInfo.phoneNumber || !buyerInfo.email) {
      alert('Vui lòng nhập đầy đủ thông tin người mua');
      return;
    }
    setLoading(true);

    try {
      if (paymentMethod === 'momo') {
        const randomToken = `${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
        const orderId = `DEMO_${Date.now()}`;
        setDemoQr({
          orderId,
          amount: displayTotal,
          qrText: `demo-payment|orderId=${orderId}|user=${user?.id}|showtime=${showtime?.showtimeId}|token=${randomToken}`,
        });
        setLoading(false);
      } else {
        alert('Hình thức thanh toán này đang được bảo trì. Vui lòng chọn Ví MoMo.');
        setLoading(false);
      }
    } catch (err) {
      console.error('Payment failed', err);
      alert(err.message || 'Đã có lỗi xảy ra. Vui lòng thử lại.');
      setLoading(false);
    }
  };

  const handleDemoResult = async (isSuccess) => {
    if (!demoQr) return;
    try {
      setLoading(true);
      const checkoutData = {
        userId: user.id,
        showtimeId: showtime.showtimeId,
        seatIds: selectedSeats.map(s => s.seatId),
        fnbs: (selectedSnacks || []).map(s => ({ itemId: s.itemId, quantity: s.quantity })),
        promoCode: voucherCode || null,
      };
      const result = await demoCheckout(checkoutData, isSuccess);
      const target = isSuccess
        ? `/profile/transactions?payment=success&orderId=${encodeURIComponent(result?.bookingCode || demoQr.orderId)}&bookingId=${result?.bookingId || ''}&demo=1`
        : `/profile/transactions?payment=failed&errorCode=DEMO_CANCEL&bookingId=${result?.bookingId || ''}&demo=1`;
      setDemoQr(null);
      navigate(target);
    } catch (err) {
      alert(err.message || 'Không thể cập nhật trạng thái thanh toán demo');
    } finally {
      setLoading(false);
    }
  };

  const paymentMethods = [
    { id: 'momo', name: 'Ví MoMo', desc: 'Thanh toán nhanh qua QR', icon: 'account_balance_wallet', iconColor: 'text-pink-500' },
    { id: 'visa', name: 'Thẻ Visa / Mastercard', desc: 'Sắp ra mắt', icon: 'credit_card', iconColor: 'text-blue-600', disabled: true },
    { id: 'zalo', name: 'ZaloPay', desc: 'Sắp ra mắt', icon: 'payments', iconColor: 'text-blue-500', disabled: true },
  ];

  if (!movie || !isAuthenticated) return null;

  return (
    <main className="pt-44 pb-20 bg-slate-50 dark:bg-slate-950 min-h-screen">
      {demoQr && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center px-4">
          <div className="w-full max-w-md rounded-3xl bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-700 shadow-2xl p-6">
            <h3 className="text-xl font-black text-slate-800 dark:text-white uppercase tracking-tight">QR Thanh Toán Demo</h3>
            <p className="mt-2 text-sm text-slate-500 dark:text-slate-300">Mã đơn: <span className="font-bold">{demoQr.orderId}</span></p>
            <p className="text-sm text-slate-500 dark:text-slate-300 mb-4">Số tiền: <span className="font-black text-orange-500">{Number(demoQr.amount || 0).toLocaleString('vi-VN')}đ</span></p>

            <div className="bg-white rounded-2xl p-4 border border-slate-200 mx-auto w-fit">
              <img
                alt="Demo payment QR"
                className="w-64 h-64 object-contain"
                src={`https://quickchart.io/qr?text=${encodeURIComponent(demoQr.qrText)}&size=320`}
              />
            </div>

            <div className="mt-5 grid grid-cols-2 gap-3">
              <button
                onClick={() => handleDemoResult(false)}
                className="py-3 rounded-xl border-2 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 font-black uppercase text-xs hover:bg-slate-100 dark:hover:bg-slate-800 transition"
              >
                Thất bại
              </button>
              <button
                onClick={() => handleDemoResult(true)}
                className="py-3 rounded-xl bg-gradient-to-r from-green-500 to-emerald-500 text-white font-black uppercase text-xs shadow-lg shadow-green-500/30 hover:scale-[1.02] transition"
              >
                Thành công
              </button>
            </div>

            <button
              onClick={() => setDemoQr(null)}
              className="mt-3 w-full py-2 text-xs font-bold text-slate-500 hover:text-slate-700 dark:hover:text-slate-300"
            >
              Đóng
            </button>
          </div>
        </div>
      )}
      <div className="max-w-[1440px] mx-auto px-6 md:px-10">
        <Stepper active={3} />
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-start">
          <div className="lg:col-span-8 space-y-10">
            {/* Buyer Info */}
            <section className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-[2rem] shadow-xl p-8 md:p-10">
              <div className="flex items-center gap-3 mb-8">
                <span className="w-2 h-8 bg-gradient-to-b from-orange-500 to-red-500 rounded-full"></span>
                <h2 className="text-2xl font-black text-slate-800 dark:text-white tracking-tight uppercase">Thông tin người mua</h2>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="flex flex-col gap-2">
                  <label className="text-[10px] font-black uppercase tracking-widest text-slate-400">Họ và Tên</label>
                  <input 
                    type="text" 
                    placeholder="Nhập họ và tên..."
                    value={buyerInfo.fullName} 
                    onChange={e => setBuyerInfo(prev => ({ ...prev, fullName: e.target.value }))}
                    className="bg-slate-50 dark:bg-slate-800 border-2 border-slate-100 dark:border-slate-800 rounded-xl px-5 py-4 text-sm font-bold text-slate-800 dark:text-white outline-none focus:border-orange-500 transition-colors" 
                  />
                </div>
                <div className="flex flex-col gap-2">
                  <label className="text-[10px] font-black uppercase tracking-widest text-slate-400">Số điện thoại</label>
                  <input 
                    type="text" 
                    placeholder="Nhập số điện thoại..."
                    value={buyerInfo.phoneNumber} 
                    onChange={e => setBuyerInfo(prev => ({ ...prev, phoneNumber: e.target.value }))}
                    className="bg-slate-50 dark:bg-slate-800 border-2 border-slate-100 dark:border-slate-800 rounded-xl px-5 py-4 text-sm font-bold text-slate-800 dark:text-white outline-none focus:border-orange-500 transition-colors" 
                  />
                </div>
                <div className="md:col-span-2 flex flex-col gap-2">
                  <label className="text-[10px] font-black uppercase tracking-widest text-slate-400">Địa chỉ Email</label>
                  <input 
                    type="email" 
                    placeholder="Nhập email xử lý vé..."
                    value={buyerInfo.email} 
                    onChange={e => setBuyerInfo(prev => ({ ...prev, email: e.target.value }))}
                    className="bg-slate-50 dark:bg-slate-800 border-2 border-slate-100 dark:border-slate-800 rounded-xl px-5 py-4 text-sm font-bold text-slate-800 dark:text-white outline-none focus:border-orange-500 transition-colors" 
                  />
                </div>
              </div>
            </section>

            {/* Payment Method */}
            <section className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-[2rem] shadow-xl p-8 md:p-10">
              <div className="flex items-center gap-3 mb-8">
                <span className="w-2 h-8 bg-gradient-to-b from-cyan-500 to-blue-500 rounded-full"></span>
                <h2 className="text-2xl font-black text-slate-800 dark:text-white tracking-tight uppercase">Hình thức thanh toán</h2>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {paymentMethods.map(m => (
                  <label key={m.id} className={`cursor-pointer group ${m.disabled ? 'opacity-50 grayscale pointer-events-none' : ''}`}>
                    <input type="radio" name="payment" className="peer sr-only" checked={paymentMethod === m.id} onChange={() => setPaymentMethod(m.id)} disabled={m.disabled} />
                    <div className={`p-5 rounded-2xl border-2 transition-all flex items-center gap-4 ${paymentMethod === m.id ? 'border-orange-500 bg-orange-50 dark:bg-orange-500/10 shadow-lg' : 'border-slate-100 dark:border-slate-800 bg-slate-50 dark:bg-slate-800 hover:border-slate-300 dark:hover:border-slate-600'}`}>
                      <div className="w-11 h-11 rounded-xl bg-white dark:bg-slate-900 flex items-center justify-center shrink-0 shadow-md">
                        <span className={`material-symbols-outlined text-xl ${m.iconColor}`}>{m.icon}</span>
                      </div>
                      <div>
                        <p className="font-black text-sm text-slate-800 dark:text-white">{m.name}</p>
                        <p className="text-xs text-slate-400 font-medium">{m.desc}</p>
                      </div>
                      {paymentMethod === m.id && <span className="material-symbols-outlined text-orange-500 ml-auto text-xl">check_circle</span>}
                    </div>
                  </label>
                ))}
              </div>
            </section>

            {/* Voucher */}
            <section className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-[2rem] shadow-xl p-8 md:p-10">
              <div className="flex items-center gap-3 mb-8">
                <span className="w-2 h-8 bg-gradient-to-b from-green-500 to-emerald-500 rounded-full"></span>
                <h2 className="text-2xl font-black text-slate-800 dark:text-white tracking-tight uppercase">Mã giảm giá</h2>
              </div>
              <div className="flex gap-4">
                <input type="text" placeholder="Nhập mã ưu đãi của bạn..." value={voucherCode} onChange={e => setVoucherCode(e.target.value)} className="flex-grow bg-slate-50 dark:bg-slate-800 border-2 border-slate-200 dark:border-slate-700 rounded-xl px-5 py-4 text-sm font-bold text-slate-800 dark:text-white placeholder-slate-300 outline-none focus:border-orange-500 transition-colors" />
                <button onClick={handleApplyVoucher} disabled={voucherLoading} className="px-8 py-4 bg-slate-800 dark:bg-slate-700 text-white font-black rounded-xl hover:bg-slate-700 transition-colors uppercase tracking-wider text-sm shrink-0 disabled:opacity-50">
                  {voucherLoading ? 'Đang kiểm tra...' : 'Áp dụng'}
                </button>
              </div>
              {voucherMsg && (
                <p className={`text-sm font-bold mt-3 ${displayDiscount > 0 ? 'text-green-500' : 'text-red-500'}`}>
                  {voucherMsg}
                </p>
              )}
            </section>
          </div>

          {/* RIGHT: Order Summary */}
          <aside className="lg:col-span-4">
            <div className="bg-white dark:bg-slate-900 border-2 border-slate-200 dark:border-slate-800 rounded-[2.5rem] shadow-2xl p-10 sticky top-32">
              {/* Movie */}
              <div className="flex gap-5 pb-8 border-b-2 border-slate-50 dark:border-slate-800">
                <div className="w-24 aspect-[2/3] rounded-2xl overflow-hidden shadow-2xl shrink-0 border border-slate-100 dark:border-slate-700">
                  <img alt={movie.title} className="w-full h-full object-cover" src={movie.posterUrl && movie.posterUrl.startsWith('http') ? movie.posterUrl : `https://lh3.googleusercontent.com/aida-public/${movie.posterUrl}`} />
                </div>
                <div className="space-y-1">
                  <h3 className="font-black text-slate-800 dark:text-white text-lg leading-tight uppercase tracking-tight line-clamp-2">{movie.title}</h3>
                  <div className="flex gap-2"><span className="px-2.5 py-1 rounded-lg text-[10px] font-black text-white bg-red-600">{movie.ageRating}</span></div>
                </div>
              </div>

              {/* Booking Details */}
              <div className="py-6 space-y-4 border-b-2 border-slate-50 dark:border-slate-800">
                <div className="flex justify-between items-start"><span className="text-xs font-black uppercase text-slate-400">Rạp</span><span className="text-sm font-black text-slate-800 dark:text-white text-right">{cinema?.name}</span></div>
                <div className="flex justify-between items-start"><span className="text-xs font-black uppercase text-slate-400">Suất chiếu</span><span className="text-sm font-black text-slate-800 dark:text-white text-right">{showtime?.startTime?.split('T')[1]?.substring(0, 5)} • {showtime?.startTime ? new Date(showtime.startTime).toLocaleDateString('vi-VN') : ''}</span></div>
                <div className="flex justify-between items-start"><span className="text-xs font-black uppercase text-slate-400">Ghế</span><span className="text-sm font-black text-slate-800 dark:text-white text-right">{(selectedSeats || []).map(s => `${s.seatRow || s.rowName}${s.seatNumber}`).join(', ')}</span></div>
              </div>

              {/* Price Breakdown */}
              <div className="py-6 space-y-3 border-b-2 border-slate-50 dark:border-slate-800">
                <div className="flex justify-between text-sm">
                  <span className="text-slate-500 font-bold">Vé ({(selectedSeats || []).length} ghế)</span>
                  <span className="font-black text-slate-800 dark:text-white">{displayTicket.toLocaleString('vi-VN')}đ</span>
                </div>
                {displayFnb > 0 && (
                  <div className="flex justify-between text-sm">
                    <span className="text-slate-500 font-bold">Bắp nước</span>
                    <span className="font-black text-slate-800 dark:text-white">{displayFnb.toLocaleString('vi-VN')}đ</span>
                  </div>
                )}
                {displayDiscount > 0 && (
                  <div className="flex justify-between text-sm">
                    <span className="text-green-500 font-bold">Giảm giá</span>
                    <span className="font-black text-green-500">-{displayDiscount.toLocaleString('vi-VN')}đ</span>
                  </div>
                )}
              </div>

              {/* Grand Total & Pay Button */}
              <div className="pt-6 space-y-6">
                <div>
                  <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Tổng cộng</p>
                  <p className="text-4xl font-black tracking-tighter text-slate-800 dark:text-white">{displayTotal.toLocaleString('vi-VN')}đ</p>
                </div>
                <button onClick={handlePayment} disabled={loading} className={`w-full py-5 rounded-[1.5rem] font-black text-base uppercase tracking-widest flex items-center justify-center gap-3 shadow-xl transition-all ${loading ? 'bg-slate-200 text-slate-400 cursor-not-allowed' : 'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-orange-500/30 hover:shadow-orange-500/50 hover:scale-[1.03]'}`}>
                  {loading ? <div className="w-6 h-6 border-2 border-slate-400 border-t-white rounded-full animate-spin"></div> : <><span className="material-symbols-outlined font-black text-xl">lock</span>THANH TOÁN NGAY</>}
                </button>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </main>
  );
}
