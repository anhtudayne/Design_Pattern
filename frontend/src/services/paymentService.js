import { BASE_URL, getAuthHeaders } from '../utils/api';

// ── REQUIRES AUTH ───────────────────────────────────────────────────

/**
 * Handle checkout and redirect to MoMo.
 * @param {Object} body - { userId, showtimeId, seatIds, fnbs, promoCode }
 * @returns {Promise<string>} MoMo Payment URL
 */
export const payMoMo = async (body) => {
  const res = await fetch(`${BASE_URL}/payment/checkout`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const msg = await res.text();
    throw new Error(msg || 'Lỗi thanh toán MoMo');
  }
  const data = await res.json();
  if (!data?.payUrl || typeof data.payUrl !== 'string') {
    throw new Error('Không nhận được link thanh toán MoMo hợp lệ từ hệ thống');
  }
  return data.payUrl;
};

export const getUserPayments = async (userId) => {
  const res = await fetch(`${BASE_URL}/payment/history/${userId}`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Lỗi lấy lịch sử');
  return res.json();
};

export const demoCheckout = async (body, success = true) => {
  const res = await fetch(`${BASE_URL}/payment/checkout/demo?success=${success}`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const msg = await res.text();
    throw new Error(msg || 'Demo checkout thất bại');
  }
  return res.json();
};

export const getPaymentDetail = async (paymentId) => {
  const res = await fetch(`${BASE_URL}/payment/details/${paymentId}`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Lỗi lấy chi tiết');
  return res.json();
};
