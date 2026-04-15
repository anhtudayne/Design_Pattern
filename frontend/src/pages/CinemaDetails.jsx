import { useState, useEffect, useMemo } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useBooking } from '../contexts/BookingContext';
import { fetchCinemas } from '../services/cinemaService';
import { fetchPublicShowtimes } from '../services/showtimeService';

// ── Banner images for the carousel ──────────────────────────────────
const CINEMA_BANNERS = [
  "/cinema-banner.jpg",
  "/1_1700637478799.jpg",
  "/2_1700637484818.jpg"
];

// ── Template description for all cinemas ─────────────────────────────
const CINEMA_DESCRIPTION = `Chào mừng bạn đến với hệ thống rạp chiếu phim StarCine — nơi mang đến trải nghiệm điện ảnh đỉnh cao với công nghệ IMAX Laser và âm thanh Dolby Atmos 7.1 vòm. Không gian được thiết kế hiện đại, sang trọng với ghế ngồi Gold Class êm ái, phòng chiếu riêng tư và dịch vụ đặt bắp nước trực tuyến tiện lợi.

Tại StarCine, chúng tôi tin rằng điện ảnh không chỉ là giải trí — đó là nghệ thuật. Mỗi phòng chiếu đều được trang bị hệ thống âm thanh vòm Dolby Atmos 7.1 cùng màn hình IMAX Laser 4K, đảm bảo mỗi thước phim đều sống động và chân thực nhất.`;

// ── Facilities data ──────────────────────────────────────────────────
const FACILITIES = [
  { icon: 'movie_filter', name: 'IMAX Laser', desc: 'Màn hình khổng lồ, hình ảnh sắc nét 4K', gradient: 'from-orange-500 to-red-500' },
  { icon: 'surround_sound', name: 'Dolby Atmos 7.1', desc: 'Âm thanh vòm 360° sống động', gradient: 'from-cyan-500 to-blue-500' },
  { icon: 'workspace_premium', name: 'Gold Class', desc: 'Ghế da cao cấp, dịch vụ VIP', gradient: 'from-yellow-500 to-amber-600' },
  { icon: '4k', name: '4K Laser', desc: 'Độ phân giải siêu nét', gradient: 'from-violet-500 to-purple-600' },
  { icon: 'local_cafe', name: 'Star Lounge', desc: 'Quầy bar & snack cao cấp', gradient: 'from-emerald-500 to-teal-600' },
  { icon: 'accessible', name: 'Accessible', desc: 'Lối đi & ghế dành riêng', gradient: 'from-pink-500 to-rose-600' },
];

// ── Promotions data ──────────────────────────────────────────────────
const PROMOS = [
  { icon: 'fastfood', title: 'Combo Bắp Nước', desc: 'Giảm 20% khi mua combo bắp nước trực tuyến.', color: 'orange' },
  { icon: 'celebration', title: 'Happy Day Thứ Tư', desc: 'Đồng giá 55.000đ cho mọi suất chiếu 2D.', color: 'cyan' },
  { icon: 'school', title: 'Sinh Viên -30%', desc: 'Giảm 30% cho HSSV khi xuất trình thẻ tại quầy.', color: 'violet' },
];

