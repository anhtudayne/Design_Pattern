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
            <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex items-center justify-center">
                <div className="w-16 h-16 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-800 dark:text-white pt-40 pb-12 px-4 sm:px-6 font-sans transition-colors duration-300">
            <div className="max-w-5xl mx-auto">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-10">
                    <div>
                        <div className="flex items-center gap-3 mb-2">
                            <span className="w-2 h-8 bg-gradient-to-b from-orange-500 to-red-500 rounded-full"></span>
                            <h1 className="text-4xl font-black text-slate-800 dark:text-white uppercase tracking-tight">
                                Lịch sử giao dịch
                            </h1>
                        </div>
                        <p className="text-slate-500 dark:text-slate-400 font-bold tracking-tight">Theo dõi các đơn hàng và trạng thái thanh toán của bạn</p>
                        <div className="mt-4 p-3 bg-orange-50 dark:bg-orange-500/10 border border-orange-200 dark:border-orange-500/20 rounded-xl inline-flex items-center gap-2">
                            <AlertCircle className="w-4 h-4 text-orange-500 shrink-0" />
                            <p className="text-orange-600 dark:text-orange-400 text-xs font-bold uppercase tracking-wider">
                                Do độ trễ của hệ thống (Sandbox), trạng thái vé có thể mất 1-2 phút để cập nhật.
                            </p>
                        </div>
                    </div>
                </div>

                {paymentStatus === 'success' && (
                    <div className="mb-8 p-5 bg-emerald-50 dark:bg-emerald-500/10 border-2 border-emerald-200 dark:border-emerald-500/20 rounded-2xl flex items-center gap-4 text-emerald-600 dark:text-emerald-400 shadow-sm animate-in fade-in slide-in-from-top-4 duration-500">
                        <CheckCircle2 className="w-6 h-6 shrink-0 text-emerald-500" />
                        <div>
                            <p className="font-black text-sm uppercase tracking-widest text-emerald-700 dark:text-emerald-400">Thanh toán thành công!</p>
                            <p className="text-sm font-bold opacity-80 mt-0.5">Vé của bạn đã sẵn sàng trong danh sách bên dưới.</p>
                        </div>
                    </div>
                )}

                {payments.length === 0 ? (
                    <div className="bg-white dark:bg-slate-900 rounded-[2rem] p-16 text-center border-2 border-slate-200 dark:border-slate-800 shadow-xl transition-colors">
                        <div className="w-24 h-24 bg-slate-50 dark:bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-6 shadow-inner">
                            <Ticket className="w-12 h-12 text-slate-400 dark:text-slate-500" />
                        </div>
                        <h3 className="text-2xl font-black mb-3 italic tracking-tight text-slate-800 dark:text-white uppercase">Chưa có giao dịch</h3>
                        <p className="text-slate-500 dark:text-slate-400 mb-8 max-w-sm mx-auto font-bold tracking-tight">Bạn chưa thực hiện bất kỳ giao dịch nào. Hãy chọn bộ phim yêu thích và đặt vé ngay!</p>
                        <button
                            onClick={() => navigate('/movies')}
                            className="group relative px-10 py-4 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-2xl font-black uppercase tracking-widest transition-all flex items-center gap-2 mx-auto overflow-hidden shadow-xl shadow-orange-500/20 hover:scale-[1.02]"
                        >
                            <span className="relative z-10 text-xs">ĐẶT VÉ NGAY</span>
                            <ArrowRight className="w-5 h-5 relative z-10 group-hover:translate-x-1 transition-transform" />
                        </button>
                    </div>
                ) : (
                    <div className="grid gap-6">
                        {payments.map((payment) => {
                            const status = getStatusInfo(payment.status);
                            const movie = payment.booking?.showtime?.movie;
                            
                            return (
                                <div
                                    key={payment.paymentId}
                                    onClick={() => navigate(`/profile/transaction/${payment.paymentId}`)}
                                    className="group bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800 rounded-[2rem] overflow-hidden border-2 border-slate-200 dark:border-slate-800 transition-all duration-300 cursor-pointer shadow-lg hover:shadow-xl hover:border-orange-500/50 dark:hover:border-orange-500/50 active:scale-[0.98]"
                                >
                                    <div className="flex flex-col sm:flex-row h-full">
                                        <div className="w-full sm:w-44 h-48 sm:h-auto overflow-hidden relative flex-shrink-0 border-b-2 sm:border-b-0 sm:border-r-2 border-slate-100 dark:border-slate-800">
                                            <img 
                                                src={movie?.posterUrl || 'https://via.placeholder.com/300x450?text=Movie+Poster'} 
                                                alt={movie?.title}
                                                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                                            />
                                        </div>

                                        <div className="flex-1 p-8 flex flex-col justify-between">
                                            <div className="flex flex-col sm:flex-row justify-between items-start gap-4">
                                                <div className="flex-1">
                                                    <div className="flex items-center gap-3 mb-3">
                                                        <span className="px-2.5 py-1 bg-gradient-to-r from-orange-500 to-red-500 text-white text-[10px] font-black rounded uppercase shadow-sm">ORD-{payment.booking?.bookingId}</span>
                                                        <span className="text-[10px] text-slate-500 dark:text-slate-400 uppercase font-black tracking-widest px-2 py-1 bg-slate-100 dark:bg-slate-800 rounded">
                                                            {payment.paymentMethod}
                                                        </span>
                                                    </div>
                                                    <h3 className="text-2xl sm:text-3xl font-black mb-4 group-hover:text-orange-500 transition-colors leading-tight tracking-tight text-slate-800 dark:text-white uppercase line-clamp-2">
                                                        {movie?.title || 'Đơn hàng dịch vụ'}
                                                    </h3>
                                                    <div className="flex flex-wrap items-center gap-6 text-sm text-slate-500 dark:text-slate-400 font-bold tracking-tight">
                                                        <div className="flex items-center gap-2">
                                                            <Calendar className="w-5 h-5 text-orange-500" />
                                                            {payment.paidAt ? format(new Date(payment.paidAt), 'dd/MM/yyyy • HH:mm', { locale: vi }) : '---'}
                                                        </div>
                                                        <div className="flex items-center gap-2">
                                                            <Wallet className="w-5 h-5 text-orange-500" />
                                                            <span className="text-slate-800 dark:text-white text-base">
                                                                {payment.amount.toLocaleString('vi-VN')}đ
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                                
                                                <div className={`shrink-0 px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest flex items-center gap-2 ${status.bg} ${status.color} border border-current shadow-sm`}>
                                                    {status.icon}
                                                    {status.label}
                                                </div>
                                            </div>

                                            <div className="mt-6 pt-5 border-t-2 border-slate-100 dark:border-slate-800 flex justify-between items-center">
                                                <div className="text-[11px] text-slate-500 font-black uppercase tracking-widest group-hover:text-orange-500 transition-colors flex items-center gap-2">
                                                    Xem chi tiết vé <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
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
