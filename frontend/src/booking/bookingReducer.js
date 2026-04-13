import { BOOKING_ACTIONS } from './bookingActionTypes';

/**
 * Default state cho booking flow.
 * Mô tả cấu trúc dữ liệu của từng bước trong quy trình đặt vé.
 */
export const defaultBookingState = {
  selectedMovie: null,      // { movieId, title, posterUrl, ageRating, durationMinutes }
  selectedCinema: null,     // { cinemaId, name, address }
  selectedShowtime: null,   // { showtimeId, startTime, endTime, roomId, roomName, basePrice, surcharge, screenType }
  selectedSeats: [],        // [{ seatId, seatRow, seatNumber, seatType, seatTypeSurcharge, totalPrice }]
  selectedFnbs: [],         // [{ itemId, name, unitPrice, imageUrl, quantity }]
  priceBreakdown: null,     // { ticketTotal, fnbTotal, discountAmount, finalTotal }
  voucherCode: '',
};

/**
 * Booking Reducer — Reducer Pattern.
 * Cập nhật state booking theo action type, đảm bảo mỗi transition đều có ý nghĩa.
 * 
 * @param {Object} state - Current booking state
 * @param {Object} action - { type: BOOKING_ACTIONS.*, payload: any }
 * @returns {Object} New state
 */
export function bookingReducer(state, action) {
  switch (action.type) {
    case BOOKING_ACTIONS.SELECT_MOVIE:
      return { ...state, selectedMovie: action.payload };

    case BOOKING_ACTIONS.SELECT_CINEMA:
      return { ...state, selectedCinema: action.payload };

    case BOOKING_ACTIONS.SELECT_SHOWTIME:
      return { ...state, selectedShowtime: action.payload };

    case BOOKING_ACTIONS.SELECT_SEATS:
      return { ...state, selectedSeats: action.payload };

    case BOOKING_ACTIONS.SET_FNBS:
      return { ...state, selectedFnbs: action.payload };

    case BOOKING_ACTIONS.SET_PRICE_BREAKDOWN:
      return { ...state, priceBreakdown: action.payload };

    case BOOKING_ACTIONS.SET_VOUCHER_CODE:
      return { ...state, voucherCode: action.payload };

    case BOOKING_ACTIONS.SET_BOOKING_SELECTION:
      // Cho phép set movie + cinema + showtime cùng lúc (Quick Booking)
      return {
        ...state,
        selectedMovie: action.payload.movie || state.selectedMovie,
        selectedCinema: action.payload.cinema || state.selectedCinema,
        selectedShowtime: action.payload.showtime || state.selectedShowtime,
      };

    case BOOKING_ACTIONS.RESET:
      return { ...defaultBookingState };

    default:
      return state;
  }
}