export default function CinemaDetails() {
  const { id: cinemaId } = useParams();
  const navigate = useNavigate();
  const { setBookingSelection } = useBooking();

  const [cinema, setCinema] = useState(null);
  const [allShowtimes, setAllShowtimes] = useState([]);
  const [activeDate, setActiveDate] = useState(0);
  const [selectedShowtimeSlot, setSelectedShowtimeSlot] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Carousel State
  const [activeSlide, setActiveSlide] = useState(0);

  // Auto-advance carousel
  useEffect(() => {
    const timer = setInterval(() => {
      setActiveSlide(prev => (prev + 1) % CINEMA_BANNERS.length);
    }, 6000);
    return () => clearInterval(timer);
  }, []);

  // Fetch cinema info and showtimes
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      try {
        const [cinemaList, showtimeList] = await Promise.all([
          fetchCinemas(),
          fetchPublicShowtimes({ cinemaId }),
        ]);
        const currentCinema = cinemaList.find(c => c.cinemaId === parseInt(cinemaId));
        setCinema(currentCinema);
        setAllShowtimes(showtimeList);
      } catch (err) {
        console.error('Failed to load cinema details', err);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, [cinemaId]);

  // Generate 7 dates starting from today
  const dates = useMemo(() => {
    const dayNames = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
    const dts = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date();
      d.setDate(d.getDate() + i);
      const dateStr = d.toISOString().split('T')[0];
      dts.push({
        label: i === 0 ? 'Hôm nay' : i === 1 ? 'Ngày mai' : dayNames[d.getDay()],
        date: d.getDate(),
        month: d.getMonth() + 1,
        full: d,
        dateStr,
      });
    }
    return dts;
  }, []);

  // Filter movies for active date
  const filteredMovies = useMemo(() => {
    if (allShowtimes.length === 0) return [];
    const activeDateStr = dates[activeDate].dateStr;
    const movieMap = new Map();

    allShowtimes.forEach(st => {
      const stDate = st.startTime.split('T')[0];
      if (stDate !== activeDateStr) return;

      if (!movieMap.has(st.movieId)) {
        movieMap.set(st.movieId, {
          movieId: st.movieId,
          title: st.movieTitle,
          posterUrl: st.moviePosterUrl,
          ageRating: st.movieAgeRating,
          durationMinutes: st.movieDurationMinutes,
          screenType: st.screenType,
          showtimes: [],
        });
      }
      movieMap.get(st.movieId).showtimes.push(st);
    });

    return Array.from(movieMap.values());
  }, [allShowtimes, activeDate, dates]);

  const handleSelectSlot = (movie, st) => {
    setSelectedShowtimeSlot({ movie, showtime: st });
  };

  const confirmBooking = () => {
    const { movie, showtime } = selectedShowtimeSlot;
    setBookingSelection({
      movie: {
        movieId: movie.movieId,
        title: movie.title,
        posterUrl: movie.posterUrl,
        ageRating: movie.ageRating,
        durationMinutes: movie.durationMinutes,
      },
      cinema: {
        cinemaId: cinema.cinemaId,
        name: cinema.name,
        address: cinema.address,
      },
      showtime: {
        showtimeId: showtime.showtimeId,
        startTime: showtime.startTime,
        endTime: showtime.endTime,
        roomId: showtime.roomId,
        roomName: showtime.roomName,
        basePrice: showtime.basePrice,
        surcharge: showtime.surcharge,
        screenType: showtime.screenType,
      }
    });
    navigate('/booking/seats');
  };

  // ── Loading State ──────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="pt-44 flex flex-col items-center justify-center min-h-screen bg-slate-50 dark:bg-slate-950">
        <div className="w-16 h-16 border-4 border-orange-500/20 border-t-orange-500 rounded-full animate-spin"></div>
        <p className="mt-4 text-slate-400 font-bold uppercase tracking-widest text-xs">Đang tải thông tin rạp...</p>
      </div>
    );
  }

  // ── Not Found State ────────────────────────────────────────────────
  if (!cinema) {
    return (
      <div className="pt-44 flex flex-col items-center justify-center min-h-screen bg-slate-50 dark:bg-slate-950">
        <span className="material-symbols-outlined text-6xl text-slate-300 mb-4">theaters</span>
        <h2 className="text-2xl font-black text-slate-800 dark:text-white">Không tìm thấy rạp</h2>
        <button onClick={() => navigate('/')} className="mt-6 px-8 py-3 bg-gradient-to-r from-orange-500 to-red-500 text-white rounded-xl font-bold shadow-lg shadow-orange-500/30 hover:shadow-orange-500/50 transition-all">Quay lại trang chủ</button>
      </div>
    );
  }

  return (
    <main className="pt-32 relative bg-slate-50 dark:bg-slate-950 min-h-screen">
      <style>{`
        @keyframes ken-burns { 0% { transform: scale(1); } 100% { transform: scale(1.1); } }
      `}</style>

      {/* ═══════════════════════════════════════════════════════════════════
          SECTION 1: HERO CAROUSEL (HOME PAGE STYLE)
      ═══════════════════════════════════════════════════════════════════ */}
      <section className="relative w-full h-[600px] bg-black overflow-hidden group mb-10">
        {/* Slides */}
        {CINEMA_BANNERS.map((banner, idx) => (
          <div 
            key={idx}
            className={`absolute inset-0 transition-opacity duration-1000 ease-in-out ${
              idx === activeSlide ? 'opacity-100 z-10' : 'opacity-0 z-0'
            }`}
          >
            <img
              alt={`${cinema.name} banner ${idx + 1}`}
              className={`w-full h-full object-cover transition-transform duration-[10000ms] ease-linear ${idx === activeSlide ? 'scale-110' : 'scale-100'}`}
              src={banner}
            />
            {/* Gradients to match Home Page */}
            <div className="absolute inset-0 bg-gradient-to-r from-black/90 via-black/40 to-transparent"></div>
            <div className="absolute inset-0 bg-gradient-to-t from-slate-955 via-slate-950/20 to-transparent"></div>

            {/* Slide Content */}
            <div className="absolute inset-0 max-w-[1440px] mx-auto h-full flex flex-col justify-center px-8 md:px-16 pt-20">
              <div className={`transform transition-all duration-1000 delay-300 ${
                idx === activeSlide ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'
              }`}>
                <div className="inline-block px-4 py-1 rounded-full bg-orange-600 text-white text-[10px] font-bold mb-6 tracking-[0.2em] w-fit shadow-lg shadow-orange-500/30 uppercase">
                  Trải nghiệm rạp tiêu chuẩn
                </div>
                
                <h1 className="text-4xl md:text-6xl font-black text-white tracking-tighter leading-[0.9] mb-4 drop-shadow-2xl uppercase italic">
                  {cinema.name.split(' ').slice(0, -1).join(' ')}<br/>
                  <span className="text-transparent bg-clip-text bg-gradient-to-r from-orange-400 to-red-500">
                    {cinema.name.split(' ').slice(-1)}
                  </span>
                </h1>

                <p className="max-w-xl text-sm md:text-base text-slate-300 font-medium leading-relaxed mb-10 drop-shadow-md flex items-start gap-2">
                  <span className="material-symbols-outlined text-orange-500 shrink-0">location_on</span>
                  {cinema.address}
                </p>

                <div className="flex flex-wrap gap-4">
                  <button 
                    onClick={() => {
                      document.getElementById('showtimes-section')?.scrollIntoView({ behavior: 'smooth' });
                    }}
                    className="px-8 py-3 rounded-full bg-gradient-to-r from-orange-500 to-red-500 text-white font-bold text-sm shadow-[0_0_20px_rgba(249,115,22,0.4)] hover:shadow-[0_0_30px_rgba(249,115,22,0.6)] hover:-translate-y-1 transition-all flex items-center gap-2 tracking-widest uppercase"
                  >
                    <span className="material-symbols-outlined text-xl">confirmation_number</span>
                    ĐẶT VÉ NGAY
                  </button>
                  <button 
                    onClick={() => window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(cinema.name + ' ' + cinema.address)}`, '_blank')}
                    className="px-8 py-3 rounded-full bg-white/10 hover:bg-white/20 text-white backdrop-blur-md font-bold text-sm border border-white/20 hover:-translate-y-1 transition-all flex items-center gap-2 tracking-widest uppercase"
                  >
                    <span className="material-symbols-outlined text-xl">directions</span>
                    CHỈ ĐƯỜNG
                  </button>
                </div>
              </div>
            </div>
          </div>
        ))}

        {/* Navigation Arrows (Home Style) */}
        <button 
          onClick={() => setActiveSlide(prev => (prev === 0 ? CINEMA_BANNERS.length - 1 : prev - 1))}
          className="absolute left-4 md:left-8 top-1/2 -translate-y-1/2 z-30 w-12 h-12 md:w-14 md:h-14 bg-black/20 hover:bg-orange-500 text-white border border-white/10 backdrop-blur-md rounded-full flex items-center justify-center transition-all duration-300 opacity-50 hover:opacity-100 group"
        >
          <span className="material-symbols-outlined text-2xl md:text-3xl group-hover:-translate-x-1 transition-transform">arrow_left</span>
        </button>
        <button 
          onClick={() => setActiveSlide(prev => (prev + 1) % CINEMA_BANNERS.length)}
          className="absolute right-4 md:right-8 top-1/2 -translate-y-1/2 z-30 w-12 h-12 md:w-14 md:h-14 bg-black/20 hover:bg-orange-500 text-white border border-white/10 backdrop-blur-md rounded-full flex items-center justify-center transition-all duration-300 opacity-50 hover:opacity-100 group"
        >
          <span className="material-symbols-outlined text-2xl md:text-3xl group-hover:translate-x-1 transition-transform">arrow_right</span>
        </button>

        {/* Indicators (Home Style) */}
        <div className="absolute bottom-10 left-1/2 -translate-x-1/2 z-30 flex gap-3">
          {CINEMA_BANNERS.map((_, i) => (
            <button 
              key={i} 
              onClick={() => setActiveSlide(i)}
              className={`h-2 rounded-full transition-all duration-500 ${
                i === activeSlide 
                  ? 'bg-orange-500 w-10 shadow-[0_0_10px_rgba(249,115,22,0.8)]' 
                  : 'bg-white/40 hover:bg-white/80 w-2'
              }`}
            />
          ))}
        </div>
      </section>


      <section className="max-w-7xl mx-auto px-6 md:px-8 mb-16">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">

          {/* Left Column: Intro + Facilities */}
          <div className="lg:col-span-2 space-y-12">

            {/* ── Giới thiệu rạp ── */}
            <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 shadow-xl rounded-[2.5rem] p-8 md:p-10">
              <h2 className="text-2xl md:text-3xl font-black text-slate-800 dark:text-white tracking-tight uppercase flex items-center gap-3 mb-8">
                <span className="w-2 h-8 bg-gradient-to-b from-orange-500 to-red-500 rounded-full"></span>
                Giới thiệu rạp
              </h2>
              <div className="text-slate-600 dark:text-slate-400 leading-relaxed space-y-4 text-[15px]">
                {CINEMA_DESCRIPTION.split('\n\n').map((para, i) => (
                  <p key={i}>{para}</p>
                ))}
              </div>

              {/* Quick info pills */}
              <div className="flex flex-wrap gap-3 mt-8 pt-8 border-t border-slate-100 dark:border-slate-800">
                {['Wifi miễn phí', 'Bãi đỗ xe rộng', 'Phòng chờ VIP', 'Đặt vé online'].map(tag => (
                  <span key={tag} className="px-4 py-2 rounded-full bg-slate-50 dark:bg-slate-800 border border-slate-100 dark:border-slate-700 text-xs font-black text-slate-500 uppercase tracking-wider">
                    {tag}
                  </span>
                ))}
              </div>
            </div>

            {/* ── Tiện ích & Công nghệ (6 cards) ── */}
            <div>
              <h2 className="text-2xl md:text-3xl font-black text-slate-800 dark:text-white tracking-tight uppercase flex items-center gap-3 mb-8">
                <span className="w-2 h-8 bg-gradient-to-b from-cyan-500 to-blue-500 rounded-full"></span>
                Tiện ích & Công nghệ
              </h2>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-5">
                {FACILITIES.map((f, i) => (
                  <div key={i} className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 p-6 md:p-8 rounded-[2rem] shadow-lg hover:-translate-y-2 hover:shadow-xl transition-all duration-500 flex flex-col items-center justify-center text-center group">
                    <div className={`w-16 h-16 rounded-2xl bg-gradient-to-tr ${f.gradient} flex items-center justify-center mb-4 shadow-lg group-hover:scale-110 transition-transform`}>
                      <span className="material-symbols-outlined text-white text-3xl">{f.icon}</span>
                    </div>
                    <h3 className="font-black text-slate-800 dark:text-white uppercase tracking-wider text-sm mb-1">{f.name}</h3>
                    <p className="text-[11px] text-slate-400 font-medium">{f.desc}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Right Column: Sidebar */}
          <div className="lg:col-span-1">
            <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 p-8 md:p-10 rounded-[2.5rem] shadow-xl sticky top-28 space-y-8">
              <h3 className="text-xl font-black text-slate-800 dark:text-white uppercase tracking-tight flex items-center gap-3">
                <span className="w-2 h-6 bg-cyan-500 rounded-full"></span>
                Thông tin rạp
              </h3>

              <div className="space-y-5">
                {/* Operating Hours */}
                <div className="flex items-start gap-4">
                  <div className="w-11 h-11 rounded-xl bg-green-50 dark:bg-green-500/10 flex items-center justify-center shrink-0">
                    <span className="material-symbols-outlined text-green-500">schedule</span>
                  </div>
                  <div>
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Giờ mở cửa</p>
                    <p className="text-sm font-bold text-slate-800 dark:text-white">8:00 — 23:30 hàng ngày</p>
                    <p className="text-[11px] text-green-500 font-bold mt-0.5 flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
                      Đang mở cửa
                    </p>
                  </div>
                </div>

                {/* Hotline */}
                <div className="flex items-start gap-4">
                  <div className="w-11 h-11 rounded-xl bg-orange-50 dark:bg-orange-500/10 flex items-center justify-center shrink-0">
                    <span className="material-symbols-outlined text-orange-500">phone_in_talk</span>
                  </div>
                  <div>
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Hotline CSKH</p>
                    <p className="text-lg font-bold text-slate-800 dark:text-white">{cinema.hotline || '1900 0000'}</p>
                  </div>
                </div>

                {/* Email */}
                <div className="flex items-start gap-4">
                  <div className="w-11 h-11 rounded-xl bg-cyan-50 dark:bg-cyan-500/10 flex items-center justify-center shrink-0">
                    <span className="material-symbols-outlined text-cyan-500">alternate_email</span>
                  </div>
                  <div>
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Email</p>
                    <p className="text-sm font-bold text-slate-800 dark:text-white truncate">contact@starcine.vn</p>
                  </div>
                </div>

                {/* Address */}
                <div className="flex items-start gap-4">
                  <div className="w-11 h-11 rounded-xl bg-violet-50 dark:bg-violet-500/10 flex items-center justify-center shrink-0">
                    <span className="material-symbols-outlined text-violet-500">pin_drop</span>
                  </div>
                  <div>
                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Địa chỉ</p>
                    <p className="text-sm font-bold text-slate-800 dark:text-white leading-snug">{cinema.address}</p>
                  </div>
                </div>
              </div>

              {/* Map Button */}
              <button
                onClick={() => window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(cinema.name + ' ' + cinema.address)}`, '_blank')}
                className="w-full py-4 rounded-2xl border-2 border-slate-100 dark:border-slate-700 text-slate-600 dark:text-slate-300 font-bold hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors flex items-center justify-center gap-2 text-sm"
              >
                <span className="material-symbols-outlined text-xl">map</span>
                Xem trên Google Maps
              </button>

              {/* CTA Book Now */}
              <Link
                to="/movies"
                className="w-full py-5 rounded-2xl bg-gradient-to-r from-orange-500 to-red-500 text-white font-black text-sm uppercase tracking-widest shadow-lg shadow-orange-500/30 hover:shadow-orange-500/50 hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2"
              >
                <span className="material-symbols-outlined">confirmation_number</span>
                ĐẶT VÉ NGAY
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════════════════════════════
          SECTION 5: LỊCH CHIẾU PHIM (Full-width)
      ═══════════════════════════════════════════════════════════════════ */}
      <section className="max-w-7xl mx-auto px-6 md:px-8 mb-16">
        <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 shadow-xl rounded-[2.5rem] p-8 md:p-10">
          <h2 className="text-2xl md:text-3xl font-black text-slate-800 dark:text-white tracking-tight uppercase flex items-center gap-3 mb-10">
            <span className="w-2 h-8 bg-orange-500 rounded-full"></span>
            Lịch Chiếu Phim
            <span className="text-sm font-bold text-slate-400 normal-case tracking-normal ml-2">tại {cinema.name}</span>
          </h2>

          {/* Date Selection */}
          <div className="flex gap-3 md:gap-4 mb-10 overflow-x-auto pb-4 no-scrollbar">
            {dates.map((item, i) => {
              const isActive = i === activeDate;
              return (
                <button
                  key={i}
                  onClick={() => { setActiveDate(i); setSelectedShowtimeSlot(null); }}
                  className={`min-w-[90px] md:min-w-[110px] py-4 rounded-3xl flex flex-col items-center border-2 border-transparent transition-all duration-300 shrink-0 ${
                    isActive
                    ? "bg-gradient-to-b from-orange-400 to-red-500 text-white shadow-xl shadow-orange-500/40 -translate-y-1"
                    : "bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:border-orange-500/50 hover:bg-white dark:hover:bg-slate-900"
                  }`}
                >
                  <span className={`text-[9px] font-black uppercase tracking-widest ${isActive ? "text-white/80" : "text-slate-400"}`}>{item.label}</span>
                  <span className="text-3xl md:text-4xl font-black tracking-tighter my-0.5">{item.date}</span>
                  <span className={`text-[9px] font-black uppercase ${isActive ? "text-white/80" : "text-slate-400"}`}>Tháng {item.month}</span>
                </button>
              );
            })}
          </div>

          {/* Movie List */}
          <div className="space-y-6">
            {filteredMovies.length > 0 ? filteredMovies.map((movie) => (
              <div key={movie.movieId} className="flex flex-col sm:flex-row gap-6 p-5 md:p-6 bg-slate-50 dark:bg-slate-800/50 rounded-[2rem] border border-slate-100 dark:border-slate-800 transition-all hover:bg-white dark:hover:bg-slate-800 hover:shadow-xl group">
                <div className="w-full sm:w-[130px] shrink-0 h-[195px] rounded-2xl overflow-hidden shadow-lg relative">
                  <img alt={movie.title} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                    src={movie.posterUrl && movie.posterUrl.startsWith('http') ? movie.posterUrl : `https://lh3.googleusercontent.com/aida-public/${movie.posterUrl}`}
                  />
                  <div className="absolute top-2 left-2 px-2.5 py-1 rounded-lg text-[10px] font-black text-white uppercase shadow-sm bg-red-600">{movie.ageRating}</div>
                </div>

                <div className="flex-grow flex flex-col justify-between">
                  <div>
                    <h3 className="text-xl md:text-2xl font-black text-slate-800 dark:text-white mb-2 uppercase tracking-tight leading-tight">{movie.title}</h3>
                    <div className="flex flex-wrap gap-3 text-sm text-slate-400 font-medium mb-5">
                      <span className="flex items-center gap-1">
                        <span className="material-symbols-outlined text-base">schedule</span>
                        {movie.durationMinutes} phút
                      </span>
                      {movie.screenType && (
                        <span className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-base">tv</span>
                          {movie.screenType}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-3">
                    {movie.showtimes
                      .sort((a,b) => a.startTime.localeCompare(b.startTime))
                      .map((st) => {
                        const time = st.startTime.split('T')[1]?.substring(0, 5);
                        const isSelected = selectedShowtimeSlot?.showtime?.showtimeId === st.showtimeId;
                        return (
                          <button
                            key={st.showtimeId}
                            onClick={() => handleSelectSlot(movie, st)}
                            className={`px-5 py-2.5 rounded-xl font-bold text-sm transition-all shadow-sm ${
                              isSelected
                                ? 'bg-gradient-to-r from-orange-500 to-red-500 text-white shadow-lg shadow-orange-500/30 -translate-y-0.5'
                                : 'bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:border-orange-500 hover:text-orange-500'
                            }`}
                          >
                            {time}
                          </button>
                        );
                      })}
                  </div>
                </div>
              </div>
            )) : (
              <div className="text-center py-20">
                <span className="material-symbols-outlined text-6xl text-slate-200 mb-4 block">movie_off</span>
                <p className="text-slate-400 font-bold text-lg">Không có suất chiếu vào ngày {dates[activeDate].date}/{dates[activeDate].month}</p>
                <p className="text-slate-300 text-sm mt-2">Thử chọn ngày khác hoặc xem lịch chiếu tại rạp khác.</p>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════════════════════════════
          SECTION 6: KHUYẾN MÃI TẠI RẠP
      ═══════════════════════════════════════════════════════════════════ */}
      <section className="max-w-7xl mx-auto px-6 md:px-8 mb-16">
        <h2 className="text-2xl md:text-3xl font-black text-slate-800 dark:text-white tracking-tight uppercase flex items-center gap-3 mb-8">
          <span className="w-2 h-8 bg-gradient-to-b from-yellow-500 to-amber-600 rounded-full"></span>
          Ưu đãi dành cho bạn
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {PROMOS.map((promo, i) => (
            <div key={i} className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-8 shadow-lg hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group">
              <div className={`w-14 h-14 rounded-2xl bg-${promo.color}-50 dark:bg-${promo.color}-500/10 flex items-center justify-center mb-5 group-hover:scale-110 transition-transform`}>
                <span className={`material-symbols-outlined text-${promo.color}-500 text-2xl`}>{promo.icon}</span>
              </div>
              <h4 className="text-lg font-black text-slate-800 dark:text-white uppercase tracking-tight mb-2">{promo.title}</h4>
              <p className="text-sm text-slate-500 leading-relaxed mb-5">{promo.desc}</p>
              <a href="#" className="text-orange-500 font-bold text-xs uppercase tracking-widest hover:underline underline-offset-4">
                Xem chi tiết →
              </a>
            </div>
          ))}
        </div>
      </section>

      {/* ═══════════════════════════════════════════════════════════════════
          SECTION 7: VỊ TRÍ & BẢN ĐỒ (Full-width CTA)
      ═══════════════════════════════════════════════════════════════════ */}
      <section className="max-w-7xl mx-auto px-6 md:px-8">
        <div className="relative bg-gradient-to-br from-slate-800 to-slate-900 rounded-[2.5rem] p-10 md:p-16 overflow-hidden">
          {/* Decorative blobs */}
          <div className="absolute top-0 right-0 w-72 h-72 bg-orange-500/10 rounded-full blur-[100px]"></div>
          <div className="absolute bottom-0 left-0 w-96 h-96 bg-cyan-500/10 rounded-full blur-[120px]"></div>

          <div className="relative z-10 flex flex-col lg:flex-row items-center justify-between gap-10">
            <div className="text-center lg:text-left max-w-xl">
              <h2 className="text-3xl md:text-4xl font-black text-white tracking-tighter mb-4">
                Đến <span className="text-transparent bg-clip-text bg-gradient-to-r from-orange-400 to-red-400">{cinema.name}</span>
              </h2>
              <p className="text-slate-400 text-lg leading-relaxed font-medium flex items-start gap-2">
                <span className="material-symbols-outlined text-orange-400 mt-1 shrink-0">location_on</span>
                {cinema.address}{cinema.locationName ? `, ${cinema.locationName}` : ''}
              </p>
            </div>
            <div className="flex flex-col sm:flex-row gap-4 shrink-0">
              <button
                onClick={() => window.open(`https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(cinema.name + ' ' + cinema.address)}`, '_blank')}
                className="px-10 py-5 rounded-2xl bg-gradient-to-r from-orange-500 to-red-500 text-white font-black text-sm uppercase tracking-widest shadow-xl shadow-orange-500/30 hover:shadow-orange-500/50 hover:scale-105 transition-all flex items-center gap-3"
              >
                <span className="material-symbols-outlined text-xl">directions</span>
                Chỉ Đường
              </button>
              <button
                onClick={() => window.open(`tel:${cinema.hotline || '19000000'}`)}
                className="px-10 py-5 rounded-2xl bg-white/10 border border-white/20 text-white font-black text-sm uppercase tracking-widest hover:bg-white/20 transition-all flex items-center gap-3"
              >
                <span className="material-symbols-outlined text-xl">call</span>
                Gọi Ngay
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* ═══════════════════════════════════════════════════════════════════
          CONFIRMATION MODAL
      ═══════════════════════════════════════════════════════════════════ */}
      {selectedShowtimeSlot && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" onClick={() => setSelectedShowtimeSlot(null)}></div>
          <div className="relative bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl shadow-2xl p-8 max-w-sm w-full animate-[zoomIn_0.3s_ease-out]">
            <button onClick={() => setSelectedShowtimeSlot(null)} className="absolute top-4 right-4 text-slate-400 hover:text-slate-800 dark:hover:text-white transition-colors">
              <span className="material-symbols-outlined">close</span>
            </button>
            <div className="text-center mb-8">
              <div className="w-16 h-16 bg-orange-50 dark:bg-orange-500/10 rounded-full flex items-center justify-center text-orange-500 mx-auto mb-4">
                <span className="material-symbols-outlined text-3xl">local_activity</span>
              </div>
              <h2 className="text-xl font-black text-slate-800 dark:text-white mb-1">Xác nhận suất chiếu</h2>
              <p className="text-sm text-slate-400">Kiểm tra thông tin trước khi chọn ghế</p>
            </div>
            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-2xl p-5 mb-8 border border-slate-100 dark:border-slate-800 space-y-3">
              <div className="flex justify-between border-b border-slate-200/50 dark:border-slate-700/50 pb-3">
                <span className="text-xs font-bold text-slate-400 uppercase">Phim</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white text-right line-clamp-1 max-w-[180px]">{selectedShowtimeSlot.movie.title}</span>
              </div>
              <div className="flex justify-between border-b border-slate-200/50 dark:border-slate-700/50 pb-3">
                <span className="text-xs font-bold text-slate-400 uppercase">Rạp</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white">{cinema?.name}</span>
              </div>
              <div className="flex justify-between border-b border-slate-200/50 dark:border-slate-700/50 pb-3">
                <span className="text-xs font-bold text-slate-400 uppercase">Ngày chiếu</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white">{dates[activeDate].date}/{dates[activeDate].month} — {dates[activeDate].label}</span>
              </div>
              <div className="flex justify-between border-b border-slate-200/50 dark:border-slate-700/50 pb-3">
                <span className="text-xs font-bold text-slate-400 uppercase">Giờ chiếu</span>
                <span className="text-sm font-black text-orange-500">{selectedShowtimeSlot.showtime.startTime.split('T')[1].substring(0, 5)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-xs font-bold text-slate-400 uppercase">Phòng</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white">{selectedShowtimeSlot.showtime.roomName}</span>
              </div>
            </div>
            <button
              onClick={confirmBooking}
              className="w-full py-4 rounded-2xl bg-gradient-to-r from-orange-500 to-red-500 text-white font-black tracking-widest uppercase shadow-lg shadow-orange-500/30 hover:shadow-orange-500/50 hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2"
            >
              Tiếp tục chọn ghế
              <span className="material-symbols-outlined">arrow_forward</span>
            </button>
          </div>
        </div>
      )}
    </main>
  );
}
