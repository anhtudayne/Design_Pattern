import { Link } from 'react-router-dom';
import { useState, useEffect } from 'react';

const StaffHeader = () => {
  const [time, setTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (d) =>
    d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const formatDate = (d) =>
    d.toLocaleDateString('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' });

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
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-orange-500 to-red-500 flex items-center justify-center text-white text-xs font-black">
            S
          </div>
          <span className="text-xs font-bold text-slate-600 dark:text-slate-300 hidden md:inline">Staff</span>
        </div>
      </div>
    </header>
  );
};

export default StaffHeader;
