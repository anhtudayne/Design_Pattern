import { BOOKING_ACTIONS } from '../bookingActionTypes';
import { calculatePrice } from '../../services/bookingService';

/**
 * Command Pattern — ApplyVoucherCommand
 * Đóng gói hành động áp dụng voucher: gọi API tính giá với mã giảm giá → dispatch kết quả.
 * 
 * @param {string} voucherCode - Mã voucher
 */
export class ApplyVoucherCommand {
  constructor(voucherCode) {
    this.voucherCode = voucherCode;
  }

  async execute(dispatch, getState) {
    const state = getState();

    if (!state.selectedShowtime || !state.selectedSeats || state.selectedSeats.length === 0) {
      throw new Error('Vui lòng chọn suất chiếu và ghế trước khi áp dụng voucher.');
    }

    // Lưu voucher code vào state
    dispatch({ type: BOOKING_ACTIONS.SET_VOUCHER_CODE, payload: this.voucherCode });

    // Gọi API tính giá với voucher
    const priceResult = await calculatePrice({
      showtimeId: state.selectedShowtime.showtimeId,
      seatIds: state.selectedSeats.map(s => s.seatId),
      fnbs: (state.selectedFnbs || [])
        .filter(f => f.quantity > 0)
        .map(f => ({ itemId: f.itemId, quantity: f.quantity })),
      promoCode: this.voucherCode || null,
    });

    dispatch({ type: BOOKING_ACTIONS.SET_PRICE_BREAKDOWN, payload: priceResult });

    return priceResult;
  }
}
