import { BASE_URL, getAuthHeaders } from '../utils/api';

// ── REQUIRES AUTH ───────────────────────────────────────────────────

/**
 * Factory/Strategy phía server: {@code paymentMethod} = MOMO | VNPAY.
 * @param {Object} body - userId, showtimeId, seatIds, fnbs, promoCode, paymentMethod
 * @returns {Promise<string>} payUrl
 */
export const requestCheckoutPayUrl = async (body) => {
  const payload = {
    userId: body.userId,
    showtimeId: body.showtimeId,
    seatIds: body.seatIds,
    fnbs: body.fnbs ?? [],
    promoCode: body.promoCode ?? null,
    paymentMethod: body.paymentMethod ?? 'MOMO',
  };
  const res = await fetch(`${BASE_URL}/payment/checkout`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const text = await res.text();
    let msg = text;
    try {
      const j = JSON.parse(text);
      if (j?.message) msg = j.message;
    } catch {
      /* giữ nguyên text */
    }
    throw new Error(msg || 'Không tạo được link thanh toán');
  }
  const data = await res.json();
  if (!data?.payUrl || typeof data.payUrl !== 'string') {
    throw new Error('Không nhận được payUrl hợp lệ từ hệ thống');
  }
  return data.payUrl;
};

/** Alias MoMo — tương thích POS; tương đương requestCheckoutPayUrl(..., paymentMethod: 'MOMO'). */
export const payMoMo = async (body) => requestCheckoutPayUrl({ ...body, paymentMethod: body.paymentMethod ?? 'MOMO' });

export const payVnpay = async (body) => requestCheckoutPayUrl({ ...body, paymentMethod: 'VNPAY' });

/** MoMo QR mô phỏng — sau khi bấm Thành công / Thất bại */
export const finishMomoUiCheckout = async (checkoutPayload, success) => {
  const res = await fetch(`${BASE_URL}/payment/checkout/momo-ui/finish`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ ...checkoutPayload, success }),
  });
  if (!res.ok) {
    const text = await res.text();
    let msg = text;
    try {
      const j = JSON.parse(text);
      if (j?.message) msg = j.message;
    } catch {
      /* noop */
    }
    throw new Error(msg || 'Không xác nhận được thanh toán MoMo');
  }
  return res.json();
};

/** VNPay QR mô phỏng — sau khi bấm Thành công / Thất bại */
export const finishVnpayUiCheckout = async (checkoutPayload, success) => {
  const res = await fetch(`${BASE_URL}/payment/checkout/vnpay-ui/finish`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ ...checkoutPayload, success }),
  });
  if (!res.ok) {
    const text = await res.text();
    let msg = text;
    try {
      const j = JSON.parse(text);
      if (j?.message) msg = j.message;
    } catch {
      /* noop */
    }
    throw new Error(msg || 'Không xác nhận được thanh toán VNPay');
  }
  return res.json();
};

export const getUserPayments = async (userId) => {
  const res = await fetch(`${BASE_URL}/payment/history/${userId}`, {
    headers: getAuthHeaders(),
  });
  if (!res.ok) throw new Error('Lỗi lấy lịch sử');
  return res.json();
};

/** Checkout tiền mặt (khách web) — backend: CashPaymentStrategy / StaffCashCheckoutProcess */
export const cashCheckout = async (body) => {
  const res = await fetch(`${BASE_URL}/payment/checkout/cash`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const msg = await res.text();
    throw new Error(msg || 'Thanh toán tiền mặt thất bại');
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
