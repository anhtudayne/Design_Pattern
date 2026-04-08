import { BASE_URL } from '../utils/api';

// ── PUBLIC endpoint (uses enriched ShowtimeDTO) ─────────────────────

/**
 * Fetch showtimes with optional filters.
 * @param {Object} filters - { cinemaId, movieId, date (YYYY-MM-DD) }
 * @returns {Promise<ShowtimeDTO[]>} Enriched showtimes with movieTitle, cinemaName, roomName, etc.
 */
export const fetchPublicShowtimes = async (filters = {}) => {
  const params = new URLSearchParams();
  if (filters.cinemaId) params.append('cinemaId', filters.cinemaId);
  if (filters.movieId) params.append('movieId', filters.movieId);
  if (filters.date) params.append('date', filters.date);

  const query = params.toString() ? `?${params.toString()}` : '';
  const res = await fetch(`${BASE_URL}/public/showtimes${query}`);
  if (!res.ok) throw new Error('Không thể tải lịch chiếu');
  return res.json();
};
