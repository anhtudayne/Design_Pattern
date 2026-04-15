import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../store/authSlice';
import { fetchCinemas, fetchLocations } from '../services/cinemaService';

export default function Header() {
  const [scrolled, setScrolled] = useState(false);
  const [isCinemaMenuOpen, setIsCinemaMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  
  const [cinemas, setCinemas] = useState([]);
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);

  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  // Redux auth state
  const { user, token } = useSelector((state) => state.auth);
  const isLoggedIn = !!token;

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 10);
    };
    window.addEventListener('scroll', handleScroll);

    const loadHeaderData = async () => {
      try {
        const [cinemaData, locationData] = await Promise.all([
          fetchCinemas(),
          fetchLocations()
        ]);
        setCinemas(cinemaData);
        setLocations(locationData);
      } catch (err) {
        console.error('Failed to load header data:', err);
      } finally {
        setLoading(false);
      }
    };

    loadHeaderData();
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const isActive = (path) => location.pathname === path;

  // Group cinemas by locationId for the dropdown
  const groupedCinemas = locations.map(loc => ({
    ...loc,
    cinemas: cinemas.filter(c => c.locationId === loc.locationId)
  })).filter(loc => loc.cinemas.length > 0);

  return (
    <header 
      className={`fixed top-0 w-full z-50 transition-all duration-500 ${
        scrolled 
          ? 'bg-white/80 dark:bg-slate-900/80 backdrop-blur-2xl shadow-[0_10px_40px_-10px_rgba(0,0,0,0.08)] py-2 border-b border-white/20'
          : 'bg-white/50 dark:bg-slate-900/50 backdrop-blur-md shadow-sm py-4 border-b border-transparent'
      }`}
    >
      {/* Container */}
      <div className="max-w-[1440px] mx-auto px-6 md:px-8 space-y-4">
        
        {/* Layer 1: Brand & Key CTAs */}
        <div className="flex justify-between items-center">
          
          {/* Logo */}
          <Link to="/" className="group flex items-center gap-2">
            <div className="relative w-10 h-10 flex items-center justify-center bg-gradient-to-tr from-yellow-300 via-orange-400 to-red-500 rounded-xl shadow-[0_5px_15px_rgba(249,115,22,0.4)] overflow-hidden group-hover:scale-105 transition-all duration-300 border border-white/20">
              <div className="absolute inset-0 bg-white/30 transform -skew-x-12 -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
              {/* Fake 3D Star Effect using drop shadow */}
              <span className="material-symbols-outlined text-white text-[28px] drop-shadow-[1px_2px_1px_rgba(0,0,0,0.5)] group-hover:rotate-180 transition-transform duration-700">stars</span>
            </div>
            <span className="text-2xl font-black tracking-tighter text-slate-800 dark:text-white">
              Star<span className="text-orange-500">Cine</span>
            </span>
          </Link>
          
          {/* Focal Point CTAs */}
          <div className="hidden md:flex items-center gap-3">
            <Link to="/booking/snacks" className="group relative px-5 py-2.5 rounded-full bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 font-bold flex items-center gap-2 transition-all duration-300">
              <span className="material-symbols-outlined text-lg group-hover:scale-110 transition-transform text-slate-500">fastfood</span>
              <span className="text-sm">ĐẶT BẮP NƯỚC</span>
            </Link>
            
            <Link to="/movies" className="group relative px-6 py-2.5 rounded-full bg-gradient-to-r from-orange-500 to-red-500 text-white font-bold flex items-center gap-2 transition-all duration-300 shadow-[0_8px_20px_rgba(249,115,22,0.3)] hover:shadow-[0_8px_25px_rgba(249,115,22,0.5)] hover:-translate-y-0.5 overflow-hidden">
              <div className="absolute inset-0 w-full h-full bg-gradient-to-r from-transparent via-white/30 to-transparent -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
              <span className="material-symbols-outlined text-lg animate-pulse">confirmation_number</span>
              <span className="text-sm whitespace-nowrap">ĐẶT VÉ NGAY</span>
            </Link>
          </div>

          {/* Utils */}
          <div className="flex items-center gap-2">
            <button className="w-10 h-10 rounded-full flex items-center justify-center text-slate-600 hover:bg-slate-100 hover:text-orange-500 transition-colors">
              <span className="material-symbols-outlined">search</span>
            </button>
            <button className="w-10 h-10 rounded-full flex items-center justify-center text-slate-600 hover:bg-slate-100 hover:text-orange-500 transition-colors">
              <span className="material-symbols-outlined">notifications</span>
            </button>

            {isLoggedIn ? (
              <div className="relative ml-2">
                <button
                  onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                  className="flex items-center gap-2 px-3 py-1.5 rounded-full border border-orange-200 bg-orange-50 hover:bg-orange-100 text-orange-600 transition-all font-medium text-sm group"
                >
                  <div className="w-7 h-7 rounded-full bg-gradient-to-br from-orange-400 to-red-500 flex items-center justify-center text-white text-xs font-black">
                    {user?.fullname?.charAt(0)?.toUpperCase() || user?.username?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  <span className="hidden md:inline max-w-[100px] truncate">{user?.fullname || user?.username || 'User'}</span>
                  <span className={`material-symbols-outlined text-sm transition-transform ${isUserMenuOpen ? 'rotate-180' : ''}`}>expand_more</span>
                </button>

                {isUserMenuOpen && (
                  <div className="absolute top-full right-0 mt-2 w-56 bg-white dark:bg-slate-900 rounded-2xl shadow-2xl border border-slate-100 dark:border-slate-800 overflow-hidden z-50 animate-[fadeIn_0.2s_ease-out]">
                    <div className="px-5 py-4 border-b border-slate-100 dark:border-slate-800">
                      <p className="text-[11px] font-bold text-slate-400 uppercase tracking-[0.1em] mb-0.5">Tài khoản</p>
                      <p className="text-sm font-bold text-slate-800 dark:text-white truncate">{user?.email || ''}</p>
                    </div>

                    <div className="py-2">
                      {isAdmin && (
                        <Link 
                          to="/admin" 
                          onClick={() => setIsUserMenuOpen(false)}
                          className="w-full flex items-center gap-3 px-5 py-3 text-left text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all text-sm font-bold group"
                        >
                          <div className="w-8 h-8 rounded-lg bg-orange-100 dark:bg-orange-900/30 flex items-center justify-center text-orange-600 group-hover:scale-110 transition-transform">
                            <span className="material-symbols-outlined text-lg">admin_panel_settings</span>
                          </div>
                          <span>Trang quản trị (Admin)</span>
                        </Link>
                      )}

                      <Link 
                        to="/profile/transactions" 
                        onClick={() => setIsUserMenuOpen(false)}
                        className="w-full flex items-center gap-3 px-5 py-3 text-left text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all text-sm font-bold group"
                      >
                        <div className="w-8 h-8 rounded-lg bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center text-blue-600 group-hover:scale-110 transition-transform">
                          <span className="material-symbols-outlined text-lg">history</span>
                        </div>
                        <span>Lịch sử giao dịch</span>
                      </Link>

                      <button
                        onClick={() => {
                          dispatch(logout());
                          setIsUserMenuOpen(false);
                          navigate('/');
                        }}
                        className="w-full flex items-center gap-3 px-5 py-3 text-left text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 transition-all text-sm font-bold group"
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
            ) : (
              <Link to="/login" id="btn-header-login" className="ml-2 flex items-center justify-center gap-2 px-3 py-1.5 rounded-full border border-slate-200 hover:border-orange-500 text-slate-600 hover:text-orange-500 transition-all font-medium text-sm group">
                <span className="material-symbols-outlined text-xl group-hover:scale-110 transition-transform">account_circle</span>
                <span>Đăng nhập</span>
              </Link>
            )}
          </div>
        </div>
        
        {/* Layer 2: Navigation Links */}
        <nav className={`flex justify-between items-center text-sm font-bold transition-all duration-500 ${scrolled ? 'h-0 opacity-0 overflow-hidden' : 'h-10 opacity-100'}`}>
          <div className="flex gap-8 relative">
            <div 
              className="relative group"
              onMouseEnter={() => setIsCinemaMenuOpen(true)}
              onMouseLeave={() => setIsCinemaMenuOpen(false)}
            >
              <button className={`relative py-2 text-slate-600 hover:text-orange-500 transition-colors uppercase flex items-center gap-1 font-bold ${isActive('/booking/seats') ? 'text-orange-500' : ''}`}>
                CHỌN RẠP
                <span className={`material-symbols-outlined text-sm transition-transform duration-300 ${isCinemaMenuOpen ? 'rotate-180' : ''}`}>expand_more</span>
                <span className={`absolute bottom-0 left-0 w-full h-0.5 bg-orange-500 transform origin-left transition-transform duration-300 ${isActive('/booking/seats') || isCinemaMenuOpen ? 'scale-x-100' : 'scale-x-0'}`}></span>
              </button>

              {/* Mega Dropdown Panel */}
              <div className={`absolute top-full left-0 pt-4 w-[600px] transition-all duration-300 origin-top shadow-2xl ${isCinemaMenuOpen ? 'opacity-100 scale-y-100' : 'opacity-0 scale-y-0 pointer-events-none'}`}>
                <div className="bg-white/95 dark:bg-slate-900/95 backdrop-blur-xl rounded-2xl shadow-[0_20px_60px_-15px_rgba(0,0,0,0.3)] border border-slate-200/50 dark:border-slate-800/50 p-6 flex flex-col gap-6">
                  <div className="flex justify-between items-center border-b border-slate-100 dark:border-slate-800 pb-3">
                    <h4 className="text-sm font-bold text-slate-500 uppercase tracking-widest">Hệ thống rạp StarCine</h4>
                    <span className="text-xs bg-orange-100 text-orange-600 px-2 py-1 rounded font-bold">
                      {loading ? '...' : `${cinemas.length} Rạp đang hoạt động`}
                    </span>
                  </div>
                  
                  {loading ? (
                    <div className="flex justify-center py-10">
                      <div className="animate-spin w-6 h-6 border-2 border-orange-500 border-t-transparent rounded-full"></div>
                    </div>
                  ) : (
                    <div className="grid grid-cols-2 gap-x-8 gap-y-6 max-h-[400px] overflow-y-auto pr-2 custom-scrollbar">
                      {groupedCinemas.length > 0 ? (
                        groupedCinemas.map(loc => (
                          <div key={loc.locationId}>
                            <h5 className="text-[11px] font-black text-orange-500 mb-4 border-l-2 border-orange-500 pl-2 uppercase tracking-wider">{loc.name}</h5>
                            <ul className="space-y-4">
                              {loc.cinemas.map(cinema => (
                                <li key={cinema.cinemaId}>
                                  <Link to={`/cinema/${cinema.cinemaId}`} className="flex items-start gap-2 group/item">
                                    <span className="material-symbols-outlined text-slate-300 group-hover/item:text-orange-500 transition-colors mt-0.5 text-lg">location_on</span>
                                    <div>
                                      <p className="text-sm font-bold text-slate-700 group-hover/item:text-orange-600 transition-colors leading-snug">{cinema.name}</p>
                                      <p className="text-[11px] font-medium text-slate-400 group-hover/item:text-slate-500 transition-colors line-clamp-1">{cinema.address}</p>
                                    </div>
                                  </Link>
                                </li>
                              ))}
                            </ul>
                          </div>
                        ))
                      ) : (
                        <div className="col-span-2 text-center py-6 text-slate-400 font-medium italic">
                          Không tìm thấy dữ liệu rạp chiếu
                        </div>
                      )}
                    </div>
                  )}
                  
                  <div className="pt-2">
                    <Link to="/booking/seats" className="w-full text-center block py-3 bg-slate-50 dark:bg-slate-800 hover:bg-orange-500 hover:text-white text-orange-600 rounded-xl text-xs font-black transition-all uppercase tracking-widest">
                      Xem tất cả hệ thống rạp
                    </Link>
                  </div>
                </div>
              </div>
            </div>
            <Link 
              to="/movies" 
              className={`relative py-2 text-slate-600 hover:text-orange-500 transition-colors group ${isActive('/movies') ? 'text-orange-500' : ''}`}
            >
              LỊCH CHIẾU
              <span className={`absolute bottom-0 left-0 w-full h-0.5 bg-orange-500 transform origin-left transition-transform duration-300 ${isActive('/movies') ? 'scale-x-100' : 'scale-x-0 group-hover:scale-x-100'}`}></span>
            </Link>
          </div>
          <div className="flex gap-8 text-slate-500 font-medium">
            <a className="relative py-2 hover:text-orange-500 transition-colors group flex items-center gap-1" href="#">
              Khuyến mãi
              <span className="absolute -top-1 -right-4 flex h-3 w-3">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3 w-3 bg-red-500"></span>
              </span>
            </a>
            <a className="relative py-2 hover:text-orange-500 transition-colors" href="#">Tổ chức sự kiện</a>
            <a className="relative py-2 hover:text-orange-500 transition-colors" href="#">Dịch vụ giải trí khác</a>
            <a className="relative py-2 hover:text-orange-500 transition-colors" href="#">Giới thiệu</a>
          </div>
        </nav>

      </div>
    </header>
  );
}
