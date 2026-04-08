import { Link } from 'react-router-dom';

/**
 * MovieCard — Accepts both legacy mock data and MovieDTO from API.
 * 
 * From API (MovieDTO):
 *   movieId, title, posterUrl, ageRating (T13/T16/T18/P/C/K), 
 *   durationMinutes, language, trailerUrl, status
 * 
 * Legacy mock:
 *   title, img, tag, type, genre, duration, country, subtitle, isComingSoon, trailer
 * 
 * Extra (from parent):
 *   genreText — pre-formatted genre string (e.g. "Hành Động, Phiêu Lưu")
 *   screenType — from showtime (e.g. "2D", "IMAX")
 */
const MovieCard = ({ movie }) => {
  const {
    // movieId — used by parent for key/navigation, not inside this component
    title,
    // Backend uses posterUrl, legacy uses img
    posterUrl,
    img,
    // Backend uses ageRating enum, legacy uses tag
    ageRating,
    tag,
    // screenType from showtime or legacy 'type'
    screenType,
    type,
    // Extra
    genreText,
    genre,
    durationMinutes,
    duration,
    language,
    country,
    subtitle,
    isComingSoon = false,
    trailerUrl,
    trailer,
    status,
  } = movie;

  // Resolved values (prioritize API fields, fallback to legacy)
  const resolvedImg = posterUrl || img || '';
  const resolvedTag = ageRating || tag || 'P';
  const resolvedType = screenType || type || '2D';
  const resolvedGenre = genreText || genre || '';
  const resolvedDuration = durationMinutes ? `${durationMinutes} Phút` : (duration || '');
  const resolvedCountry = language || country || '';
  const resolvedSubtitle = subtitle || 'VN';
  const resolvedTrailer = trailerUrl || trailer || '';
  const resolvedComingSoon = isComingSoon || status === 'COMING_SOON';

  // Build image src
  const imageSrc = resolvedImg.startsWith('http')
    ? resolvedImg
    : `https://lh3.googleusercontent.com/aida-public/${resolvedImg}`;

  return (
    <div className="group relative rounded-2xl overflow-hidden h-[450px] transition-all duration-500 hover:-translate-y-2 hover:shadow-2xl hover:shadow-orange-500/30 bg-slate-900 border border-white/5">
      {/* Poster Image — absolute fill to prevent size variance */}
      <div className="absolute inset-0">
        <img
          className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
          src={imageSrc}
          alt={title}
        />
      </div>

      {/* Top Left Badges */}
      <div className="absolute top-4 left-4 flex gap-2">
        <div className="px-2 py-1 bg-gradient-to-br from-orange-400 to-orange-600 rounded-md border border-black shadow-lg">
          <span className="text-black font-black text-xs">{resolvedType}</span>
        </div>
        <div className="px-2 py-1 bg-red-600 rounded-md border border-white/10 shadow-lg">
          <span className="text-white font-black text-xs">{resolvedTag}</span>
        </div>
      </div>

      {/* Hover Overlay */}
      <div className="absolute inset-0 bg-black/85 backdrop-blur-[2px] opacity-0 group-hover:opacity-100 transition-all duration-500 flex flex-col justify-center p-8">
        <h3 className="text-2xl font-black text-white leading-tight mb-8 tracking-tight uppercase">
          {title} ({resolvedTag})
        </h3>

        <div className="space-y-4 mb-10">
          {resolvedGenre && (
            <div className="flex items-center gap-4 text-slate-300">
              <span className="material-symbols-outlined text-orange-500 text-xl">label</span>
              <span className="font-bold text-base">{resolvedGenre}</span>
            </div>
          )}
          {resolvedDuration && (
            <div className="flex items-center gap-4 text-slate-300">
              <span className="material-symbols-outlined text-orange-500 text-xl">schedule</span>
              <span className="font-bold text-base">{resolvedDuration}</span>
            </div>
          )}
          {resolvedCountry && (
            <div className="flex items-center gap-4 text-slate-300">
              <span className="material-symbols-outlined text-orange-500 text-xl">public</span>
              <span className="font-bold text-base">{resolvedCountry}</span>
            </div>
          )}
          <div className="flex items-center gap-4 text-slate-300">
            <span className="material-symbols-outlined text-orange-500 text-xl">chat_bubble</span>
            <span className="font-bold text-base">{resolvedSubtitle}</span>
          </div>
        </div>

        <div className="flex flex-col gap-3">
          {resolvedComingSoon ? (
            <button className="w-full py-4 rounded-xl border-2 border-white/20 text-white/40 font-black tracking-widest cursor-not-allowed uppercase">
              Chờ đón
            </button>
          ) : (
            <>
              <Link
                to="/booking/seats"
                className="w-full py-4 rounded-xl bg-gradient-to-r from-orange-500 to-red-600 text-white font-black tracking-widest text-center shadow-lg shadow-orange-500/40 hover:scale-[1.02] active:scale-95 transition-all uppercase"
              >
                Mua vé
              </Link>
              {resolvedTrailer && (
                <button
                  onClick={(e) => {
                    e.preventDefault();
                    window.open(resolvedTrailer, '_blank');
                  }}
                  className="w-full py-3 rounded-xl bg-white/10 hover:bg-white/20 text-white font-bold tracking-wider text-center border border-white/10 transition-all flex items-center justify-center gap-2"
                >
                  <span className="material-symbols-outlined text-lg">play_circle</span>
                  TRAILER
                </button>
              )}
            </>
          )}
        </div>
      </div>

      {/* Bottom Title (Pre-hover) */}
      {!resolvedComingSoon && (
        <div className="absolute bottom-0 left-0 right-0 p-6 bg-gradient-to-t from-black/90 via-black/40 to-transparent group-hover:opacity-0 transition-opacity duration-300">
          <h3 className="text-white font-black italic tracking-tight truncate drop-shadow-lg text-lg">
            {title}
          </h3>
        </div>
      )}
    </div>
  );
};

export default MovieCard;

