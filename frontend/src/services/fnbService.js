import { BASE_URL } from '../utils/api';

// ── PUBLIC endpoints ────────────────────────────────────────────────

export const fetchFnBCategories = async () => {
  const res = await fetch(`${BASE_URL}/public/fnb/categories`);
  if (!res.ok) throw new Error('Không thể tải danh mục F&B');
  return res.json();
};

/** @param {number} [cinemaId] - lọc menu theo chi nhánh / rạp */
export const fetchFnBItems = async (cinemaId) => {
  const qs =
    cinemaId != null && cinemaId !== ''
      ? `?cinemaId=${encodeURIComponent(cinemaId)}`
      : '';
  const res = await fetch(`${BASE_URL}/public/fnb/items${qs}`);
  if (!res.ok) throw new Error('Không thể tải sản phẩm F&B');
  return res.json();
};
