import { BOOKING_ACTIONS } from '../bookingActionTypes';
import { createBooking } from '../../services/bookingService';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

/**
 * Command Pattern — SubmitCheckoutCommand
 * Đóng gói hành động submit thanh toán: gọi API checkout và dispatch kết quả.
 * 
 * @param {number} userId - ID người dùng
 * @param {string} paymentMethod - 'momo' hoặc 'demo'
 */
export class SubmitCheckoutCommand {
  constructor(userId, paymentMethod = 'demo') {
    this.userId = userId;
    this.paymentMethod = paymentMethod;
  }

  async execute(dispatch, getState) {
    const state = getState();

    // Validation
    if (!state.selectedShowtime) {
      throw new Error('Thiếu thông tin suất chiếu.');
    }
    if (!state.selectedSeats || state.selectedSeats.length === 0) {
      throw new Error('Vui lòng chọn ít nhất 1 ghế.');
    }

    const payload = {
      userId: this.userId,
      showtimeId: state.selectedShowtime.showtimeId,
      tickets: state.selectedSeats.map(s => ({ seatId: s.seatId })),
      fnbLines: (state.selectedFnbs || [])
        .filter(f => f.quantity > 0)
        .map(f => ({ itemId: f.itemId, quantity: f.quantity })),
      promoCode: state.voucherCode || null,
    };

    if (this.paymentMethod === 'momo') {
      // Gọi API checkout MoMo thật → trả về payUrl
      const payUrl = await createBooking(payload);
      return { type: 'momo', payUrl };
    } else {
      // Gọi API demo checkout
      const { getAuthHeaders } = await import('../../utils/api');
      const body = {
        userId: payload.userId,
        showtimeId: payload.showtimeId,
        seatIds: payload.tickets.map(t => t.seatId),
        fnbs: payload.fnbLines,
        promoCode: payload.promoCode,
      };

      const res = await fetch(`${BASE_URL}/payment/checkout/demo?success=true`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(body),
      });

      if (!res.ok) {
        const msg = await res.text();
        throw new Error(msg || 'Lỗi Demo Checkout');
      }

      const result = await res.json();
      return { type: 'demo', ...result };
    }
  }
}
