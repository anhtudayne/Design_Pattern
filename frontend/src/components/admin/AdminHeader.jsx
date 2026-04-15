import { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { selectCurrentUser, fetchProfile, logout } from '../../store/authSlice';

const AdminHeader = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector(selectCurrentUser);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  useEffect(() => {
    if (user && !user.fullname) {
      dispatch(fetchProfile());
    }
  }, [user, dispatch]);

  const displayFullName = user?.fullname || user?.email || 'Alex Sterling';
  const displayRole = user?.roles?.[0]?.replace('ROLE_', '') || 'Admin Principal';

  return (
    <header className="fixed top-0 right-0 left-64 h-16 flex justify-between items-center px-8 z-30 bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl border-b border-slate-200/20 font-['Space_Grotesk'] font-medium">
      <div className="flex items-center gap-6 w-1/3">
        <div className="relative w-full max-w-md">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-xl">search</span>
          <input
            className="w-full pl-10 pr-4 py-2 bg-surface-container-low border-none rounded-full text-sm focus:ring-2 focus:ring-orange-500 transition-all outline-none"
            placeholder="Search analytics, movies, or staff..."
            type="text"
          />
        </div>
      </div>

      <div className="flex items-center gap-4">
        <Link
          to="/"
          className="flex items-center gap-2 px-4 py-2 bg-slate-100 hover:bg-orange-500 hover:text-white text-slate-600 rounded-full text-xs font-bold transition-all duration-300 border border-slate-200 hover:border-orange-500"
        >
          <span className="material-symbols-outlined text-sm">open_in_new</span>
          Xem trang chủ
        </Link>

        <div className="h-6 w-px bg-slate-200 mx-2"></div>

        <button className="p-2 text-slate-500 hover:text-orange-500 transition-colors">
          <span className="material-symbols-outlined">notifications</span>
        </button>
        <button className="p-2 text-slate-500 hover:text-orange-500 transition-colors">
          <span className="material-symbols-outlined">settings</span>
        </button>

        <div className="h-8 w-px bg-surface-container-high mx-2"></div>

        <div className="relative">
          <button
            type="button"
            onClick={() => setIsUserMenuOpen((open) => !open)}
            className="flex items-center gap-3 cursor-pointer group text-left rounded-xl pr-1 pl-1 hover:bg-slate-100/80 dark:hover:bg-slate-800/50 transition-colors"
            aria-expanded={isUserMenuOpen}
            aria-haspopup="menu"
          >
            <div className="text-right hidden sm:block">
              <p className="text-xs font-bold text-on-surface group-hover:text-orange-600 transition-colors">{displayFullName}</p>
              <p className="text-[10px] text-on-surface-variant capitalize">{displayRole}</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-orange-500 flex items-center justify-center text-white font-bold ring-2 ring-orange-500/20 group-hover:ring-orange-500/50 transition-all overflow-hidden">
              {user?.fullname ? user.fullname.charAt(0).toUpperCase() : <span className="material-symbols-outlined">person</span>}
            </div>
            <span className={`material-symbols-outlined text-slate-400 text-sm transition-transform hidden sm:inline ${isUserMenuOpen ? 'rotate-180' : ''}`}>expand_more</span>
          </button>

          {isUserMenuOpen && (
            <div
              className="absolute top-full right-0 mt-2 w-56 bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-100 dark:border-slate-800 overflow-hidden z-50 animate-[fadeIn_0.2s_ease-out]"
              role="menu"
            >
              <div className="px-5 py-4 border-b border-slate-100 dark:border-slate-800">
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-[0.1em] mb-0.5">Tài khoản</p>
                <p className="text-sm font-bold text-slate-800 dark:text-white truncate">{user?.email || ''}</p>
              </div>
              <div className="py-2">
                <button
                  type="button"
                  onClick={() => {
                    dispatch(logout());
                    setIsUserMenuOpen(false);
                    navigate('/');
                  }}
                  className="w-full flex items-center gap-3 px-5 py-3 text-left text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-all text-sm font-bold group"
                  role="menuitem"
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

export default AdminHeader;
