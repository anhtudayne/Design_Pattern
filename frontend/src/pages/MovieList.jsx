import { useState, useRef, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBooking } from '../contexts/BookingContext';
import { fetchLocations, fetchCinemas } from '../services/cinemaService';
import { fetchPublicShowtimes } from '../services/showtimeService';

export default function MovieList() {
  const navigate = useNavigate();
  const { setBookingSelection } = useBooking();

  // --- Dropdown toggle states ---
  const [isCityOpen, setIsCityOpen] = useState(false);
  const [isCinemaOpen, setIsCinemaOpen] = useState(false);

  // --- Data states ---
  const [cities, setCities] = useState([]);
  const [cinemas, setCinemas] = useState([]);
  const [allShowtimes, setAllShowtimes] = useState([]);
  const [loadingLocations, setLoadingLocations] = useState(true);
  const [loadingCinemas, setLoadingCinemas] = useState(false);
  const [loadingShowtimes, setLoadingShowtimes] = useState(false);

  // --- Selection states ---
  const [selectedCity, setSelectedCity] = useState(null);
  const [selectedCinema, setSelectedCinema] = useState(null);
  const [isConfirmed, setIsConfirmed] = useState(false);
  const [activeDate, setActiveDate] = useState(0);
  const [selectedShowtimeSlot, setSelectedShowtimeSlot] = useState(null);

  // --- Refs for click-outside ---
  const cityRef = useRef(null);
  const cinemaRef = useRef(null);

  // Fetch locations on mount
  useEffect(() => {
    const loadLocations = async () => {
      try {
        const data = await fetchLocations();
        setCities(data);
      } catch (err) {
        console.error('Failed to load locations', err);
      } finally {
        setLoadingLocations(false);
      }
    };
    loadLocations();
  }, []);

  // Fetch cinemas when city changes
  useEffect(() => {
    if (!selectedCity) {
      setCinemas([]);
      return;
    }
    const loadCinemas = async () => {
      setLoadingCinemas(true);
      try {
        const data = await fetchCinemas();
        setCinemas(data.filter(c => c.locationId === selectedCity.locationId));
      } catch (err) {
        console.error('Failed to load cinemas', err);
      } finally {
        setLoadingCinemas(false);
      }
    };
    loadCinemas();
  }, [selectedCity]);

  useEffect(() => {
    const handler = (e) => {
      if (cityRef.current && !cityRef.current.contains(e.target)) setIsCityOpen(false);
      if (cinemaRef.current && !cinemaRef.current.contains(e.target)) setIsCinemaOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // Generate 5 dates starting from today
  const dates = useMemo(() => {
    const dayNames = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
    const dts = [];
    for (let i = 0; i < 5; i++) {
      const d = new Date();
      d.setDate(d.getDate() + i);
      const dateStr = d.toISOString().split('T')[0];
      dts.push({
        label: i === 0 ? 'Hôm nay' : dayNames[d.getDay()],
        date: d.getDate(),
        month: d.getMonth() + 1,
        full: d,
        dateStr,
      });
    }
    return dts;
  }, []);

  // --- Group showtimes by movie ---
  const filteredMovies = useMemo(() => {
    if (!isConfirmed || allShowtimes.length === 0) return [];
    
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
      
      const movieData = movieMap.get(st.movieId);
      movieData.showtimes.push(st);
    });

    return Array.from(movieMap.values());
  }, [isConfirmed, allShowtimes, activeDate, dates]);

  // --- Handlers ---
  const handleSelectCity = (city) => {
    setSelectedCity(city);
    setSelectedCinema(null);
    setIsConfirmed(false);
    setIsCityOpen(false);
  };

  const handleSelectCinema = (cinema) => {
    setSelectedCinema(cinema);
    setIsConfirmed(false);
    setIsCinemaOpen(false);
  };

  const handleConfirm = async () => {
    if (!selectedCity || !selectedCinema) {
      alert("Vui lòng chọn Thành phố và Rạp trước khi xem lịch chiếu!");
      return;
    }
    setLoadingShowtimes(true);
    try {
      const data = await fetchPublicShowtimes({ cinemaId: selectedCinema.cinemaId });
      setAllShowtimes(data);
      setIsConfirmed(true);
      setActiveDate(0);
    } catch (err) {
      console.error('Failed to fetch showtimes', err);
      alert("Đã có lỗi xảy ra khi tải lịch chiếu.");
    } finally {
      setLoadingShowtimes(false);
    }
  };

  const handleSelectSlot = (movie, st) => {
    setSelectedShowtimeSlot({
      movie,
      showtime: st,
      cinema: selectedCinema,
      city: selectedCity,
    });
  };

  const confirmBooking = () => {
    const { movie, showtime, cinema } = selectedShowtimeSlot;
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

  return (
    <main className="pt-44 pb-20 bg-slate-50 dark:bg-slate-950 min-h-screen">

      {/* Page Title */}
      <section className="max-w-7xl mx-auto px-6 md:px-8 mb-10">
        <h1 className="text-4xl md:text-5xl font-black text-slate-800 dark:text-white tracking-tighter uppercase flex items-center gap-4">
          <span className="w-2 h-10 bg-gradient-to-b from-orange-500 to-red-500 rounded-full"></span>
          Lịch Chiếu Phim
        </h1>
        <p className="text-slate-500 mt-3 text-lg font-medium ml-6">Chọn thành phố và rạp để xem lịch chiếu hôm nay</p>
      </section>

      {/* Cinema Selection Bar */}
      <section className="max-w-7xl mx-auto px-6 md:px-8 mb-12">
        <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 shadow-xl rounded-[2rem] p-6 md:p-8">
          <div className="flex flex-col md:flex-row items-stretch md:items-center gap-4">

            {/* City Dropdown */}
            <div ref={cityRef} className="flex-1 relative">
              <button
                onClick={() => { setIsCityOpen(!isCityOpen); setIsCinemaOpen(false); }}
                className="w-full flex items-center gap-3 px-6 py-4 bg-slate-50 dark:bg-slate-800 rounded-2xl hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors group"
                disabled={loadingLocations}
              >
                <span className="material-symbols-outlined text-2xl text-orange-500">location_city</span>
                <div className="flex-1 text-left">
                  <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Thành Phố</p>
                  <p className="text-sm font-bold text-slate-800 dark:text-white truncate">
                    {selectedCity ? selectedCity.name : (loadingLocations ? 'Đang tải...' : 'Chọn thành phố...')}
                  </p>
                </div>
                <span className={`material-symbols-outlined text-slate-400 transition-transform ${isCityOpen ? 'rotate-180' : ''}`}>expand_more</span>
              </button>

              {isCityOpen && (
                <div className="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl shadow-2xl z-40 overflow-hidden animate-[fadeIn_0.2s_ease-out]">
                  {cities.map(city => (
                    <button
                      key={city.locationId}
                      onClick={() => handleSelectCity(city)}
                      className={`w-full flex items-center gap-3 px-6 py-4 text-left hover:bg-orange-50 dark:hover:bg-orange-500/10 transition-colors ${
                        selectedCity?.locationId === city.locationId ? 'bg-orange-50 dark:bg-orange-500/10 text-orange-600' : 'text-slate-700 dark:text-slate-300'
                      }`}
                    >
                      <span className="material-symbols-outlined text-lg">apartment</span>
                      <span className="font-bold text-sm">{city.name}</span>
                      {selectedCity?.locationId === city.locationId && (
                        <span className="material-symbols-outlined text-orange-500 ml-auto text-lg">check_circle</span>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>

            <div className="hidden md:block w-px h-14 bg-slate-200 dark:bg-slate-700"></div>

            {/* Cinema Dropdown */}
            <div ref={cinemaRef} className="flex-1 relative">
              <button
                onClick={() => { if (selectedCity) { setIsCinemaOpen(!isCinemaOpen); setIsCityOpen(false); } else { alert("Vui lòng chọn Thành phố trước!"); } }}
                className={`w-full flex items-center gap-3 px-6 py-4 rounded-2xl transition-colors group ${
                  selectedCity
                    ? 'bg-slate-50 dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700'
                    : 'bg-slate-50/50 dark:bg-slate-800/50 opacity-60 cursor-not-allowed'
                }`}
                disabled={loadingCinemas}
              >
                <span className="material-symbols-outlined text-2xl text-cyan-500">movie</span>
                <div className="flex-1 text-left">
                  <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Rạp Chiếu</p>
                  <p className="text-sm font-bold text-slate-800 dark:text-white truncate">
                    {selectedCinema ? selectedCinema.name : (loadingCinemas ? 'Đang tải...' : 'Chọn rạp chiếu...')}
                  </p>
                </div>
                <span className={`material-symbols-outlined text-slate-400 transition-transform ${isCinemaOpen ? 'rotate-180' : ''}`}>expand_more</span>
              </button>

              {isCinemaOpen && cinemas.length > 0 && (
                <div className="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl shadow-2xl z-40 overflow-hidden animate-[fadeIn_0.2s_ease-out]">
                  {cinemas.map(cinema => (
                    <button
                      key={cinema.cinemaId}
                      onClick={() => handleSelectCinema(cinema)}
                      className={`w-full flex items-center gap-3 px-6 py-4 text-left hover:bg-orange-50 dark:hover:bg-orange-500/10 transition-colors ${
                        selectedCinema?.cinemaId === cinema.cinemaId ? 'bg-orange-50 dark:bg-orange-500/10 text-orange-600' : 'text-slate-700 dark:text-slate-300'
                      }`}
                    >
                      <span className="material-symbols-outlined text-lg">theaters</span>
                      <span className="font-bold text-sm">{cinema.name}</span>
                      {selectedCinema?.cinemaId === cinema.cinemaId && (
                        <span className="material-symbols-outlined text-orange-500 ml-auto text-lg">check_circle</span>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>

            <button
              onClick={handleConfirm}
              disabled={loadingShowtimes}
              className="px-10 py-4 rounded-2xl bg-gradient-to-r from-orange-500 to-red-500 text-white font-black uppercase tracking-widest shadow-lg shadow-orange-500/30 hover:shadow-orange-500/50 hover:scale-[1.02] hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2 shrink-0 group disabled:opacity-50"
            >
              <span className={`material-symbols-outlined ${loadingShowtimes ? 'animate-spin' : 'group-hover:rotate-12 transition-transform'}`}>
                {loadingShowtimes ? 'sync' : 'search'}
              </span>
              {loadingShowtimes ? 'ĐANG TẢI...' : 'XEM LỊCH CHIẾU'}
            </button>
          </div>
        </div>
      </section>

      {/* Content Area */}
      {!isConfirmed ? (
        <section className="max-w-7xl mx-auto px-6 md:px-8">
          <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-[2.5rem] shadow-xl p-16 md:p-24 flex flex-col items-center justify-center text-center">
            <div className="w-24 h-24 bg-orange-50 dark:bg-orange-500/10 rounded-full flex items-center justify-center mb-8">
              <span className="material-symbols-outlined text-5xl text-orange-400">local_movies</span>
            </div>
            <h2 className="text-2xl font-black text-slate-800 dark:text-white mb-3 tracking-tight">Chọn rạp để xem lịch chiếu</h2>
            <p className="text-slate-400 max-w-md font-medium leading-relaxed">
              Hãy chọn <span className="text-orange-500 font-bold">Thành Phố</span> và <span className="text-cyan-500 font-bold">Rạp chiếu</span> ở thanh phía trên, sau đó bấm <span className="font-bold text-slate-600 dark:text-slate-300">"XEM LỊCH CHIẾU"</span> để xem danh sách phim đang chiếu hôm nay.
            </p>
          </div>
        </section>
      ) : (
        <section className="max-w-7xl mx-auto px-6 md:px-8">
          <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 shadow-xl rounded-[2.5rem] p-8 md:p-10">

            <div className="flex items-center gap-3 mb-8 pb-6 border-b border-slate-100 dark:border-slate-800">
              <div className="w-10 h-10 bg-gradient-to-tr from-orange-500 to-red-500 rounded-xl flex items-center justify-center shadow-lg shadow-orange-500/20">
                <span className="material-symbols-outlined text-white text-xl">theaters</span>
              </div>
              <div>
                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Đang xem lịch chiếu tại</p>
                <p className="text-lg font-black text-slate-800 dark:text-white">{selectedCinema?.name} — <span className="text-orange-500">{selectedCity?.name}</span></p>
              </div>
              <button
                onClick={() => { setIsConfirmed(false); setSelectedCinema(null); setSelectedCity(null); setSelectedShowtimeSlot(null); }}
                className="ml-auto px-4 py-2 rounded-xl bg-slate-50 dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-500 text-xs font-bold transition-colors flex items-center gap-1"
              >
                <span className="material-symbols-outlined text-sm">swap_horiz</span>
                Đổi rạp
              </button>
            </div>

            {/* Date Tabs */}
            <div className="flex gap-4 mb-10 overflow-x-auto pb-4 no-scrollbar">
              {dates.map((item, i) => {
                const isActive = i === activeDate;
                return (
                  <button
                    key={i}
                    onClick={() => { setActiveDate(i); setSelectedShowtimeSlot(null); }}
                    className={`min-w-[110px] py-4 rounded-3xl flex flex-col items-center border-2 border-transparent transition-all duration-300 shrink-0 ${
                      isActive
                        ? "bg-gradient-to-b from-orange-400 to-red-500 text-white shadow-xl shadow-orange-500/40 -translate-y-1"
                        : "bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:border-orange-500/50 hover:bg-white dark:hover:bg-slate-900"
                    }`}
                  >
                    <span className={`text-[10px] font-black uppercase tracking-widest ${isActive ? "text-white/80" : "text-slate-400"}`}>{item.label}</span>
                    <span className="text-4xl font-black tracking-tighter my-1">{item.date}</span>
                    <span className={`text-[10px] font-black uppercase ${isActive ? "text-white/80" : "text-slate-400"}`}>Tháng {item.month}</span>
                  </button>
                );
              })}
            </div>

            <div className="space-y-8">
              {filteredMovies.length > 0 ? filteredMovies.map((movie) => (
                <div key={movie.movieId} className="flex flex-col sm:flex-row gap-6 p-6 md:p-8 bg-slate-50 dark:bg-slate-800/50 rounded-[2rem] border border-slate-100 dark:border-slate-800 transition-all hover:bg-white dark:hover:bg-slate-800 hover:shadow-2xl">
                  <div className="w-full sm:w-[140px] shrink-0 h-[210px] rounded-xl overflow-hidden shadow-lg group relative">
                    <img
                      alt={movie.title}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                      src={movie.posterUrl && movie.posterUrl.startsWith('http') ? movie.posterUrl : `https://lh3.googleusercontent.com/aida-public/${movie.posterUrl}`}
                    />
                    <div className="absolute top-2 left-2 flex flex-col gap-1">
                      <span className="px-2 py-0.5 rounded text-[10px] font-black text-white uppercase shadow-sm bg-red-600">{movie.ageRating}</span>
                      <span className="px-2 py-0.5 rounded text-[10px] font-black text-slate-700 bg-white/90 backdrop-blur shadow-sm">{movie.screenType || '2D'}</span>
                    </div>
                  </div>

                  <div className="flex-grow">
                    <h3 className="text-2xl font-black text-slate-800 dark:text-white mb-1 uppercase tracking-tight leading-tight">{movie.title}</h3>
                    <p className="text-slate-500 font-medium text-sm mb-6 flex items-center gap-2">
                      <span className="material-symbols-outlined text-base">schedule</span>
                      {movie.durationMinutes} phút
                    </p>

                    <div className="space-y-5">
                      <div className="flex flex-wrap gap-3">
                        {movie.showtimes
                          .sort((a,b) => a.startTime.localeCompare(b.startTime))
                          .map((st) => {
                            const time = st.startTime.split('T')[1]?.substring(0, 5);
                            return (
                              <button
                                key={st.showtimeId}
                                onClick={() => handleSelectSlot(movie, st)}
                                className="px-6 py-2.5 rounded-xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 font-bold hover:border-orange-500 hover:text-orange-500 transition-all shadow-sm"
                              >
                                {time}
                              </button>
                            );
                          })}
                      </div>
                    </div>
                  </div>
                </div>
              )) : (
                <div className="text-center py-16">
                  <span className="material-symbols-outlined text-5xl text-slate-300 mb-4 block">movie_off</span>
                  <p className="text-slate-400 font-bold">Không có phim nào đang chiếu tại rạp này vào ngày {dates[activeDate].date}/{dates[activeDate].month}</p>
                </div>
              )}
            </div>
          </div>
        </section>
      )}

      {selectedShowtimeSlot && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div
            className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm"
            onClick={() => setSelectedShowtimeSlot(null)}
          ></div>

          <div className="relative bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl shadow-2xl p-8 max-w-sm w-full animate-[zoomIn_0.3s_ease-out]">
            <button
              onClick={() => setSelectedShowtimeSlot(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-800 dark:hover:text-white transition-colors"
            >
              <span className="material-symbols-outlined">close</span>
            </button>

            <div className="text-center mb-8">
              <div className="w-16 h-16 bg-orange-50 dark:bg-orange-500/10 rounded-full flex items-center justify-center text-orange-500 mx-auto mb-4">
                <span className="material-symbols-outlined text-3xl">local_activity</span>
              </div>
              <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-2">Xác nhận suất chiếu</h2>
              <p className="text-slate-500 text-sm">Vui lòng kiểm tra lại thông tin vé</p>
            </div>

            <div className="bg-slate-50 dark:bg-slate-800/50 rounded-2xl p-5 mb-8 border border-slate-100 dark:border-slate-800 space-y-3">
              <div className="flex justify-between border-b border-slate-200/50 dark:border-slate-700/50 pb-2">
                <span className="text-xs font-bold text-slate-400 uppercase">Phim</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white text-right max-w-[180px] line-clamp-1">{selectedShowtimeSlot.movie.title}</span>
              </div>
              <div className="flex justify-between border-b border-slate-200/50 dark:border-slate-700/50 pb-2">
                <span className="text-xs font-bold text-slate-400 uppercase">Rạp</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white">{selectedShowtimeSlot.cinema.name}</span>
              </div>
              <div className="flex justify-between border-b border-slate-200/50 dark:border-slate-700/50 pb-2">
                <span className="text-xs font-bold text-slate-400 uppercase">Ngày chiếu</span>
                <span className="text-sm font-bold text-slate-800 dark:text-white">{dates[activeDate].label} — {dates[activeDate].date}/{dates[activeDate].month}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-xs font-bold text-slate-400 uppercase">Giờ chiếu</span>
                <span className="text-sm font-bold text-orange-500">{selectedShowtimeSlot.showtime.startTime.split('T')[1].substring(0, 5)}</span>
              </div>
            </div>

            <button
              onClick={confirmBooking}
              className="w-full py-4 rounded-xl bg-gradient-to-r from-orange-500 to-red-500 text-white font-black tracking-widest uppercase shadow-lg shadow-orange-500/30 hover:shadow-orange-500/50 hover:-translate-y-0.5 transition-all flex items-center justify-center gap-2"
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
