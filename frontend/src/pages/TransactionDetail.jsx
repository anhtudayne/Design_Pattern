import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import { 
  ChevronLeft, 
  MapPin, 
  Calendar, 
  Clock, 
  Square, 
  Popcorn, 
  Ticket as TicketIcon, 
  CheckCircle2, 
  XCircle, 
  AlertCircle,
  Download,
  Share2,
  ChevronRight,
  Info
} from 'lucide-react';
import { BASE_URL, getAuthHeaders } from '../utils/api';

const TransactionDetail = () => {
    const { id } = useParams();
    const [payment, setPayment] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchDetail = async () => {
            try {
                // GỌI ĐÚNG API: GET /api/payment/details/{paymentId}
                const response = await fetch(`${BASE_URL}/payment/details/${id}`, {
                    headers: getAuthHeaders()
                });
                if (!response.ok) throw new Error('Payment not found');
                const data = await response.json();
                setPayment(data);
            } catch (error) {
                console.error('Lỗi khi lấy chi tiết giao dịch:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchDetail();
    }, [id]);

    const getStatusInfo = (status) => {
        switch (status) {
            case 'SUCCESS':
                return { 
                    label: 'Thanh toán thành công', 
                    color: 'text-emerald-400', 
                    bg: 'bg-emerald-400/10',
                    icon: <CheckCircle2 className="w-16 h-16 text-emerald-400" /> 
                };
            case 'FAILED':
                return { 
                    label: 'Thanh toán thất bại', 
                    color: 'text-rose-400', 
                    bg: 'bg-rose-400/10',
                    icon: <XCircle className="w-16 h-16 text-rose-400" /> 
                };
            default:
                return { 
                    label: 'Giao dịch đang xử lý', 
                    color: 'text-amber-400', 
                    bg: 'bg-amber-400/10',
                    icon: <AlertCircle className="w-16 h-16 text-amber-400" /> 
                };
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex items-center justify-center">
                <div className="w-20 h-20 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    if (!payment) {
        return (
            <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col items-center justify-center text-slate-800 dark:text-white px-6 text-center">
                <Info className="w-16 h-16 text-orange-500 mb-6" />
                <h2 className="text-3xl font-black italic uppercase mb-4 tracking-tight">KHÔNG TÌM THẤY GIAO DỊCH</h2>
                <p className="text-slate-500 dark:text-gray-400 mb-8 max-w-sm font-medium leading-relaxed">Mã giao dịch #{id} không tồn tại hoặc bạn không có quyền truy cập vào thông tin này.</p>
                <button
                    onClick={() => navigate('/profile/transactions')}
                    className="px-10 py-4 bg-slate-800 dark:bg-white text-white dark:text-slate-900 font-black hover:bg-orange-500 dark:hover:bg-orange-500 hover:text-white transition-all rounded-2xl flex items-center gap-2"
                >
                    <ChevronLeft className="w-5 h-5" /> QUAY LẠI LỊCH SỬ
                </button>
            </div>
        );
    }

    const booking = payment.booking;
    const firstTicket = booking?.tickets && booking.tickets.length > 0 ? booking.tickets[0] : null;
    const showtime = firstTicket?.showtime;
    const movie = showtime?.movie;
    const status = getStatusInfo(payment.status);

    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-white pt-40 pb-20 px-4 sm:px-6 font-sans transition-colors duration-300">
            <div className="max-w-4xl mx-auto">
                <div className="flex items-center justify-between mb-10 w-full">
                    <button
                        onClick={() => navigate(-1)}
                        className="p-4 bg-white dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-2xl transition-all flex items-center gap-2 group shadow-lg border-2 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300"
                    >
                        <ChevronLeft className="w-5 h-5 group-hover:-translate-x-1 transition-transform" />
                        <span className="text-xs font-black uppercase tracking-widest">Lịch sử</span>
                    </button>
                    <div className="flex gap-4">
                        <button className="p-4 bg-white dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-2xl transition-all group overflow-hidden border-2 border-slate-200 dark:border-slate-700 shadow-lg">
                            <Download className="w-5 h-5 group-hover:scale-110 transition-transform" />
                        </button>
                        <button className="p-4 bg-white dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-2xl transition-all group overflow-hidden border-2 border-slate-200 dark:border-slate-700 shadow-lg">
                            <Share2 className="w-5 h-5 group-hover:scale-110 transition-transform" />
                        </button>
                    </div>
                </div>

                <div className="max-w-3xl mx-auto bg-white dark:bg-slate-900 rounded-[2.5rem] border-2 border-slate-200 dark:border-slate-800 shadow-2xl relative overflow-hidden transition-colors">
                    {/* Header Strip */}
                    <div className="h-2 w-full bg-gradient-to-r from-orange-500 to-red-600"></div>

                    {/* Status & Header */}
                    <div className="p-8 md:p-10 text-center border-b-2 border-dashed border-slate-200 dark:border-slate-700 relative">
                        <div className="flex justify-center mb-5 scale-90 md:scale-100">{status.icon}</div>
                        <h2 className={`text-2xl md:text-3xl font-black mb-2 uppercase tracking-tight ${status.color}`}>
                            {status.label}
                        </h2>
                        <p className="text-[10px] md:text-xs font-black text-slate-400 uppercase tracking-widest">
                            Mã GD: #{payment.transactionId || 'ORD-' + booking?.bookingId}
                        </p>
                    </div>

                    {/* Movie & QR Area */}
                    <div className="p-8 md:p-10 flex flex-col md:flex-row items-center md:items-start gap-8 md:gap-10 border-b-2 border-slate-100 dark:border-slate-800">
                        <div className="w-32 md:w-40 h-48 md:h-56 rounded-2xl overflow-hidden shadow-xl flex-shrink-0 border-4 border-slate-100 dark:border-slate-800">
                            <img src={movie?.posterUrl || 'https://via.placeholder.com/300x450'} className="w-full h-full object-cover" alt="" />
                        </div>
                        <div className="flex-1 text-center md:text-left flex flex-col justify-center">
                            <div className="flex items-center justify-center md:justify-start gap-3 mb-3 md:mb-4">
                                <span className="px-3 py-1 bg-red-50 dark:bg-slate-800 rounded-full text-[10px] font-black text-red-600 uppercase tracking-widest border border-red-200 dark:border-slate-700">StarCine Ticket</span>
                                <div className="h-1 w-1 bg-slate-300 dark:bg-slate-600 rounded-full"></div>
                                <span className="text-[10px] md:text-xs font-black text-slate-400 uppercase italic tracking-widest">E-Ticket</span>
                            </div>
                            <h3 className="text-2xl md:text-3xl font-black mb-5 md:mb-6 leading-tight tracking-tight text-slate-800 dark:text-white uppercase">{movie?.title || 'Phim StarCine'}</h3>
                            <div className="flex flex-wrap items-center justify-center md:justify-start gap-6 text-xs md:text-sm font-black text-slate-500 dark:text-slate-400">
                                <div className="flex items-center gap-2">
                                    <Clock className="w-4 md:w-5 h-4 md:h-5 text-orange-500" /> {movie?.durationMinutes} phút
                                </div>
                                <div className="flex items-center gap-2">
                                    <span className="px-2.5 py-1 bg-orange-500 text-white rounded text-[10px] md:text-xs">T{movie?.ageRating?.replace('C', '')}</span>
                                </div>
                            </div>
                        </div>

                        {payment.status === 'SUCCESS' && (
                            <div className="flex flex-col items-center gap-3 shrink-0 mt-6 md:mt-0">
                                <div className="p-2 md:p-3 bg-white border-2 border-slate-100 rounded-2xl shadow-lg ring-4 ring-slate-50 dark:ring-slate-800">
                                    <img 
                                        src={`https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=STARCINE-BKG-${booking?.bookingId}`}
                                        alt="Ticket QR"
                                        className="w-24 md:w-32 h-24 md:h-32"
                                    />
                                </div>
                                <p className="text-[9px] md:text-[10px] font-black text-orange-500 uppercase tracking-[0.2em] bg-orange-50 dark:bg-orange-500/10 px-3 py-1 rounded-full">Mã vé</p>
                            </div>
                        )}
                    </div>

                    {/* Booking Details Grid */}
                    <div className="p-8 md:p-10 grid grid-cols-1 md:grid-cols-2 gap-8 md:gap-10 border-b-2 border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/10">
                        <div className="space-y-5 md:space-y-6">
                            <h4 className="text-[10px] md:text-[11px] font-black uppercase tracking-[0.2em] text-slate-400 flex items-center gap-3">
                                <MapPin className="w-4 md:w-5 h-4 md:h-5 text-orange-500" /> Thông tin Rạp
                            </h4>
                            <div>
                                <p className="text-lg md:text-xl font-black tracking-tight text-slate-800 dark:text-white">{showtime?.room?.cinema?.name}</p>
                                <p className="text-[10px] md:text-xs text-slate-500 font-bold mt-1.5 leading-relaxed">{showtime?.room?.cinema?.address}</p>
                            </div>
                            <div className="grid grid-cols-2 gap-6 bg-white dark:bg-slate-900 p-4 rounded-xl border border-slate-100 dark:border-slate-800 shadow-sm">
                                <div>
                                    <p className="text-[9px] md:text-[10px] text-slate-400 uppercase font-black tracking-widest mb-1.5">Ngày</p>
                                    <p className="font-black text-sm md:text-base text-slate-800 dark:text-white">{showtime?.startTime ? format(new Date(showtime.startTime), 'dd/MM/yyyy') : '---'}</p>
                                </div>
                                <div>
                                    <p className="text-[9px] md:text-[10px] text-slate-400 uppercase font-black tracking-widest mb-1.5">Giờ</p>
                                    <p className="font-black text-sm md:text-base text-slate-800 dark:text-white">{showtime?.startTime ? format(new Date(showtime.startTime), 'HH:mm') : '---'}</p>
                                </div>
                            </div>
                        </div>

                        <div className="space-y-5 md:space-y-6">
                            <h4 className="text-[10px] md:text-[11px] font-black uppercase tracking-[0.2em] text-slate-400 flex items-center gap-3">
                                <TicketIcon className="w-4 md:w-5 h-4 md:h-5 text-orange-500" /> Phòng & Ghế
                            </h4>
                            <div>
                                <p className="text-[9px] md:text-[10px] text-slate-400 uppercase font-black tracking-widest mb-1.5">Phòng chiếu</p>
                                <p className="text-lg md:text-xl font-black tracking-tight text-slate-800 dark:text-white">{showtime?.room?.name}</p>
                            </div>
                            <div>
                                <p className="text-[9px] md:text-[10px] text-slate-400 uppercase font-black tracking-widest mb-2 md:mb-3">Ghế đã đặt</p>
                                <div className="flex flex-wrap gap-2">
                                    {booking?.tickets?.map((t, idx) => (
                                        <div key={idx} className="px-2 md:px-3 py-1 md:py-1.5 bg-orange-50 dark:bg-orange-500/10 border border-orange-200 dark:border-orange-500/20 rounded-lg flex items-center justify-center gap-1 shadow-sm">
                                            <span className="text-[9px] md:text-[10px] font-black text-orange-500">{t.seat?.seatRow}</span>
                                            <span className="text-xs md:text-sm font-black text-slate-800 dark:text-white">{t.seat?.seatNumber}</span>
                                        </div>
                                    ))}
                                    {(!booking?.tickets || booking.tickets.length === 0) && <p className="text-slate-400 italic text-xs md:text-sm">Trống</p>}
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* F&B Section */}
                    {((booking?.fnBLines && booking.fnBLines.length > 0) || (booking?.fnbItems && booking.fnbItems.length > 0)) && (
                        <div className="p-8 md:p-10 border-b-2 border-slate-100 dark:border-slate-800">
                             <h4 className="text-[10px] md:text-[11px] font-black uppercase tracking-[0.2em] text-slate-400 flex items-center gap-3 mb-5 md:mb-6">
                                <Popcorn className="w-4 md:w-5 h-4 md:h-5 text-orange-500" /> Combo Bắp Nước
                            </h4>
                            <div className="space-y-3 md:space-y-4">
                                {(booking.fnBLines || booking.fnbItems).map((fnb, idx) => {
                                    const qty = fnb.quantity;
                                    const name = fnb.item?.name || fnb.itemName || 'Sản phẩm';
                                    const unitPrice = Number(fnb.unitPrice ?? fnb.price ?? 0);
                                    return (
                                        <div key={idx} className="flex items-center justify-between">
                                            <div className="flex items-center gap-3 md:gap-4">
                                                <div className="w-7 md:w-8 h-7 md:h-8 rounded-lg bg-orange-100 dark:bg-orange-500/20 text-orange-600 dark:text-orange-400 flex items-center justify-center font-black text-xs md:text-sm">
                                                    x{qty}
                                                </div>
                                                <p className="font-bold text-slate-800 dark:text-white uppercase tracking-tight text-xs md:text-sm">{name}</p>
                                            </div>
                                            <p className="font-bold text-slate-500 dark:text-slate-400 text-xs md:text-sm">
                                                {(unitPrice * qty).toLocaleString('vi-VN')}đ
                                            </p>
                                        </div>
                                    )
                                })}
                            </div>
                        </div>
                    )}

                    {/* Total Area with Perforations */}
                    <div className="p-8 md:p-10 bg-slate-50 dark:bg-slate-900 border-t-2 border-dashed border-slate-200 dark:border-slate-700 relative">
                        <div className="absolute -top-4 -left-4 w-8 h-8 bg-slate-50 dark:bg-slate-950 rounded-full border-b-2 border-r-2 border-slate-200 dark:border-slate-800 -rotate-45 transform"></div>
                        <div className="absolute -top-4 -right-4 w-8 h-8 bg-slate-50 dark:bg-slate-950 rounded-full border-b-2 border-l-2 border-slate-200 dark:border-slate-800 rotate-45 transform"></div>
                        
                        <div className="flex flex-col md:flex-row items-center md:items-end justify-between gap-4 md:gap-6 text-center md:text-left">
                            <div>
                                <h4 className="text-[10px] md:text-[11px] font-black uppercase tracking-[0.2em] text-slate-400 mb-1.5 md:mb-2">Tổng Thanh Toán</h4>
                                <p className="text-[10px] md:text-xs font-bold text-slate-500">Đã bao gồm thuế và phí dịch vụ</p>
                            </div>
                            <div className="text-3xl md:text-4xl font-black text-orange-500 italic flex items-baseline gap-1 md:gap-2 justify-center">
                                {payment.amount.toLocaleString('vi-VN')} <span className="text-lg md:text-xl relative -top-1">đ</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TransactionDetail;
