import { BASE_URL } from '../utils/api';

// ── PUBLIC endpoints ────────────────────────────────────────────────

export const fetchFnBItems = async () => {
  const res = await fetch(`${BASE_URL}/public/fnb/items`);
  if (!res.ok) throw new Error('Không thể tải sản phẩm F&B');
  return res.json();
};
