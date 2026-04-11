import { BOOKING_ACTIONS } from '../bookingActionTypes';

/**
 * Command Pattern — SelectSeatsCommand
 * Đóng gói hành động chọn ghế kèm validation logic.
 * 
 * @param {Array} seats - Danh sách ghế được chọn
 */
export class SelectSeatsCommand {
  constructor(seats) {
    this.seats = seats;
  }

  execute(dispatch, getState) {
    const state = getState();

    // Validation: phải có showtime trước khi chọn ghế
    if (!state.selectedShowtime) {
      throw new Error('Vui lòng chọn suất chiếu trước khi chọn ghế.');
    }

    // Validation: phải chọn ít nhất 1 ghế
    if (!this.seats || this.seats.length === 0) {
      throw new Error('Vui lòng chọn ít nhất 1 ghế.');
    }

    dispatch({ type: BOOKING_ACTIONS.SELECT_SEATS, payload: this.seats });
  }
}
