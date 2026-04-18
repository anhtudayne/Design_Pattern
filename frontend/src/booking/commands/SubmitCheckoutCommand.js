import { createBooking } from '../../services/bookingService';

/**
 * Command Pattern — Submit checkout: tạo phiên thanh toán online (MoMo) và trả payUrl.
 */
export class SubmitCheckoutCommand {
  constructor(userId) {
    this.userId = userId;
  }

  async execute(dispatch, getState) {
    const state = getState();

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

    const payUrl = await createBooking(payload);
    return { type: 'momo', payUrl };
  }
}
