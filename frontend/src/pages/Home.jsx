import { useState, useEffect, useRef, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import MovieCard from '../components/MovieCard';
import { useBooking } from '../contexts/BookingContext';
import { fetchNowShowingMovies, fetchComingSoonMovies } from '../services/movieService';
import { fetchCinemas } from '../services/cinemaService';
import { fetchPublicShowtimes } from '../services/showtimeService';

export default function Home() {
  const navigate = useNavigate();
  const { setBookingSelection } = useBooking();

  const [currentSlide, setCurrentSlide] = useState(0);

  // ── API Data States ─────────────────────────────────────────────────
  const [nowShowingMovies, setNowShowingMovies] = useState([]);
  const [comingSoonMovies, setComingSoonMovies] = useState([]);
  const [cinemas, setCinemas] = useState([]);
  const [showtimes, setShowtimes] = useState([]);
  const [loadingMovies, setLoadingMovies] = useState(true);
  const [loadingCinemas, setLoadingCinemas] = useState(true);

  // ── Quick Booking State ─────────────────────────────────────────────
  const [selectedCinema, setSelectedCinema] = useState("");
  const [selectedMovie, setSelectedMovie] = useState("");
  const [selectedDate, setSelectedDate] = useState("");
  const [selectedTime, setSelectedTime] = useState("");
  const [openDropdown, setOpenDropdown] = useState(null);
  const bookingBarRef = useRef(null);

  // Contact Form State
  const [contactStatus, setContactStatus] = useState("idle");

  // Smooth Scroll Refs
  const nowShowingRef = useRef(null);
  const comingSoonRef = useRef(null);

  // ── Fetch data on mount ─────────────────────────────────────────────
  useEffect(() => {
    const loadMovies = async () => {
      try {
        const [nowShowing, comingSoon] = await Promise.all([
          fetchNowShowingMovies(),
          fetchComingSoonMovies(),
        ]);
        setNowShowingMovies(nowShowing);
        setComingSoonMovies(comingSoon);
      } catch (err) {
        console.error('Error loading movies:', err);
      } finally {
        setLoadingMovies(false);
      }
    };

    const loadCinemas = async () => {
      try {
        const data = await fetchCinemas();
        setCinemas(data);
      } catch (err) {
        console.error('Error loading cinemas:', err);
      } finally {
        setLoadingCinemas(false);
      }
    };

    loadMovies();
    loadCinemas();
  }, []);

  // ── Quick Booking: Load showtimes when cinema is selected ───────────
  useEffect(() => {
    if (!selectedCinema) {
      setShowtimes([]);
      return;
    }
    const loadShowtimes = async () => {
      try {
        const data = await fetchPublicShowtimes({ cinemaId: selectedCinema });
        setShowtimes(data);
      } catch (err) {
        console.error('Error loading showtimes:', err);
        setShowtimes([]);
      }
    };
    loadShowtimes();
  }, [selectedCinema]);

  // ── Derived: movies for selected cinema ─────────────────────────────
  const moviesForCinema = useCallback(() => {
    if (!selectedCinema || showtimes.length === 0) return [];
    const uniqueMovies = new Map();
    showtimes.forEach(st => {
      if (!uniqueMovies.has(st.movieId)) {
        uniqueMovies.set(st.movieId, {
          id: st.movieId,
          title: st.movieTitle,
          posterUrl: st.moviePosterUrl,
        });
      }
    });
    return Array.from(uniqueMovies.values());
  }, [selectedCinema, showtimes]);

  // ── Derived: dates for selected movie ───────────────────────────────
  const datesForMovie = useCallback(() => {
    if (!selectedMovie) return [];
    const dateSet = new Map();
    showtimes
      .filter(st => st.movieId === parseInt(selectedMovie))
      .forEach(st => {
        const dateStr = st.startTime.split('T')[0]; // YYYY-MM-DD
        if (!dateSet.has(dateStr)) {
          const d = new Date(dateStr);
          const dayNames = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
          const today = new Date().toISOString().split('T')[0];
          const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0];
          let label;
          if (dateStr === today) label = 'Hôm nay';
          else if (dateStr === tomorrow) label = 'Ngày mai';
          else label = `${dayNames[d.getDay()]}, ${d.getDate()}/${d.getMonth() + 1}`;

          dateSet.set(dateStr, { id: dateStr, label });
        }
      });
    return Array.from(dateSet.values());
  }, [selectedMovie, showtimes]);

  // ── Derived: times for selected date ────────────────────────────────
  const timesForDate = useCallback(() => {
    if (!selectedDate || !selectedMovie) return [];
    return showtimes
      .filter(st => {
        const stDate = st.startTime.split('T')[0];
        return st.movieId === parseInt(selectedMovie) && stDate === selectedDate;
      })
      .map(st => {
        const time = st.startTime.split('T')[1]?.substring(0, 5); // HH:mm
        return {
          id: st.showtimeId,
          label: `${time} — ${st.screenType || '2D'}`,
          showtime: st,
        };
      })
      .sort((a, b) => a.label.localeCompare(b.label));
  }, [selectedDate, selectedMovie, showtimes]);

  // ── Handlers ────────────────────────────────────────────────────────
  const handleCinemaChange = (cinemaId) => {
    setSelectedCinema(cinemaId);
    setSelectedMovie("");
    setSelectedDate("");
    setSelectedTime("");
  };

  const handleMovieChange = (movieId) => {
    setSelectedMovie(movieId);
    setSelectedDate("");
    setSelectedTime("");
  };

  const handleDateChange = (date) => {
    setSelectedDate(date.id);
    setOpenDropdown(null);
    setSelectedTime("");
  };

  const handleTimeChange = (time) => {
    setSelectedTime(time.id);
    setOpenDropdown(null);
  };

  const handleQuickBook = (e) => {
    if (!selectedTime) {
      e.preventDefault();
      alert("Vui lòng hoàn thành đủ các bước chọn Rạp - Phim - Ngày - Suất để tiếp tục đặt vé!");
      return;
    }
    const timeObj = timesForDate().find(t => t.id === selectedTime);
    if (!timeObj) return;

    const st = timeObj.showtime;
    const cinemaObj = cinemas.find(c => c.cinemaId === parseInt(selectedCinema));

    setBookingSelection({
      movie: {
        movieId: st.movieId,
        title: st.movieTitle,
        posterUrl: st.moviePosterUrl,
        ageRating: st.movieAgeRating,
        durationMinutes: st.movieDurationMinutes,
      },
      cinema: {
        cinemaId: cinemaObj?.cinemaId,
        name: cinemaObj?.name,
        address: cinemaObj?.address,
      },
      showtime: {
        showtimeId: st.showtimeId,
        startTime: st.startTime,
        endTime: st.endTime,
        roomId: st.roomId,
        roomName: st.roomName,
        basePrice: st.basePrice,
        surcharge: st.surcharge,
        screenType: st.screenType,
      },
    });
    navigate('/booking/seats');
  };

  const handleContactSubmit = (e) => {
    e.preventDefault();
    setContactStatus("submitting");
    setTimeout(() => setContactStatus("success"), 1500);
  };

  // ── Close dropdowns on click outside ────────────────────────────────
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (bookingBarRef.current && !bookingBarRef.current.contains(event.target)) {
        setOpenDropdown(null);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const scrollLeft = (ref) => ref.current?.scrollBy({ left: -400, behavior: 'smooth' });
  const scrollRight = (ref) => ref.current?.scrollBy({ left: 400, behavior: 'smooth' });

  // ── Hero Banner Slides ─────────────────────────────────────────────
  const slides = [
    {
      tag: "PHIM HOT TRONG TUẦN",
      title1: "THE NEON",
      title2: "RECKONING",
      desc: "Trải nghiệm đỉnh cao công nghệ IMAX Laser duy nhất tại StarCine.",
      img: "https://lh3.googleusercontent.com/aida-public/AB6AXuB7VYZrP0zS3zZ_9MBRT3y9oeBDAIn4OLREY0wM3Rb_iPgNK3yIPGTa1-xwaClgPP7UrP061jcms7SGfl4Hz8PXhuvEmfjKDueZuLCgC_PImrLzZjbhmD8IgxJ4b1MP44g-PfZBjzmABwZWJYqieTrJzMC63n-07aR3u0K2R87AJNefcD1QGKb0cuya20dip47MiJezTcya2aRMG2kWVD0-im1mEizjJek7tZUtELXFfcKPhTlPJSW4pWp7muXqcP9GsB9RZCipDeY",
      trailer: "https://www.youtube.com/watch?v=kYJv8PjX_Xo"
    },
    {
      tag: "ĐỘC QUYỀN IMAX",
      title1: "AVATAR:",
      title2: "THE WAY OF WATER",
      desc: "Hành trình trở lại Pandora với kỉ xảo đỉnh cao và trải nghiệm thị giác ngoạn mục.",
      img: "https://images.unsplash.com/photo-1626814026160-2237a95fc5a0?q=80&w=2070&auto=format&fit=crop",
      trailer: "https://www.youtube.com/watch?v=d9MyW72ELq0"
    },
    {
      tag: "PHIM HAY THÁNG",
      title1: "OPPENHEIMER",
      title2: "A MASTERPIECE",
      desc: "Siêu phẩm điện ảnh của Christopher Nolan về cha đẻ của bom nguyên tử.",
      img: "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?q=80&w=2070&auto=format&fit=crop",
      trailer: "https://www.youtube.com/watch?v=uYPbbksJxIg"
    }
  ];

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % slides.length);
    }, 6000);
    return () => clearInterval(timer);
  }, [slides.length]);

  const nextSlide = () => setCurrentSlide((prev) => (prev + 1) % slides.length);
  const prevSlide = () => setCurrentSlide((prev) => (prev === 0 ? slides.length - 1 : prev - 1));

  // ── Computed lists for Quick Booking dropdowns ──────────────────────
  const moviesList = moviesForCinema();
  const datesList = datesForMovie();
  const timesList = timesForDate();

  return (
    <main className="pt-32 relative">
      {/* Hero Banner Carousel */}
      <section className="relative w-full h-[600px] bg-black">
        <div className="absolute inset-0 overflow-hidden">
          {slides.map((slide, index) => (
            <div 
              key={`hero-${slide.tag}-${slide.title1}`}
              className={`absolute inset-0 transition-opacity duration-1000 ease-in-out ${
                index === currentSlide ? 'opacity-100 z-10' : 'opacity-0 z-0'
              }`}
            >
              <img 
                className={`w-full h-full object-cover transition-transform duration-[12000ms] ease-linear ${index === currentSlide ? 'scale-110' : 'scale-100'}`} 
                alt={slide.title1} 
                src={slide.img} 
              />
              <div className="absolute inset-0 bg-gradient-to-r from-black/90 via-black/40 to-transparent"></div>
              <div className="absolute inset-0 bg-gradient-to-t from-surface via-surface/20 to-transparent"></div>
              
              <div className="absolute inset-0 max-w-[1440px] mx-auto h-full flex flex-col justify-center px-8 md:px-16 pt-20">
                <div className={`transform transition-all duration-1000 delay-300 ${index === currentSlide ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'}`}>
                  <div className="inline-block px-4 py-1 rounded-full bg-orange-600 text-white text-xs font-bold mb-6 tracking-widest w-fit shadow-lg shadow-orange-500/30">
                    {slide.tag}
                  </div>
                  <h1 className="text-4xl md:text-6xl font-black text-white tracking-tighter leading-[0.9] mb-6 drop-shadow-2xl uppercase">
                    {slide.title1}<br/>
                    <span className="text-transparent bg-clip-text bg-gradient-to-r from-orange-400 to-red-500">{slide.title2}</span>
                  </h1>
                  <p className="max-w-xl text-base md:text-lg text-slate-300 font-medium leading-relaxed mb-10 drop-shadow-md">
                    {slide.desc}
                  </p>
                  <div className="flex flex-wrap gap-4">
                    <button onClick={handleQuickBook} className="px-6 py-3 rounded-full bg-gradient-to-r from-orange-500 to-red-500 text-white font-bold text-base shadow-[0_0_20px_rgba(249,115,22,0.4)] hover:shadow-[0_0_30px_rgba(249,115,22,0.6)] hover:-translate-y-1 transition-all flex items-center gap-2">
                      <span className="material-symbols-outlined text-xl">confirmation_number</span>
                      MUA VÉ NGAY
                    </button>
                    <button 
                      onClick={() => window.open(slide.trailer, '_blank')}
                      className="px-6 py-3 rounded-full bg-white/10 hover:bg-white/20 text-white backdrop-blur-md font-bold text-base border border-white/20 hover:-translate-y-1 transition-all flex items-center gap-2"
                    >
                      <span className="material-symbols-outlined text-xl">play_circle</span>
                      XEM TRAILER
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Navigation Arrows */}
        <button 
          onClick={prevSlide} 
          className="absolute left-4 md:left-8 top-1/2 -translate-y-1/2 z-30 w-14 h-14 bg-black/20 hover:bg-orange-500 text-white border border-white/10 backdrop-blur-md rounded-full flex items-center justify-center transition-all duration-300 opacity-50 hover:opacity-100 group"
        >
          <span className="material-symbols-outlined text-3xl group-hover:-translate-x-1 transition-transform">arrow_left</span>
        </button>
        <button 
          onClick={nextSlide} 
          className="absolute right-4 md:right-8 top-1/2 -translate-y-1/2 z-30 w-14 h-14 bg-black/20 hover:bg-orange-500 text-white border border-white/10 backdrop-blur-md rounded-full flex items-center justify-center transition-all duration-300 opacity-50 hover:opacity-100 group"
        >
          <span className="material-symbols-outlined text-3xl group-hover:translate-x-1 transition-transform">arrow_right</span>
        </button>

        {/* Slide Indicators */}
        <div className="absolute bottom-[140px] left-1/2 -translate-x-1/2 z-30 flex gap-3">
          {slides.map((s, i) => (
            <button 
              onClick={() => setCurrentSlide(i)} 
              key={`dot-${s.tag}-${i}`} 
              className={`h-2 rounded-full transition-all duration-500 ${i === currentSlide ? 'bg-orange-500 w-10 shadow-[0_0_10px_rgba(249,115,22,0.8)]' : 'bg-white/40 hover:bg-white/80 w-2'}`}
              aria-label={`Go to slide ${i + 1}`}
            ></button>
          ))}
        </div>

        {/* Floating Quick Booking Bar */}
        <div className="absolute bottom-0 left-1/2 z-30 animate-[slideUpOverlap_1s_ease-out_0.5s_both]">
          <div ref={bookingBarRef} className="bg-white/95 dark:bg-slate-900/95 backdrop-blur-3xl p-2 rounded-3xl md:rounded-[2.5rem] shadow-[0_40px_80px_-15px_rgba(0,0,0,0.35)] border border-white/40 dark:border-white/10 flex flex-col md:flex-row items-center gap-2 w-[95vw] max-w-6xl relative">
            <div className="flex flex-1 items-stretch w-full">
              {/* Segment: Rạp */}
              <div className="flex-1 flex items-center gap-4 px-6 py-4 hover:bg-slate-100/80 dark:hover:bg-slate-800/80 transition-all cursor-pointer group relative" onClick={() => setOpenDropdown('cinema')}>
                <span className="material-symbols-outlined text-3xl text-orange-600 dark:text-orange-500 group-hover:scale-110 transition-transform">location_on</span>
                <div className="flex-1 min-w-0">
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-none mb-1">Rạp Chiếu</p>
                  <p className="text-lg font-bold text-slate-800 dark:text-white truncate">
                    {selectedCinema ? cinemas.find(c => c.cinemaId === parseInt(selectedCinema))?.name : (loadingCinemas ? "Đang tải..." : "Chọn rạp...")}
                  </p>
                </div>
                <div className={`absolute top-full left-0 w-[300px] pt-4 transition-all duration-300 origin-top shadow-2xl z-[60] ${openDropdown === 'cinema' ? 'opacity-100 scale-y-100' : 'opacity-0 scale-y-0 pointer-events-none'}`}>
                  <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-2 space-y-1 shadow-2xl max-h-[300px] overflow-y-auto">
                    {cinemas.map(c => (
                      <div key={c.cinemaId} onClick={(e) => { e.stopPropagation(); handleCinemaChange(String(c.cinemaId)); setOpenDropdown(null); }} className={`p-3 hover:bg-orange-50 dark:hover:bg-orange-500/10 rounded-xl transition-colors cursor-pointer flex items-center gap-3 ${selectedCinema === String(c.cinemaId) ? 'bg-orange-50 dark:bg-orange-500/10' : ''}`}>
                        <span className="text-sm font-bold text-slate-700 dark:text-slate-300 block">{c.name}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
              
              <div className="w-[1px] bg-slate-200/60 dark:bg-slate-700/60 my-4 hidden md:block"></div>
              
              {/* Segment: Phim */}
              <div className="flex-1 flex items-center gap-4 px-6 py-4 transition-all cursor-pointer group hover:bg-slate-100/80 dark:hover:bg-slate-800/80 relative" onClick={() => selectedCinema ? setOpenDropdown('movie') : null}>
                <span className="material-symbols-outlined text-3xl text-orange-600 dark:text-orange-500 group-hover:scale-110 transition-transform">movie</span>
                <div className="flex-1 min-w-0">
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-none mb-1">Phim</p>
                  <p className="text-lg font-bold text-slate-800 dark:text-white truncate">
                    {selectedMovie ? moviesList.find(m => m.id === parseInt(selectedMovie))?.title : "Chọn phim..."}
                  </p>
                </div>
                <div className={`absolute top-full left-0 w-[300px] pt-4 transition-all duration-300 origin-top shadow-2xl z-[60] ${openDropdown === 'movie' ? 'opacity-100 scale-y-100' : 'opacity-0 scale-y-0 pointer-events-none'}`}>
                  <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-2 space-y-1 shadow-2xl max-h-[300px] overflow-y-auto">
                    {moviesList.map(m => (
                      <div key={m.id} onClick={(e) => { e.stopPropagation(); handleMovieChange(String(m.id)); setOpenDropdown(null); }} className="p-3 hover:bg-orange-50 dark:hover:bg-orange-500/10 rounded-xl transition-colors cursor-pointer flex items-center gap-3">
                        <span className="text-sm font-bold text-slate-700 dark:text-slate-300">{m.title}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="w-[1px] bg-slate-200/60 dark:bg-slate-700/60 my-4 hidden md:block"></div>
              
              {/* Segment: Ngày */}
              <div className="flex-1 flex items-center gap-4 px-6 py-4 transition-all cursor-pointer group hover:bg-slate-100/80 dark:hover:bg-slate-800/80 relative" onClick={() => selectedMovie ? setOpenDropdown('date') : null}>
                <span className="material-symbols-outlined text-3xl text-orange-600 dark:text-orange-500 group-hover:scale-110 transition-transform">calendar_month</span>
                <div className="flex-1 min-w-0">
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-none mb-1">Ngày</p>
                  <p className="text-lg font-bold text-slate-800 dark:text-white truncate">
                    {selectedDate ? datesList.find(d => d.id === selectedDate)?.label : "Chọn ngày..."}
                  </p>
                </div>
                <div className={`absolute top-full left-0 w-[200px] pt-4 transition-all duration-300 origin-top shadow-2xl z-[60] ${openDropdown === 'date' ? 'opacity-100 scale-y-100' : 'opacity-0 scale-y-0 pointer-events-none'}`}>
                  <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-2 space-y-1 shadow-2xl">
                    {datesList.map(d => (
                      <div key={d.id} onClick={(e) => { e.stopPropagation(); handleDateChange(d); }} className="p-3 hover:bg-orange-50 dark:hover:bg-orange-500/10 rounded-xl transition-colors cursor-pointer flex items-center gap-3">
                        <span className="text-sm font-bold text-slate-700 dark:text-slate-300">{d.label}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="w-[1px] bg-slate-200/60 dark:bg-slate-700/60 my-4 hidden md:block"></div>
              
              {/* Segment: Suất */}
              <div className="flex-1 flex items-center gap-4 px-6 py-4 transition-all cursor-pointer group hover:bg-slate-100/80 dark:hover:bg-slate-800/80 relative" onClick={() => selectedDate ? setOpenDropdown('time') : null}>
                <span className="material-symbols-outlined text-3xl text-orange-600 dark:text-orange-500 group-hover:scale-110 transition-transform">schedule</span>
                <div className="flex-1 min-w-0">
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-none mb-1">Suất Chiếu</p>
                  <p className="text-lg font-bold text-slate-800 dark:text-white truncate">
                    {selectedTime ? timesList.find(t => t.id === selectedTime)?.label : "Chọn suất..."}
                  </p>
                </div>
                <div className={`absolute top-full left-0 w-[250px] pt-4 transition-all duration-300 origin-top shadow-2xl z-[60] ${openDropdown === 'time' ? 'opacity-100 scale-y-100' : 'opacity-0 scale-y-0 pointer-events-none'}`}>
                  <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-2 space-y-1 shadow-2xl">
                    {timesList.map(t => (
                      <div key={`${t.id}-${t.showtime?.startTime ?? ''}`} onClick={(e) => { e.stopPropagation(); handleTimeChange(t); }} className="p-3 hover:bg-orange-50 dark:hover:bg-orange-500/10 rounded-xl transition-colors cursor-pointer flex items-center gap-3">
                        <span className="text-sm font-bold text-slate-700 dark:text-slate-300">{t.label}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>
            <div className="p-2 w-full md:w-auto">
              <button onClick={handleQuickBook} className="w-full md:w-auto px-10 py-5 rounded-2xl md:rounded-full text-white font-black text-sm uppercase tracking-widest shadow-xl transition-all shrink-0 flex items-center justify-center gap-2 group overflow-hidden relative bg-gradient-to-r from-orange-500 to-red-500 shadow-orange-500/30 hover:shadow-orange-500/50 hover:scale-[1.02] hover:-translate-y-0.5">
                <div className="absolute inset-0 w-full h-full bg-white/20 -translate-x-full group-hover:translate-x-full transition-transform duration-700"></div>
                <span className="relative z-10 whitespace-nowrap">MUA VÉ NGAY</span>
                <span className="material-symbols-outlined text-xl group-hover:translate-x-1 transition-transform relative z-10">arrow_forward</span>
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* ═══ PHIM ĐANG CHIẾU ═══ */}
      <section className="mt-40 max-w-7xl mx-auto px-8">
        <div className="flex justify-between items-end mb-12">
          <div className="flex flex-col items-start">
            <h2 className="text-4xl font-black tracking-tighter text-on-surface">PHIM ĐANG CHIẾU</h2>
            <div className="h-1 w-20 bg-orange-500 mt-2"></div>
          </div>
          <div className="flex gap-2">
            <button onClick={() => scrollLeft(nowShowingRef)} className="w-10 h-10 rounded-full flex items-center justify-center bg-slate-100 text-slate-800 hover:bg-orange-500 hover:text-white transition-all"><span className="material-symbols-outlined">chevron_left</span></button>
            <button onClick={() => scrollRight(nowShowingRef)} className="w-10 h-10 rounded-full flex items-center justify-center bg-slate-100 text-slate-800 hover:bg-orange-500 hover:text-white transition-all"><span className="material-symbols-outlined">chevron_right</span></button>
          </div>
        </div>
        <div ref={nowShowingRef} className="flex overflow-x-auto gap-8 pb-8 no-scrollbar scroll-smooth">
          {loadingMovies ? (
            Array.from({ length: 5 }).map((_, i) => (<div key={i} className="min-w-[300px] aspect-[2/3] rounded-2xl bg-slate-200 animate-pulse" />))
          ) : nowShowingMovies.map((movie, index) => (
            <div key={movie.movieId != null ? `now-${movie.movieId}-${index}` : `now-idx-${index}`} className="min-w-[300px]"><MovieCard movie={movie} /></div>
          ))}
        </div>
      </section>

      {/* ═══ PHIM SẮP CHIẾU ═══ */}
      <section className="mt-24 max-w-7xl mx-auto px-8 py-20 bg-slate-50 rounded-3xl relative">
        <div className="flex justify-between items-center mb-12">
          <div className="flex flex-col items-start">
            <h2 className="text-4xl font-black tracking-tighter text-on-surface">PHIM SẮP CHIẾU</h2>
            <div className="h-1 w-20 bg-slate-300 mt-2"></div>
          </div>
          <div className="flex gap-2">
            <button onClick={() => scrollLeft(comingSoonRef)} className="w-10 h-10 rounded-full flex items-center justify-center bg-white text-slate-800 shadow-sm hover:bg-orange-500 hover:text-white transition-all"><span className="material-symbols-outlined">chevron_left</span></button>
            <button onClick={() => scrollRight(comingSoonRef)} className="w-10 h-10 rounded-full flex items-center justify-center bg-white text-slate-800 shadow-sm hover:bg-orange-500 hover:text-white transition-all"><span className="material-symbols-outlined">chevron_right</span></button>
          </div>
        </div>
        <div ref={comingSoonRef} className="flex overflow-x-auto gap-8 pb-8 no-scrollbar scroll-smooth">
          {loadingMovies ? (
            Array.from({ length: 5 }).map((_, i) => (<div key={i} className="min-w-[300px] aspect-[2/3] rounded-2xl bg-slate-200 animate-pulse" />))
          ) : comingSoonMovies.map((movie, index) => (
            <div key={movie.movieId != null ? `soon-${movie.movieId}-${index}` : `soon-idx-${index}`} className="min-w-[300px]"><MovieCard movie={{ ...movie, isComingSoon: true }} /></div>
          ))}
        </div>
      </section>

      {/* Promotions Section */}
      <section className="mt-24 max-w-7xl mx-auto px-8">
        <h2 className="text-4xl font-black tracking-tighter text-on-surface mb-12 uppercase">KHUYẾN MÃI MỚI NHẤT</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            { title: "COMBO PIZZA & PHIM", sub: "Thưởng thức trọn vẹn hương vị Ý cùng những siêu phẩm điện ảnh tháng này.", img: "AB6AXuBFDm1HRCaB7TRXMh95OcNvMZg_qaFOcV-xFruKvjcX4sdNor-CvMBo3Ci-xg09Y9ZMt7a9r1z72qDXfLFFZkk0XgsyCXIIYGvsFt5SCaMnr5ycyxQH24aal1DEFpX2sGrwgkVGphzLUImQJdevEVryNXJgTp-K9pjypvU_oWJIl4A4lrb7ZrEkn5VJQXjfc8cQzjLPd5nJRC-sTz0vMNeRtA0y5uwCtOJksCm9NnT9MzGuDY8h6THeUVkqDbCG0PUa4D5IEFXKX0o" },
            { title: "GIẢM 50% VÉ THỨ 2", sub: "Áp dụng cho mọi suất chiếu IMAX vào mỗi thứ Tư hàng tuần tại StarCine.", img: "AB6AXuAsPM7bAjYl8k_9u58hLEJnOIvF5uYu3R0K9LGauF86cuwNCbatBReSJaF1ozmiYoyEiZC7ei2c6-Wn4Ni5jA4ls_oy-vP0kkJeK7JlwwmJsgVeR-GAhvP2GeiUdGerSdEvMEOg_kNBDrnuDwpaFIYcQ67Ykb1f1CqVJxBWuAc_gpcc5sBwTWrXbzCPGP03KY9NB4sttjfe7MIdoG1_bh91tWJZZM76saiF-lWD-fgSO6R9dFLJLaHibZoRUUN2DE0NZtobg_Lk4Ls" },
            { title: "THỨ 2 SINH VIÊN", sub: "Đồng giá 45.000đ cho tất cả các bạn học sinh, sinh viên trên toàn hệ thống.", img: "AB6AXuDGodo9mvmlpK8-G16HE3OjmJ7_nJsIWtvoxJaqhohDq154RtgGwAnXKwy5_OYxHjjHFBviEdzvuPIglR4_o35LT17eAwqienblkajoX2x6lWknmpH1bGYKnfMV31BcXADiIHnUD2iN00PiPRVo7jOQ5ywL46mFzd47c36V7qg95gsUM-a65YJEgGkHPfJTbcoSJ1BDeqZL0Agut8We0p9tbZYpVGTm7mke3gky3-CgtrljtVO7uzrkGTwiwvi83-ZV1wyjNELWwQo" }
          ].map((promo) => (
            <div key={promo.title} className="group relative bg-white rounded-2xl overflow-hidden shadow-lg transition-transform hover:scale-[1.02] duration-300">
              <img className="w-full h-48 object-cover" src={`https://lh3.googleusercontent.com/aida-public/${promo.img}`} alt={promo.title}/>
              <div className="p-6">
                <h4 className="font-bold text-lg mb-2">{promo.title}</h4>
                <p className="text-sm text-slate-500 line-clamp-2">{promo.sub}</p>
                <a className="inline-block mt-4 text-orange-600 font-bold text-sm tracking-widest hover:underline underline-offset-4" href="#">XEM CHI TIẾT →</a>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Contact & Support */}
      <section className="mt-24 mb-24 max-w-7xl mx-auto px-8 grid grid-cols-1 md:grid-cols-2 gap-16 items-center">
        <div className="space-y-8">
          <div>
            <h2 className="text-4xl font-black tracking-tighter text-on-surface mb-4">KẾT NỐI VỚI STARCINE</h2>
            <p className="text-slate-500 max-w-md">Nhận thông tin phim mới nhất và các ưu đãi độc quyền dành riêng cho thành viên StarCine.</p>
          </div>
          <div className="grid grid-cols-2 gap-6">
            <div className="p-8 rounded-2xl bg-blue-50 border border-blue-100 flex flex-col items-center gap-4 transition-transform hover:-translate-y-2 cursor-pointer">
              <div className="w-12 h-12 rounded-full bg-blue-600 flex items-center justify-center text-white"><span className="material-symbols-outlined">forum</span></div>
              <span className="font-bold text-blue-900">Facebook</span>
            </div>
            <div className="p-8 rounded-2xl bg-cyan-50 border border-cyan-100 flex flex-col items-center gap-4 transition-transform hover:-translate-y-2 cursor-pointer">
              <div className="w-12 h-12 rounded-full bg-cyan-500 flex items-center justify-center text-white"><span className="material-symbols-outlined">chat</span></div>
              <span className="font-bold text-cyan-900">Zalo Official</span>
            </div>
          </div>
        </div>
        <div className="bg-white p-8 rounded-2xl shadow-xl shadow-slate-100 border-2 border-dashed border-slate-200 min-h-[400px] flex items-center justify-center">
          {contactStatus === 'success' ? (
            <div className="text-center space-y-6 animate-[fadeInUp_0.5s_ease-out]">
              <div className="w-20 h-20 bg-green-500 rounded-full flex items-center justify-center shadow-[0_0_30px_rgba(34,197,94,0.4)] mx-auto"><span className="material-symbols-outlined text-white text-4xl">check_circle</span></div>
              <div>
                <h3 className="text-2xl font-bold text-slate-800 mb-2">Gửi thành công!</h3>
                <p className="text-slate-500">Chúng tôi sẽ phản hồi bạn sớm nhất.</p>
              </div>
              <button onClick={() => setContactStatus('idle')} className="px-8 py-3 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-full text-sm font-bold transition-all">Gửi lại</button>
            </div>
          ) : (
            <form onSubmit={handleContactSubmit} className="space-y-6 w-full">
              <div className="grid grid-cols-2 gap-4">
                <input required className="w-full bg-slate-50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-orange-500" placeholder="Họ tên" type="text" />
                <input required className="w-full bg-slate-50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-orange-500" placeholder="Email" type="email" />
              </div>
              <textarea required className="w-full bg-slate-50 border-none rounded-xl py-3 px-4 focus:ring-2 focus:ring-orange-500" placeholder="Tin nhắn..." rows={4}></textarea>
              <button disabled={contactStatus === 'submitting'} className="w-full py-4 text-white font-black tracking-widest rounded-xl shadow-lg transition-all flex items-center justify-center bg-gradient-to-r from-orange-500 to-red-500 shadow-orange-500/20 active:scale-95">
                {contactStatus === 'submitting' ? <Spinner /> : 'GỬI YÊU CẦU'}
              </button>
            </form>
          )}
        </div>
      </section>
    </main>
  );
}
