import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { logout } from '../../store/authSlice';

const AdminSidebar = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const handleLogout = () => {
    dispatch(logout());
    navigate('/');
  };

  const sections = [
    {
      label: 'Tổng quan',
      links: [
        { name: 'Dashboard',    path: '/admin/dashboard',  icon: 'dashboard' },
        // { name: 'Analytics',    path: '/admin/analytics',  icon: 'analytics' },
      ],
    },
    {
      label: 'Quản lý',
      links: [
        { name: 'Phim',                    path: '/admin/management',  icon: 'movie'         },
        { name: 'Cơ sở & Phòng chiếu',    path: '/admin/facilities',  icon: 'business'      },
        { name: 'Nghệ sĩ',                path: '/admin/artists',     icon: 'groups'        },
        { name: 'F&B / Đồ ăn',            path: '/admin/fnb',         icon: 'fastfood'      },
        { name: 'Lịch chiếu',             path: '/admin/showtimes',   icon: 'event'         },
        // { name: 'Đặt vé',                 path: '/admin/bookings',    icon: 'confirmation_number' },
        { name: 'Khuyến mãi',             path: '/admin/vouchers',    icon: 'local_offer'   },
        // { name: 'Nhân viên',              path: '/admin/staff',       icon: 'badge'         },
      ],
    },
    {
      label: 'Hệ thống',
      links: [
        // { name: 'Cài đặt', path: '/admin/settings', icon: 'settings' },
      ],
    },
  ];

  return (
    <aside className="fixed left-0 top-0 h-full z-40 flex flex-col w-64 border-r border-slate-200/20 dark:border-slate-800/20 bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl shadow-[20px_0_30px_-15px_rgba(24,28,32,0.05)] font-['Space_Grotesk'] antialiased">
      
      {/* Brand */}
      <div className="px-6 py-7 border-b border-slate-100/60">
        <Link to="/" className="flex items-center gap-2.5 group/logo">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-yellow-300 via-orange-400 to-red-500 flex items-center justify-center shadow-md shadow-orange-200 group-hover/logo:scale-110 transition-transform">
            <span className="material-symbols-outlined text-white text-lg">stars</span>
          </div>
          <div>
            <h1 className="text-lg font-black tracking-tight text-slate-800 group-hover/logo:text-orange-600 transition-colors">StarCine</h1>
            <p className="text-[10px] text-slate-400 font-semibold tracking-widest uppercase -mt-0.5">Admin Panel</p>
          </div>
        </Link>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 py-4 space-y-5 overflow-y-auto">
        {sections.map((section) => (
          <div key={section.label}>
            <p className="px-3 mb-1.5 text-[10px] font-bold text-slate-400 uppercase tracking-widest">
              {section.label}
            </p>
            <div className="space-y-0.5">
              {section.links.map((link) => {
                const isActive = location.pathname.startsWith(link.path);
                return (
                  <Link
                    key={link.name}
                    to={link.path}
                    className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 text-sm ${
                      isActive
                        ? 'text-orange-600 font-bold bg-gradient-to-r from-orange-50 to-red-50 border border-orange-100 shadow-sm'
                        : 'text-slate-500 font-medium hover:text-slate-800 hover:bg-slate-100/70'
                    }`}
                  >
                    <span className={`material-symbols-outlined text-[20px] transition-colors ${isActive ? 'text-orange-500' : 'text-slate-400'}`}>
                      {link.icon}
                    </span>
                    <span>{link.name}</span>
                    {isActive && (
                      <span className="ml-auto w-1.5 h-1.5 rounded-full bg-orange-500" />
                    )}
                  </Link>
                );
              })}
            </div>
          </div>
        ))}
      </nav>

      {/* Footer */}
      <div className="p-4 border-t border-slate-100/60">
        <button
          type="button"
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-slate-500 hover:text-red-500 hover:bg-red-50 transition-all text-sm font-medium group"
        >
          <span className="material-symbols-outlined text-[20px] group-hover:text-red-500 transition-colors">logout</span>
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>
  );
};

export default AdminSidebar;

