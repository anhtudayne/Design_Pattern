import { BASE_URL } from '../utils/api';

// ── PUBLIC endpoints ────────────────────────────────────────────────

export const fetchCinemas = async () => {
  const res = await fetch(`${BASE_URL}/public/cinemas`);
  if (!res.ok) throw new Error('Không thể tải danh sách rạp');
  return res.json();
};

export const fetchLocations = async () => {
  const res = await fetch(`${BASE_URL}/public/locations`);
  if (!res.ok) throw new Error('Không thể tải danh sách tỉnh/thành');
  return res.json();
};
