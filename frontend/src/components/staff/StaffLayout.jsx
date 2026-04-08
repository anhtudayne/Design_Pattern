import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useEffect, useCallback } from 'react';
import StaffHeader from './StaffHeader';

const TABS = [
  { label: 'Bán Vé',   path: '/staff/pos',    icon: 'point_of_sale',       hotkey: 'F1' },
  { label: 'F&B',      path: '/staff/fnb',    icon: 'fastfood',            hotkey: 'F2' },
  { label: 'Tra Cứu',  path: '/staff/lookup', icon: 'search',              hotkey: 'F3' },
];

const StaffLayout = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // ── Hotkey navigation (F1-F4) ───────────────────────────────────────
  const handleKeyDown = useCallback((e) => {
    const tab = TABS.find(t => t.hotkey === e.key);
    if (tab) {
      e.preventDefault();
      navigate(tab.path);
    }
  }, [navigate]);

  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);

  return (
    <div className="antialiased selection:bg-orange-500/30 bg-surface dark:bg-slate-950 min-h-screen font-['Space_Grotesk']">
      <StaffHeader />

      {/* ── Tab Bar ─────────────────────────────────────────────── */}
      <nav className="fixed top-14 left-0 right-0 z-30 h-14 bg-white/80 dark:bg-slate-900/80 backdrop-blur-xl border-b border-slate-200/20 dark:border-slate-800/20 flex items-center justify-center gap-1 px-4">
        {TABS.map((tab) => {
          const isActive = location.pathname === tab.path ||
            (tab.path === '/staff/pos' && location.pathname === '/staff');
          return (
            <Link
              key={tab.path}
              to={tab.path}
              className={`relative flex items-center gap-2 px-5 py-2 rounded-xl text-sm font-bold transition-all duration-200 select-none ${
                isActive
                  ? 'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-lg shadow-orange-500/30'
                  : 'text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-white hover:bg-slate-100/70 dark:hover:bg-slate-800/50'
              }`}
            >
              <span className={`material-symbols-outlined text-lg ${isActive ? 'text-white' : ''}`}>
                {tab.icon}
              </span>
              <span className="hidden sm:inline">{tab.label}</span>
              <span className={`text-[9px] font-mono ml-1 px-1.5 py-0.5 rounded ${
                isActive
                  ? 'bg-white/20 text-white/80'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-400'
              }`}>
                {tab.hotkey}
              </span>
            </Link>
          );
        })}
      </nav>

      {/* ── Main Content ───────────────────────────────────────── */}
      <main className="pt-28 pb-4 px-4 md:px-6 min-h-screen">
        <Outlet />
      </main>
    </div>
  );
};

export default StaffLayout;
