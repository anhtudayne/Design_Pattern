import { BASE_URL } from '../utils/api';

// ── PUBLIC endpoints (no auth required) ─────────────────────────────

export const fetchNowShowingMovies = async () => {
  const res = await fetch(`${BASE_URL}/public/movies/now-showing`);
  if (!res.ok) throw new Error('Không thể tải danh sách phim đang chiếu');
  return res.json();
};

export const fetchComingSoonMovies = async () => {
  const res = await fetch(`${BASE_URL}/public/movies/coming-soon`);
  if (!res.ok) throw new Error('Không thể tải danh sách phim sắp chiếu');
  return res.json();
};

export const fetchMovieGenres = async (movieId) => {
  const res = await fetch(`${BASE_URL}/movie-genres/${movieId}`);
  if (!res.ok) return []; // graceful fallback
  return res.json();
};
