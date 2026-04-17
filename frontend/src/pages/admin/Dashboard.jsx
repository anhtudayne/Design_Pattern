import { useState, useEffect } from 'react';
import { getAuthHeaders, BASE_URL } from '../../utils/api';
import * as XLSX from 'xlsx';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalMovies: 0,
    totalUsers: 0,
    totalShowtimes: 0,
    totalFnbItems: 0,
    totalPromotions: 0,
    totalRevenue: 0,
    totalTickets: 0
  });
  const [weeklyRevenue, setWeeklyRevenue] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isPanelOpen, setIsPanelOpen] = useState(false);
  const [panelType, setPanelType] = useState(''); // 'screening', 'roster', 'report'

  useEffect(() => {
    Promise.all([
      fetch(`${BASE_URL}/admin/dashboard/stats`, { headers: getAuthHeaders() }),
      fetch(`${BASE_URL}/admin/dashboard/revenue-weekly`, { headers: getAuthHeaders() })
    ])
      .then(async ([rStats, rWeekly]) => {
        const statsData = rStats.ok ? await rStats.json() : null;
        const weeklyData = rWeekly.ok ? await rWeekly.json() : [];
        if (statsData) setStats(statsData);
        setWeeklyRevenue(weeklyData);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const openPanel = (type) => {
    setPanelType(type);
    setIsPanelOpen(true);
  };

  const closePanel = () => {
    setIsPanelOpen(false);
    setTimeout(() => setPanelType(''), 300);
  };

  const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

  const exportToExcel = () => {
    try {
      const wb = XLSX.utils.book_new();

      // 1. Weekly Revenue Sheet with Calculations
      const maxRevenue = Math.max(...weeklyRevenue.map(v => v.amount), 1);
      const totalWeekly = weeklyRevenue.reduce((a, b) => a + b.amount, 0);
      
      const weeklyData = weeklyRevenue.map(d => ({
        'Thứ': d.day,
        'Doanh Thu (VND)': d.amount,
        'Tỷ lệ trong tuần (%)': ((d.amount / totalWeekly) * 100).toFixed(1),
        'Hiệu suất ngày (%)': ((d.amount / maxRevenue) * 100).toFixed(1)
      }));
      
      const wsWeekly = XLSX.utils.json_to_sheet(weeklyData);
      XLSX.utils.book_append_sheet(wb, wsWeekly, "Doanh Thu Tuần");

      // 2. Comprehensive System Overview Sheet
      const statsData = [
        { 'Hạng Mục': '--- TÀI CHÍNH ---', 'Giá Trị': '' },
        { 'Hạng Mục': 'Tổng doanh thu hệ thống', 'Giá Trị': formatMoney(stats.totalRevenue) },
        { 'Hạng Mục': 'Doanh thu trung bình/vé', 'Giá Trị': formatMoney(stats.totalTickets > 0 ? stats.totalRevenue / stats.totalTickets : 0) },
        { 'Hạng Mục': '', 'Giá Trị': '' },
        { 'Hạng Mục': '--- HOẠT ĐỘNG ---', 'Giá Trị': '' },
        { 'Hạng Mục': 'Tổng số vé đã bán', 'Giá Trị': stats.totalTickets.toLocaleString() + ' vé' },
        { 'Hạng Mục': 'Tổng số suất chiếu', 'Giá Trị': stats.totalShowtimes + ' buổi' },
        { 'Hạng Mục': 'Số lượng phim đang mở', 'Giá Trị': stats.totalMovies + ' phim' },
        { 'Hạng Mục': '', 'Giá Trị': '' },
        { 'Hạng Mục': '--- CƠ SỞ DỮ LIỆU ---', 'Giá Trị': '' },
        { 'Hạng Mục': 'Tổng số khách hàng', 'Giá Trị': stats.totalUsers.toLocaleString() + ' thành viên' },
        { 'Hạng Mục': 'Số lượng món ăn F&B', 'Giá Trị': stats.totalFnbItems + ' món' },
        { 'Hạng Mục': 'Số lượng mã khuyến mãi (DB)', 'Giá Trị': stats.totalPromotions + ' mã' },
        { 'Hạng Mục': '', 'Giá Trị': '' },
        { 'Hạng Mục': '--- THÔNG TIN XUẤT FILE ---', 'Giá Trị': '' },
        { 'Hạng Mục': 'Ngày xuất báo cáo', 'Giá Trị': new Date().toLocaleDateString('vi-VN') },
        { 'Hạng Mục': 'Thời gian chi tiết', 'Giá Trị': new Date().toLocaleTimeString('vi-VN') },
        { 'Hạng Mục': 'Hệ thống nguồn', 'Giá Trị': 'STARCINE ADMIN DASHBOARD' }
      ];
      
      const wsSummary = XLSX.utils.json_to_sheet(statsData);
      XLSX.utils.book_append_sheet(wb, wsSummary, "Tổng Quan Hệ Thống");

      // Set column widths for better readability
      wsWeekly['!cols'] = [{ wch: 10 }, { wch: 25 }, { wch: 20 }, { wch: 20 }];
      wsSummary['!cols'] = [{ wch: 30 }, { wch: 40 }];

      // Download the file
      const fileName = `BaoCaoDoanhThu_StarCine_${new Date().toISOString().split('T')[0]}.xlsx`;
      XLSX.writeFile(wb, fileName);
    } catch (error) {
      console.error('Export Error:', error);
      alert('Đã xảy ra lỗi khi tạo file Excel. Vui lòng thử lại.');
    }
  };

  if (loading) {
    return (
      <div className="min-h-[80vh] flex flex-col items-center justify-center gap-6 animate-pulse">
        <div className="w-16 h-16 border-4 border-slate-100 border-t-indigo-600 rounded-full animate-spin shadow-inner" />
        <div className="flex flex-col items-center">
            <h2 className="text-xl font-black text-slate-800 uppercase tracking-widest font-['Space_Grotesk']">STARCINE ANALYTICS</h2>
            <p className="text-[10px] text-slate-400 font-bold uppercase tracking-[4px] mt-1 ml-1">Đang đồng bộ dữ liệu hệ thống...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="animate-in fade-in duration-500">
      {/* Header & Top Actions */}
      <section className="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h2 className="text-4xl font-black tracking-tight text-slate-800 mb-2 uppercase font-['Space_Grotesk']">Tổng Quan Hệ Thống</h2>
          <p className="text-slate-400 font-bold uppercase tracking-widest text-xs">
            Hệ thống quản trị rạp phim <span className="text-indigo-600">STARCINE</span> đang hoạt động ổn định.
          </p>
        </div>
        <div className="flex gap-3">
          <button onClick={() => openPanel('report')} className="px-6 py-3 rounded-2xl bg-white border border-slate-200 font-black text-xs uppercase tracking-widest flex items-center gap-2 hover:bg-slate-50 shadow-sm transition-all">
            <span className="material-symbols-outlined text-lg">download</span>
            Xuất Báo Cáo
          </button>
          <button onClick={() => openPanel('screening')} className="px-6 py-3 rounded-2xl bg-slate-900 text-white font-black text-xs uppercase tracking-widest flex items-center gap-2 shadow-2xl shadow-indigo-100 hover:bg-black active:scale-95 transition-all">
            <span className="material-symbols-outlined text-lg text-indigo-400">add_circle</span>
            Thêm Suất Chiêu
          </button>
        </div>
      </section>

      {/* Stats Bento Grid */}
      <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
        {/* Revenue Card */}
        <div className="bg-white p-7 rounded-[32px] shadow-sm border border-slate-100 relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-24 h-24 bg-indigo-50/50 rounded-bl-[100px] -mr-8 -mt-8 transition-transform group-hover:scale-110" />
          <div className="flex justify-between items-start mb-5 relative">
            <div className="p-3 bg-indigo-50 rounded-2xl text-indigo-600">
              <span className="material-symbols-outlined text-2xl">payments</span>
            </div>
            <span className="text-[10px] font-black text-green-600 bg-green-50 px-2.5 py-1 rounded-full uppercase tracking-tighter shadow-sm">+18.4%</span>
          </div>
          <p className="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1.5">Tổng Doanh Thu</p>
          <h3 className="text-2xl font-black text-slate-800 tracking-tighter">{formatMoney(stats.totalRevenue)}</h3>
        </div>
        
        {/* Tickets Sold */}
        <div className="bg-white p-7 rounded-[32px] shadow-sm border border-slate-100 relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-24 h-24 bg-orange-50/50 rounded-bl-[100px] -mr-8 -mt-8 transition-transform group-hover:scale-110" />
          <div className="flex justify-between items-start mb-5 relative">
            <div className="p-3 bg-orange-50 rounded-2xl text-orange-600">
              <span className="material-symbols-outlined text-2xl">confirmation_number</span>
            </div>
            <span className="text-[10px] font-black text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-full uppercase tracking-tighter shadow-sm">REALTIME</span>
          </div>
          <p className="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1.5">Vé Đã Bán</p>
          <h3 className="text-2xl font-black text-slate-800 tracking-tighter">{stats.totalTickets.toLocaleString()}</h3>
        </div>

        {/* Total Movies */}
        <div className="bg-white p-7 rounded-[32px] shadow-sm border border-slate-100 relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-24 h-24 bg-purple-50/50 rounded-bl-[100px] -mr-8 -mt-8 transition-transform group-hover:scale-110" />
          <div className="flex justify-between items-start mb-5 relative">
            <div className="p-3 bg-purple-50 rounded-2xl text-purple-600">
              <span className="material-symbols-outlined text-2xl">movie</span>
            </div>
            <span className="text-[10px] font-black text-slate-400 bg-slate-50 px-2.5 py-1 rounded-full uppercase tracking-tighter shadow-sm">ACTIVE</span>
          </div>
          <p className="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1.5">Tổng Số Phim</p>
          <h3 className="text-2xl font-black text-slate-800 tracking-tighter">{stats.totalMovies} Phim</h3>
        </div>

        {/* Total Users */}
        <div className="bg-white p-7 rounded-[32px] shadow-sm border border-slate-100 relative overflow-hidden group">
          <div className="absolute top-0 right-0 w-24 h-24 bg-blue-50/50 rounded-bl-[100px] -mr-8 -mt-8 transition-transform group-hover:scale-110" />
          <div className="flex justify-between items-start mb-5 relative">
            <div className="p-3 bg-blue-50 rounded-2xl text-blue-600">
              <span className="material-symbols-outlined text-2xl">group</span>
            </div>
            <span className="text-[10px] font-black text-green-600 bg-green-50 px-2.5 py-1 rounded-full uppercase tracking-tighter shadow-sm">+5.2%</span>
          </div>
          <p className="text-[11px] font-black text-slate-400 uppercase tracking-widest mb-1.5">Tổng Thành Viên</p>
          <h3 className="text-2xl font-black text-slate-800 tracking-tighter">{stats.totalUsers.toLocaleString()}</h3>
        </div>
      </section>

      {/* Main Container Section */}
      <section className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* System Summary Chart Faux */}
        <div className="lg:col-span-2 bg-white rounded-[40px] p-10 shadow-sm border border-slate-100">
           <div className="flex items-center justify-between mb-10">
              <div>
                <h4 className="text-2xl font-black text-slate-800 tracking-tighter uppercase leading-none mb-1">Hiệu suất rạp</h4>
                <p className="text-xs text-slate-400 font-bold uppercase tracking-widest italic">Theo dõi lưu lượng khách và doanh thu tuần</p>
              </div>
              <div className="flex gap-2">
                 <button className="w-10 h-10 rounded-xl bg-slate-50 text-slate-400 flex items-center justify-center hover:bg-indigo-50 hover:text-indigo-600 transition-all">
                    <span className="material-symbols-outlined text-xl">calendar_month</span>
                 </button>
              </div>
           </div>
           
            <div className="h-80 w-full relative flex items-end justify-between px-4 pb-2">
              {/* Faux Chart Bars - Normalized by Max Revenue */}
              {(() => {
                const maxVal = Math.max(...weeklyRevenue.map(d => d.amount), 1);
                return weeklyRevenue.map((d, i) => {
                  const percent = (d.amount / maxVal) * 100;
                  return (
                    <div key={i} className="group relative w-[10%] h-full flex flex-col items-center justify-end">
                       <div 
                         style={{ height: `${Math.max(percent, 15)}%` }} 
                         className="w-full bg-gradient-to-t from-indigo-500 to-blue-400 rounded-2xl transition-all duration-700 group-hover:scale-x-110 group-hover:brightness-110 relative shadow-lg shadow-indigo-100"
                       >
                          <div className="absolute -top-12 left-1/2 -translate-x-1/2 bg-slate-900 text-white text-[9px] font-black px-2.5 py-1.5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap shadow-xl z-20">
                             {formatMoney(d.amount)}
                          </div>
                       </div>
                       <span className="text-[10px] font-black text-slate-400 mt-4 uppercase tracking-tighter shrink-0">{d.day}</span>
                    </div>
                  );
                });
              })()}
              {/* Y-Axis Grid Lines */}
              <div className="absolute inset-0 pointer-events-none border-b border-slate-50" />
           </div>
        </div>

        {/* Breakdown Summary */}
        <div className="bg-white rounded-[40px] p-10 shadow-sm border border-slate-100 flex flex-col">
           <h4 className="text-2xl font-black text-slate-800 tracking-tighter uppercase leading-none mb-8">Danh mục con</h4>
           <div className="space-y-6 flex-1">
              <div className="flex items-center gap-4 p-5 rounded-3xl bg-slate-50 border border-slate-100/50">
                 <div className="w-12 h-12 rounded-2xl bg-orange-100 text-orange-600 flex items-center justify-center">
                    <span className="material-symbols-outlined">fastfood</span>
                 </div>
                 <div className="flex-1">
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest leading-none mb-1">Thực phẩm (F&B)</p>
                    <h5 className="text-lg font-black text-slate-700 tracking-tight">{stats.totalFnbItems} Món ăn</h5>
                 </div>
              </div>

              <div className="flex items-center gap-4 p-5 rounded-3xl bg-slate-50 border border-slate-100/50">
                 <div className="w-12 h-12 rounded-2xl bg-purple-100 text-purple-600 flex items-center justify-center">
                    <span className="material-symbols-outlined">confirmation_number</span>
                 </div>
                 <div className="flex-1">
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest leading-none mb-1">Khuyến mãi (Promotion)</p>
                    <h5 className="text-lg font-black text-slate-700 tracking-tight">{stats.totalPromotions} Mã</h5>
                 </div>
              </div>

              <div className="flex items-center gap-4 p-5 rounded-3xl bg-slate-50 border border-slate-100/50">
                 <div className="w-12 h-12 rounded-2xl bg-blue-100 text-blue-600 flex items-center justify-center">
                    <span className="material-symbols-outlined">event_seat</span>
                 </div>
                 <div className="flex-1">
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest leading-none mb-1">Lịch chiếu (Showtimes)</p>
                    <h5 className="text-lg font-black text-slate-700 tracking-tight">{stats.totalShowtimes} Buổi chiếu</h5>
                 </div>
              </div>
           </div>
           <button className="mt-8 w-full py-4 rounded-[22px] bg-indigo-50 text-indigo-600 text-[11px] font-black uppercase tracking-widest hover:bg-indigo-100 transition-all active:scale-95">
              Cập nhật dữ liệu mới
           </button>
        </div>
      </section>

      {/* Slide-over Panels (Same as before but cleaned up) */}
       {isPanelOpen && (
         <div className="fixed inset-0 z-[100] flex justify-end">
            <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-md animate-in fade-in duration-300" onClick={closePanel} />
            <div className="relative w-full max-w-lg bg-white h-full shadow-2xl flex flex-col animate-in slide-in-from-right duration-500 font-['Space_Grotesk']">
               <div className="p-8 border-b border-slate-50 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                     <div className="w-2 h-8 bg-indigo-600 rounded-full" />
                     <h3 className="text-2xl font-black text-slate-800 uppercase tracking-tighter">
                        {panelType === 'screening' && 'Tạo Suất Chiếu'}
                        {panelType === 'report' && 'Báo cáo doanh thu'}
                     </h3>
                  </div>
                  <button onClick={closePanel} className="w-10 h-10 rounded-2xl bg-slate-50 text-slate-400 hover:bg-slate-100 transition-colors flex items-center justify-center">
                     <span className="material-symbols-outlined">close</span>
                  </button>
               </div>
               <div className="flex-1 overflow-y-auto p-10">
                  {panelType === 'report' ? (
                     <div className="flex flex-col gap-8">
                        {/* Report Header Cards */}
                        <div className="grid grid-cols-2 gap-4">
                           <div className="bg-slate-50 p-5 rounded-3xl border border-slate-100">
                              <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Doanh thu tuần</p>
                              <h5 className="text-xl font-black text-slate-800">{formatMoney(weeklyRevenue.reduce((a, b) => a + b.amount, 0))}</h5>
                           </div>
                           <div className="bg-slate-50 p-5 rounded-3xl border border-slate-100">
                              <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Vé đã bán</p>
                              <h5 className="text-xl font-black text-slate-800">{stats.totalTickets.toLocaleString()}</h5>
                           </div>
                        </div>

                        {/* Interactive Export Button */}
                        <div className="p-8 bg-indigo-50/50 rounded-[40px] border border-indigo-100/50 flex flex-col items-center text-center">
                           <div className="w-20 h-20 bg-white rounded-3xl shadow-xl shadow-indigo-100 flex items-center justify-center text-indigo-600 mb-6 group cursor-pointer hover:scale-110 transition-transform duration-500">
                              <span className="material-symbols-outlined text-4xl">description</span>
                           </div>
                           <h6 className="text-lg font-black text-slate-800 uppercase tracking-tight mb-2">Xuất File Báo Cáo Excel</h6>
                           <p className="text-sm text-slate-400 font-medium mb-8">Hệ thống sẽ tự động tổng hợp dữ liệu doanh thu tuần, chỉ số vé và các danh mục con thành file .xlsx chuyên nghiệp.</p>
                           
                           <button 
                              onClick={exportToExcel}
                              className="w-full py-4 rounded-2xl bg-indigo-600 text-white text-xs font-black uppercase tracking-widest flex items-center justify-center gap-3 shadow-xl shadow-indigo-200 hover:bg-black active:scale-[0.98] transition-all"
                           >
                              <span className="material-symbols-outlined text-lg">download</span>
                              Tải Báo Cáo Ngay
                           </button>
                        </div>

                        <div className="flex flex-col gap-4">
                           <p className="text-[10px] font-black text-slate-400 uppercase tracking-[4px] text-center italic opacity-60">Dữ liệu được cập nhật thời gian thực</p>
                        </div>
                     </div>
                  ) : (
                     <div className="flex flex-col items-center justify-center text-center py-20">
                        <span className="material-symbols-outlined text-7xl text-slate-100 mb-6 font-thin">construction</span>
                        <p className="text-lg font-black text-slate-400 uppercase tracking-widest">Đang hoàn thiện tính năng này</p>
                        <p className="text-sm text-slate-300 mt-2">Dữ liệu thực tế đang được kết nối qua API...</p>
                     </div>
                  )}
               </div>
            </div>
         </div>
       )}
    </div>
  );
};

export default Dashboard;
