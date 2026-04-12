import { createContext, useContext, useReducer, useCallback, useEffect, useRef } from 'react';
import { bookingReducer, defaultBookingState } from '../booking/bookingReducer';
import { BOOKING_ACTIONS } from '../booking/bookingActionTypes';

const BookingContext = createContext(null);

const STORAGE_KEY = 'starcine_booking';

// ── Helper: read/write sessionStorage ────────────────────────────────
const loadBooking = () => {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (_e) { return null; }
};

const saveBooking = (state) => {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch (_e) { /* ignore */ }
};

// ── Provider ─────────────────────────────────────────────────────────
// eslint-disable-next-line react-refresh/only-export-components
export function useBooking() {
  const ctx = useContext(BookingContext);
  if (!ctx) throw new Error('useBooking must be used within a BookingProvider');
  return ctx;
}

export function BookingProvider({ children }) {
  // ═══════════════════════════════════════════════════════════════════
  //  Reducer Pattern: Thay thế useState bằng useReducer
  //  Tất cả state transitions đi qua bookingReducer duy nhất
  // ═══════════════════════════════════════════════════════════════════
  const [booking, dispatch] = useReducer(
    bookingReducer,
    defaultBookingState,
    // Initializer: load từ sessionStorage nếu có
    (initial) => loadBooking() || initial
  );

  // Ref để expose getState cho Command objects
  const bookingRef = useRef(booking);

  // Cập nhật ref trong useEffect để tránh lỗi "Cannot update ref during render"
  useEffect(() => {
    bookingRef.current = booking;
  }, [booking]);

  const getState = useCallback(() => bookingRef.current, []);

  // Persist to sessionStorage whenever state changes
  useEffect(() => {
    saveBooking(booking);
  }, [booking]);

  // ═══════════════════════════════════════════════════════════════════
  //  Command Pattern: executeCommand chạy command objects
  //  Mỗi Command đóng gói logic + validation, gọi dispatch bên trong
  // ═══════════════════════════════════════════════════════════════════
  const executeCommand = useCallback(async (command) => {
    return await command.execute(dispatch, getState);
  }, [getState]);

  // ── Legacy setter wrappers (backward-compatible) ───────────────
  // Các setter này wrap dispatch calls để giữ API cũ cho các page hiện tại.
  // Khi page được refactor sang Command pattern, có thể xoá dần.
  const setMovie = useCallback((movie) => {
    dispatch({ type: BOOKING_ACTIONS.SELECT_MOVIE, payload: movie });
  }, []);

  const setCinema = useCallback((cinema) => {
    dispatch({ type: BOOKING_ACTIONS.SELECT_CINEMA, payload: cinema });
  }, []);

  const setShowtime = useCallback((showtime) => {
    dispatch({ type: BOOKING_ACTIONS.SELECT_SHOWTIME, payload: showtime });
  }, []);

  const setSeats = useCallback((seats) => {
    dispatch({ type: BOOKING_ACTIONS.SELECT_SEATS, payload: seats });
  }, []);

  const setFnbs = useCallback((fnbs) => {
    dispatch({ type: BOOKING_ACTIONS.SET_FNBS, payload: fnbs });
  }, []);

  const setPriceBreakdown = useCallback((breakdown) => {
    dispatch({ type: BOOKING_ACTIONS.SET_PRICE_BREAKDOWN, payload: breakdown });
  }, []);

  const setVoucherCode = useCallback((code) => {
    dispatch({ type: BOOKING_ACTIONS.SET_VOUCHER_CODE, payload: code });
  }, []);

  /** Set movie + cinema + showtime in one shot (from Quick Booking / MovieList) */
  const setBookingSelection = useCallback(({ movie, cinema, showtime }) => {
    dispatch({ type: BOOKING_ACTIONS.SET_BOOKING_SELECTION, payload: { movie, cinema, showtime } });
  }, []);

  /** Reset entire booking flow (after payment or cancel) */
  const resetBooking = useCallback(() => {
    dispatch({ type: BOOKING_ACTIONS.RESET });
    sessionStorage.removeItem(STORAGE_KEY);
  }, []);

  const value = {
    // Legacy support (backward-compatible)
    ...booking,

    // The preferred way for components
    bookingSelection: {
      movie: booking.selectedMovie,
      cinema: booking.selectedCinema,
      showtime: booking.selectedShowtime,
      selectedSeats: booking.selectedSeats,
      selectedSnacks: booking.selectedFnbs, // alias
      priceBreakdown: booking.priceBreakdown,
      voucherCode: booking.voucherCode,
    },

    // ─── Reducer Pattern: raw dispatch + state getter ──────────
    dispatch,
    getState,

    // ─── Command Pattern: execute any BookingCommand ───────────
    executeCommand,

    // ─── Legacy setters (backward-compatible) ─────────────────
    setBookingMovie: setMovie,
    setBookingCinema: setCinema,
    setBookingShowtime: setShowtime,
    setBookingSeats: setSeats,
    setBookingSnacks: setFnbs,
    setPriceBreakdown,
    setVoucherCode,
    setBookingSelection,
    resetBooking,
  };

  return (
    <BookingContext.Provider value={value}>
      {children}
    </BookingContext.Provider>
  );
}

export default BookingContext;
