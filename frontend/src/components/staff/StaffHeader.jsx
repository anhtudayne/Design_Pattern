import { Link, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../../store/authSlice';

const StaffHeader = () => {
  const [time, setTime] = useState(new Date());
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const { user } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (d) =>
    d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const formatDate = (d) =>
    d.toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' });

  const handleLogout = () => {
    dispatch(logout());
    setIsUserMenuOpen(false);
    navigate('/');
  };

  return (
    <header className="fixed top-0 left-0 right-0 z-40 h-14 bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl border-b border-slate-200/20 dark:border-slate-800/20 flex items-center justify-between px-6 font-['Space_Grotesk']">
      {/* Left: Brand */}
      <Link to="/staff/pos" className="flex items-center gap-2.5 group">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-yellow-300 via-orange-400 to-red-500 flex items-center justify-center shadow-md shadow-orange-200 group-hover:scale-110 transition-transform">
          <span className="material-symbols-outlined text-white text-lg">point_of_sale</span>
        </div>
        <div>
          <h1 className="text-sm font-black tracking-tight text-slate-800 dark:text-white leading-none">StarCine</h1>
          <p className="text-[9px] text-slate-400 font-semibold tracking-widest uppercase">POS Terminal</p>
        </div>
      </Link>

      {/* Center: Cinema name */}
      <div className="hidden md:flex items-center gap-2 text-sm text-slate-500 dark:text-slate-400">
        <span className="material-symbols-outlined text-orange-500 text-base">store</span>
        <span className="font-semibold">Rạp StarCine — Quầy POS</span>
      </div>

      {/* Right: Clock + Profile */}
      <div className="flex items-center gap-5">
        <div className="text-right hidden sm:block">
          <p className="text-xs font-black text-slate-800 dark:text-white tabular-nums tracking-tight">{formatTime(time)}</p>
          <p className="text-[9px] text-slate-400 font-medium">{formatDate(time)}</p>
        </div>
        <div className="h-6 w-px bg-slate-200 dark:bg-slate-700"></div>
        
        <div className="relative">
          <button 
            onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
            className="flex items-center gap-2 group p-1 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 transition-all"
          >
            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-orange-500 to-red-500 flex items-center justify-center text-white text-xs font-black shadow-md shadow-orange-500/20 group-hover:scale-105 transition-transform">
              {user?.fullname?.charAt(0)?.toUpperCase() || user?.username?.charAt(0)?.toUpperCase() || 'S'}
            </div>
            <div className="hidden md:block text-left mr-1">
              <p className="text-[10px] font-black text-slate-800 dark:text-white leading-none">{user?.fullname || user?.username || 'Staff'}</p>
              <p className="text-[8px] text-orange-500 font-bold uppercase tracking-widest mt-0.5">Nhân viên</p>
            </div>
            <span className={`material-symbols-outlined text-slate-400 text-sm transition-transform ${isUserMenuOpen ? 'rotate-180' : ''}`}>expand_more</span>
          </button>

          {isUserMenuOpen && (
            <div className="absolute top-full right-0 mt-2 w-56 bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-100 dark:border-slate-800 overflow-hidden z-50 animate-[fadeIn_0.2s_ease-out]">
              <div className="px-5 py-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/30">
                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1">Tài khoản Staff</p>
                <p className="text-sm font-black text-slate-800 dark:text-white truncate">{user?.username}</p>
                <p className="text-[10px] text-slate-500 truncate">{user?.email}</p>
              </div>

              <div className="p-2">
                <button
                  onClick={() => {
                    setIsUserMenuOpen(false);
                    navigate('/staff/pos');
                  }}
                  className="w-full flex items-center gap-3 px-4 py-2.5 text-left text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-all text-sm font-bold group"
                >
                  <div className="w-8 h-8 rounded-lg bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center text-blue-600 group-hover:scale-110 transition-transform">
                    <span className="material-symbols-outlined text-lg">dashboard</span>
                  </div>
                  <span>Trang chủ POS</span>
                </button>

                <button
                  onClick={handleLogout}
                  className="w-full flex items-center gap-3 px-4 py-2.5 text-left text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 rounded-xl transition-all text-sm font-bold group"
                >
                  <div className="w-8 h-8 rounded-lg bg-red-100 dark:bg-red-900/30 flex items-center justify-center text-red-600 group-hover:scale-110 transition-transform">
                    <span className="material-symbols-outlined text-lg">logout</span>
                  </div>
                  <span>Đăng xuất</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default StaffHeader;

