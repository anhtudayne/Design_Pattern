import { BASE_URL, getAuthHeaders } from '../utils/api';

// ── REQUIRES AUTH (JWT) ─────────────────────────────────────────────

/** Fetch seat map + status for a showtime */
export const fetchSeatStatuses = async (showtimeId) => {
  const res = await fetch(`${BASE_URL}/booking/seats/${showtimeId}`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Không thể tải sơ đồ ghế');
  return res.json();
};

/** Lock a seat via Redis SETNX (10-min TTL) */
export const lockSeat = async (showtimeId, seatId, userId) => {
  const res = await fetch(
    `${BASE_URL}/booking/lock?showtimeId=${showtimeId}&seatId=${seatId}&userId=${userId}`,
    { method: 'POST', headers: getAuthHeaders() }
  );
  if (!res.ok) {
    const msg = await res.text();
    throw new Error(msg || 'Ghế đã có người giữ hoặc đã bán');
  }
  return res.text();
};

/** Unlock a previously locked seat */
export const unlockSeat = async (showtimeId, seatId) => {
  const res = await fetch(
    `${BASE_URL}/booking/unlock?showtimeId=${showtimeId}&seatId=${seatId}`,
    { method: 'POST', headers: getAuthHeaders() }
  );
  if (!res.ok) throw new Error('Không thể giải phóng ghế');
  return res.text();
};

/**
 * Calculate price breakdown.
 * @param {Object} body - { showtimeId, seatIds: [], fnbs: [{itemId, quantity}], promoCode }
 * @returns {Promise<PriceBreakdownDTO>} { ticketTotal, fnbTotal, discountAmount, finalTotal }
 */
export const calculatePrice = async (body) => {
  const res = await fetch(`${BASE_URL}/booking/calculate`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error('Không thể tính giá');
  return res.json();
};
