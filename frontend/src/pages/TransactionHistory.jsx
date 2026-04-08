import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import { AlertCircle, CheckCircle2, XCircle, ArrowRight, Wallet, Calendar, Ticket } from 'lucide-react';
import { BASE_URL, getAuthHeaders } from '../utils/api';

const TransactionHistory = () => {
    const { user } = useSelector((state) => state.auth);
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const paymentStatus = searchParams.get('payment');

    useEffect(() => {
        const fetchHistory = async () => {
            console.log(">>> [StarCine] Current User:", user);
            if (!user?.id) {
                console.warn(">>> [StarCine] User ID not found in Redux. Waiting...");
                return;
            }

            try {
                const url = `${BASE_URL}/payment/history/${user.id}`;
                console.log(">>> [StarCine] Fetching from:", url);
                
                const response = await fetch(url, {
                    headers: getAuthHeaders()
                });
                
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                
                const data = await response.json();
                console.log(">>> [StarCine] Data received:", data);
                setPayments(data);
            } catch (error) {
                console.error('Lỗi khi lấy lịch sử giao dịch:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchHistory();
    }, [user]);

    const getStatusInfo = (status) => {
        switch (status) {
            case 'SUCCESS':
                return { 
                    label: 'Thành công', 
                    color: 'text-emerald-400', 
                    bg: 'bg-emerald-400/10',
                    icon: <CheckCircle2 className="w-5 h-5 text-emerald-400" /> 
                };
            case 'FAILED':
                return { 
                    label: 'Thất bại', 
                    color: 'text-rose-400', 
                    bg: 'bg-rose-400/10',
                    icon: <XCircle className="w-5 h-5 text-rose-400" /> 
                };
            default:
                return { 
                    label: 'Đang xử lý', 
                    color: 'text-amber-400', 
                    bg: 'bg-amber-400/10',
                    icon: <AlertCircle className="w-5 h-5 text-amber-400" /> 
                };
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-[#0f171c] flex items-center justify-center">
                <div className="w-16 h-16 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#0f171c] text-white pt-40 pb-12 px-4 sm:px-6 font-sans">
            <div className="max-w-5xl mx-auto">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-10">
                    <div>
                        <h1 className="text-4xl font-black bg-gradient-to-r from-white to-gray-400 bg-clip-text text-transparent uppercase tracking-tight">
                            Lịch sử giao dịch
                        </h1>
                        <p className="text-gray-400 mt-2 font-medium">Theo dõi các đơn hàng và trạng thái thanh toán của bạn</p>
                        <p className="text-orange-500/80 text-[11px] mt-2 font-bold italic flex items-center gap-1.5 uppercase tracking-wider">
                            <AlertCircle className="w-3.5 h-3.5" />
                            Do độ trễ của hệ thống thử nghiệm (Sandbox), trạng thái vé có thể mất 1-2 phút để cập nhật. Cảm ơn bạn đã kiên nhẫn!
                        </p>
                    </div>
                </div>

                {paymentStatus === 'success' && (
                    <div className="mb-8 p-5 bg-emerald-500/10 border border-emerald-500/20 rounded-2xl flex items-center gap-4 text-emerald-400 animate-in fade-in slide-in-from-top-4 duration-500">
                        <CheckCircle2 className="w-6 h-6 shrink-0" />
                        <div>
                            <p className="font-bold">Thanh toán thành công!</p>
                            <p className="text-sm opacity-80">Vé của bạn đã sẵn sàng trong danh sách bên dưới.</p>
                        </div>
                    </div>
                )}

                {payments.length === 0 ? (
                    <div className="bg-[#1a2329] rounded-[40px] p-20 text-center border border-white/5 shadow-2xl">
                        <div className="w-24 h-24 bg-white/5 rounded-full flex items-center justify-center mx-auto mb-8">
                            <Ticket className="w-12 h-12 text-gray-600" />
                        </div>
                        <h3 className="text-2xl font-black mb-3 italic">CHƯA CÓ GIAO DỊCH</h3>
                        <p className="text-gray-400 mb-10 max-w-sm mx-auto font-medium leading-relaxed">Bạn chưa thực hiện bất kỳ giao dịch nào. Hãy chọn bộ phim yêu thích và đặt vé ngay!</p>
                        <button
                            onClick={() => navigate('/movies')}
                            className="group relative px-10 py-4 bg-orange-500 hover:bg-orange-600 rounded-2xl font-black transition-all flex items-center gap-2 mx-auto overflow-hidden shadow-xl shadow-orange-500/20"
                        >
                            <div className="absolute inset-0 bg-white/20 -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
                            <span className="relative">ĐẶT VÉ NGAY</span>
                            <ArrowRight className="w-5 h-5 relative group-hover:translate-x-1 transition-transform" />
                        </button>
                    </div>
                ) : (
                    <div className="grid gap-5">
                        {payments.map((payment) => {
                            const status = getStatusInfo(payment.status);
                            const movie = payment.booking?.showtime?.movie;
                            
                            return (
                                <div
                                    key={payment.paymentId}
                                    onClick={() => navigate(`/profile/transaction/${payment.paymentId}`)}
                                    className="group relative bg-[#1a2329] hover:bg-[#212b33] rounded-[32px] overflow-hidden border border-white/5 transition-all duration-300 cursor-pointer shadow-lg hover:shadow-2xl hover:shadow-orange-500/5 active:scale-[0.98]"
                                >
                                    <div className="flex flex-col sm:flex-row h-full">
                                        <div className="w-full sm:w-36 h-48 sm:h-auto overflow-hidden relative flex-shrink-0">
                                            <img 
                                                src={movie?.posterUrl || 'https://via.placeholder.com/300x450?text=Movie+Poster'} 
                                                alt={movie?.title}
                                                className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700"
                                            />
                                            <div className="absolute inset-0 bg-gradient-to-r from-black/60 to-transparent sm:hidden"></div>
                                        </div>

                                        <div className="flex-1 p-7 flex flex-col justify-between">
                                            <div className="flex justify-between items-start gap-4">
                                                <div className="flex-1">
                                                    <div className="flex items-center gap-3 mb-2">
                                                        <span className="px-2 py-0.5 bg-orange-500 text-white text-[10px] font-black rounded uppercase">ORD-{payment.booking?.bookingId}</span>
                                                        <span className="text-[11px] text-gray-500 uppercase font-black tracking-widest">{payment.paymentMethod}</span>
                                                    </div>
                                                    <h3 className="text-2xl font-black mb-3 group-hover:text-orange-500 transition-colors leading-tight">
                                                        {movie?.title || 'Đơn hàng dịch vụ'}
                                                    </h3>
                                                    <div className="flex flex-wrap items-center gap-6 text-sm text-gray-400 font-bold">
                                                        <div className="flex items-center gap-2">
                                                            <Calendar className="w-4 h-4 text-orange-500" />
                                                            {payment.paidAt ? format(new Date(payment.paidAt), 'dd/MM/yyyy • HH:mm', { locale: vi }) : '---'}
                                                        </div>
                                                        <div className="flex items-center gap-2">
                                                            <Wallet className="w-4 h-4 text-orange-500" />
                                                            <span className="text-white">{payment.amount.toLocaleString()}đ</span>
                                                        </div>
                                                    </div>
                                                </div>
                                                
                                                <div className={`shrink-0 px-5 py-2 rounded-2xl text-xs font-black uppercase tracking-wider flex items-center gap-2 ${status.bg} ${status.color} shadow-sm`}>
                                                    {status.icon}
                                                    {status.label}
                                                </div>
                                            </div>

                                            <div className="mt-8 flex items-center justify-between pt-6 border-t border-white/5">
                                                <div className="flex items-center gap-2 text-xs text-gray-500 font-black uppercase tracking-widest group-hover:text-gray-300 transition-colors">
                                                    Xem chi tiết vé <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                                                </div>
                                                <div className="flex -space-x-2">
                                                    {/* Fake decorative elements */}
                                                    {[1,2,3].map(i => <div key={i} className="w-6 h-6 rounded-full border-2 border-[#1a2329] bg-gray-800"></div>)}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
};

export default TransactionHistory;
