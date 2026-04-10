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
            <div className="min-h-screen bg-[#0f171c] flex items-center justify-center">
                <div className="w-20 h-20 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    if (!payment) {
        return (
            <div className="min-h-screen bg-[#0f171c] flex flex-col items-center justify-center text-white px-6 text-center">
                <Info className="w-16 h-16 text-orange-500 mb-6" />
                <h2 className="text-3xl font-black italic uppercase mb-4 tracking-tight">KHÔNG TÌM THẤY GIAO DỊCH</h2>
                <p className="text-gray-400 mb-8 max-w-sm font-medium leading-relaxed">Mã giao dịch #{id} không tồn tại hoặc bạn không có quyền truy cập vào thông tin này.</p>
                <button
                    onClick={() => navigate('/profile/transactions')}
                    className="px-10 py-4 bg-white text-black font-black hover:bg-orange-500 hover:text-white transition-all rounded-2xl flex items-center gap-2"
                >
                    <ChevronLeft className="w-5 h-5" /> QUAY LẠI LỊCH SỬ
                </button>
            </div>
        );
    }

    const booking = payment.booking;
    const movie = booking?.showtime?.movie;
    const showtime = booking?.showtime;
    const status = getStatusInfo(payment.status);

    return (
        <div className="min-h-screen bg-[#0f171c] text-white pt-40 pb-20 px-4 sm:px-6 font-sans">
            <div className="max-w-4xl mx-auto">
                <div className="flex items-center justify-between mb-10 w-full">
                    <button
                        onClick={() => navigate(-1)}
                        className="p-4 bg-white/5 hover:bg-white/10 rounded-2xl transition-all flex items-center gap-2 group shadow-lg border border-white/5"
                    >
                        <ChevronLeft className="w-5 h-5 group-hover:-translate-x-1 transition-transform" />
                        <span className="text-xs font-black uppercase tracking-widest">Lịch sử</span>
                    </button>
                    <div className="flex gap-4">
                        <button className="p-4 bg-white/5 hover:bg-white/10 rounded-2xl transition-all group overflow-hidden border border-white/5">
                            <Download className="w-5 h-5 group-hover:scale-110 transition-transform" />
                        </button>
                        <button className="p-4 bg-white/5 hover:bg-white/10 rounded-2xl transition-all group overflow-hidden border border-white/5">
                            <Share2 className="w-5 h-5 group-hover:scale-110 transition-transform" />
                        </button>
                    </div>
                </div>

                <div className="grid lg:grid-cols-12 gap-10">
                    <div className="lg:col-span-4 space-y-8">
                        <div className="bg-[#1a2329] rounded-[48px] p-10 border border-white/5 text-center flex flex-col items-center shadow-2xl relative overflow-hidden group">
                            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-orange-500 to-red-600"></div>
                            <div className="mb-8">{status.icon}</div>
                            <h2 className={`text-2xl font-black mb-3 px-2 ${status.color}`}>
                                {status.label}
                            </h2>
                            <p className="text-[11px] font-black text-gray-500 uppercase tracking-[0.2em] mb-10">
                                #{payment.transactionId || 'ORD-' + booking?.bookingId}
                            </p>

                            {payment.status === 'SUCCESS' && (
                                <div className="p-5 bg-white rounded-xl mb-8 shadow-2xl shadow-orange-500/10 relative cursor-pointer ring-8 ring-white/5 hover:ring-orange-500/10 transition-all duration-500">
                                    <img 
                                        src={`https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=STARCINE-BKG-${booking?.bookingId}`}
                                        alt="Ticket QR"
                                        className="w-48 h-48"
                                    />
                                    <div className="absolute inset-0 bg-white/95 flex flex-col items-center justify-center opacity-0 hover:opacity-100 transition-opacity rounded-xl duration-300">
                                        <TicketIcon className="w-10 h-10 text-orange-500 mb-3" />
                                        <p className="text-black font-black text-[10px] uppercase tracking-[0.2em]">Quét mã soát vé</p>
                                    </div>
                                </div>
                            )}
                            
                            <div className="flex items-center gap-2 p-4 bg-white/5 rounded-2xl text-[10px] text-gray-400 font-bold leading-relaxed border border-white/5">
                                <Info className="w-4 h-4 shrink-0 text-orange-500" />
                                <p className="text-left italic uppercase">Mang mã QR này đến quầy soát vé để nhận vé giấy hoặc vào cửa.</p>
                            </div>
                        </div>

                        <div className="bg-gradient-to-br from-orange-500 to-red-600 rounded-[48px] p-10 text-center shadow-2xl shadow-orange-500/20 relative overflow-hidden group">
                             <div className="absolute inset-0 bg-white/10 opacity-0 group-hover:opacity-100 transition-opacity duration-700 -translate-x-full group-hover:translate-x-full rotate-45 transform"></div>
                            <h4 className="text-[10px] font-black uppercase tracking-[0.4em] mb-4 text-white/60">Tổng thanh toán</h4>
                            <div className="text-5xl font-black text-white italic">
                                {payment.amount.toLocaleString()}đ
                            </div>
                            <p className="text-[11px] font-black text-white/40 mt-4 uppercase tracking-[0.1em] border-t border-white/10 pt-4">
                                {booking?.tickets?.length || 0} Ghế • {(booking?.fnBLines?.length || booking?.fnbItems?.length || 0)} Bắp nước
                            </p>
                        </div>
                    </div>

                    <div className="lg:col-span-8 space-y-8">
                        <div className="bg-[#1a2329] rounded-[48px] p-10 border border-white/5 overflow-hidden relative shadow-2xl group min-h-[220px] flex items-center">
                            <div className="absolute inset-0 opacity-10 group-hover:opacity-20 transition-opacity duration-1000">
                                <img src={movie?.posterUrl} className="w-full h-full object-cover blur-3xl scale-150" alt="" />
                            </div>
                            <div className="flex gap-10 relative z-10 w-full h-full">
                                <div className="w-32 h-44 rounded-3xl overflow-hidden shadow-2xl flex-shrink-0 border-4 border-white/5 group-hover:border-orange-500/20 transition-colors duration-500">
                                    <img src={movie?.posterUrl || 'https://via.placeholder.com/300x450'} className="w-full h-full object-cover" alt="" />
                                </div>
                                <div className="flex flex-col justify-center flex-1">
                                    <div className="flex items-center gap-3 mb-3">
                                        <span className="px-3 py-1 bg-white/10 rounded-full text-[10px] font-black text-orange-500 uppercase tracking-widest border border-orange-500/20">Official Ticket</span>
                                        <div className="h-1 w-1 bg-gray-500 rounded-full"></div>
                                        <span className="text-xs font-black text-gray-500 uppercase italic">Digital Product</span>
                                    </div>
                                    <h3 className="text-4xl font-black mb-5 leading-none tracking-tight group-hover:text-orange-500 transition-colors">{movie?.title || 'Phim StarCine'}</h3>
                                    <div className="flex flex-wrap items-center gap-8 text-xs font-black text-gray-400">
                                        <div className="flex items-center gap-2 group/icon transition-colors">
                                            <div className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center group-hover/icon:bg-orange-500 transition-colors"><Clock className="w-4 h-4 text-orange-500 group-hover/icon:text-white" /></div> 
                                            {movie?.durationMinutes} phút
                                        </div>
                                        <div className="flex items-center gap-2 group/icon transition-colors">
                                            <div className="w-8 h-8 rounded-full bg-white/5 flex items-center justify-center group-hover/icon:bg-orange-500 transition-colors"><Info className="w-4 h-4 text-orange-500 group-hover/icon:text-white" /></div>
                                            <span className="px-2 py-0.5 bg-orange-500 text-white rounded text-[10px]">T{movie?.ageRating?.replace('C', '')}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                            <div className="bg-[#1a2329] rounded-[48px] p-10 border border-white/5 space-y-8 shadow-2xl">
                                <h4 className="text-[11px] font-black uppercase tracking-[0.2em] text-gray-500 flex items-center gap-3">
                                    <MapPin className="w-5 h-5 text-orange-500" /> THÔNG TIN RẠP
                                </h4>
                                <div className="space-y-6">
                                    <div>
                                        <p className="text-2xl font-black tracking-tight">{showtime?.room?.cinema?.name || 'StarCine Complex'}</p>
                                        <p className="text-xs text-gray-400 font-bold mt-2 leading-relaxed opacity-70">{showtime?.room?.cinema?.address}</p>
                                    </div>
                                    <div className="grid grid-cols-2 gap-8 pt-8 border-t border-white/5">
                                        <div>
                                            <p className="text-[10px] text-orange-500 uppercase font-black tracking-widest mb-2">Ngày</p>
                                            <p className="font-black text-lg italic">{showtime?.startTime ? format(new Date(showtime.startTime), 'dd/MM/yyyy') : '---'}</p>
                                        </div>
                                        <div>
                                            <p className="text-[10px] text-orange-500 uppercase font-black tracking-widest mb-2">Giờ</p>
                                            <p className="font-black text-lg italic">{showtime?.startTime ? format(new Date(showtime.startTime), 'HH:mm') : '---'}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-[#1a2329] rounded-[48px] p-10 border border-white/5 space-y-8 shadow-2xl">
                                <h4 className="text-[11px] font-black uppercase tracking-[0.2em] text-gray-500 flex items-center gap-3">
                                    <TicketIcon className="w-5 h-5 text-orange-500" /> PHÒNG & GHẾ
                                </h4>
                                <div className="space-y-6">
                                    <div>
                                        <p className="text-[10px] text-orange-500 uppercase font-black tracking-widest mb-2 italic">Phòng chiếu số</p>
                                        <p className="text-3xl font-black tracking-tight">{showtime?.room?.name || 'Cinema Room'}</p>
                                    </div>
                                    <div className="pt-8 border-t border-white/5">
                                        <p className="text-[10px] text-orange-500 uppercase font-black tracking-widest mb-4">Danh sách ghế</p>
                                        <div className="flex flex-wrap gap-2.5">
                                            {booking?.tickets?.map((t, idx) => (
                                                <div key={idx} className="w-14 h-14 bg-gradient-to-t from-white/5 to-white/10 rounded-2xl flex flex-col items-center justify-center border border-white/5 group-hover:border-orange-500 transition-colors">
                                                    <span className="text-[10px] font-black text-orange-500 uppercase tracking-widest">{t.seat?.seatRow}</span>
                                                    <span className="text-lg font-black text-white">{t.seat?.seatNumber}</span>
                                                </div>
                                            ))}
                                            {(!booking?.tickets || booking.tickets.length === 0) && <p className="text-gray-500 italic font-black text-sm uppercase">Trống</p>}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        {((booking?.fnBLines && booking.fnBLines.length > 0) || (booking?.fnbItems && booking.fnbItems.length > 0)) && (
                            <div className="bg-[#1a2329] rounded-[48px] p-10 border border-white/5 shadow-2xl relative overflow-hidden">
                                <div className="absolute right-0 top-0 opacity-5 -translate-y-4 translate-x-4">
                                     <Popcorn className="w-40 h-40" />
                                </div>
                                <h4 className="text-[11px] font-black uppercase tracking-[0.2em] text-gray-500 flex items-center gap-3 mb-10">
                                    <Popcorn className="w-5 h-5 text-orange-500" /> BẮP NƯỚC ĐÃ ĐẶT
                                </h4>
                                <div className="grid gap-4">
                                    {(booking.fnBLines || booking.fnbItems).map((fnb, idx) => {
                                        const qty = fnb.quantity;
                                        const name = fnb.item?.name || fnb.itemName || 'Combo Sản phẩm';
                                        const unitPrice = Number(fnb.unitPrice ?? fnb.price ?? 0);
                                        const lineTotal = unitPrice * qty;
                                        return (
                                        <div key={idx} className="flex items-center justify-between p-6 bg-white/5 rounded-[32px] border border-white/5 group hover:bg-white/10 transition-all">
                                            <div className="flex items-center gap-6">
                                                <div className="w-14 h-14 rounded-2xl bg-orange-500 flex items-center justify-center font-black text-white text-xl shadow-lg shadow-orange-500/20">
                                                    {qty}
                                                </div>
                                                <div>
                                                    <p className="font-black text-lg tracking-tight uppercase italic">{name}</p>
                                                    <p className="text-xs text-gray-500 font-bold uppercase tracking-widest mt-1">
                                                        Đơn giá chốt: {unitPrice.toLocaleString('vi-VN')}đ
                                                    </p>
                                                </div>
                                            </div>
                                            <span className="font-black text-xl italic text-orange-500">{lineTotal.toLocaleString('vi-VN')}đ</span>
                                        </div>
                                        );
                                    })}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TransactionDetail;
