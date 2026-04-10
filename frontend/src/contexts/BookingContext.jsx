import { createContext, useContext, useState, useCallback, useEffect } from 'react';

const BookingContext = createContext(null);

const STORAGE_KEY = 'starcine_booking';

// ── Helper: read/write sessionStorage ────────────────────────────────
const loadBooking = () => {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch { return null; }
};

const saveBooking = (state) => {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  } catch { /* ignore */ }
};

// ── Default state ────────────────────────────────────────────────────
const defaultState = {
  selectedMovie: null,      // { movieId, title, posterUrl, ageRating, durationMinutes }
  selectedCinema: null,     // { cinemaId, name, address }
  selectedShowtime: null,   // { showtimeId, startTime, endTime, roomId, roomName, basePrice, surcharge, screenType }
  selectedSeats: [],        // [{ seatId, seatRow, seatNumber, seatType, seatTypeSurcharge, totalPrice }]
  selectedFnbs: [],         // [{ itemId, name, unitPrice, imageUrl, quantity }]
  priceBreakdown: null,     // { ticketTotal, fnbTotal, discountAmount, finalTotal }
  voucherCode: '',
};

// ── Provider ─────────────────────────────────────────────────────────
export function BookingProvider({ children }) {
  const [booking, setBooking] = useState(() => loadBooking() || defaultState);

  // Persist to sessionStorage whenever state changes
  useEffect(() => {
    saveBooking(booking);
  }, [booking]);

  const setMovie = useCallback((movie) => {
    setBooking(prev => ({ ...prev, selectedMovie: movie }));
  }, []);

  const setCinema = useCallback((cinema) => {
    setBooking(prev => ({ ...prev, selectedCinema: cinema }));
  }, []);

  const setShowtime = useCallback((showtime) => {
    setBooking(prev => ({ ...prev, selectedShowtime: showtime }));
  }, []);

  const setSeats = useCallback((seats) => {
    setBooking(prev => ({ ...prev, selectedSeats: seats }));
  }, []);

  const setFnbs = useCallback((fnbs) => {
    setBooking(prev => ({ ...prev, selectedFnbs: fnbs }));
  }, []);

  const setPriceBreakdown = useCallback((breakdown) => {
    setBooking(prev => ({ ...prev, priceBreakdown: breakdown }));
  }, []);

  const setVoucherCode = useCallback((code) => {
    setBooking(prev => ({ ...prev, voucherCode: code }));
  }, []);

  /** Set movie + cinema + showtime in one shot (from Quick Booking / MovieList) */
  const setBookingSelection = useCallback(({ movie, cinema, showtime }) => {
    setBooking(prev => ({
      ...prev,
      selectedMovie: movie || prev.selectedMovie,
      selectedCinema: cinema || prev.selectedCinema,
      selectedShowtime: showtime || prev.selectedShowtime,
    }));
  }, []);

  /** Reset entire booking flow (after payment or cancel) */
  const resetBooking = useCallback(() => {
    setBooking(defaultState);
    sessionStorage.removeItem(STORAGE_KEY);
  }, []);

  const value = {
    // Legacy support (optional)
    ...booking,

    // The preferred way for my recent components
    bookingSelection: {
      movie: booking.selectedMovie,
      cinema: booking.selectedCinema,
      showtime: booking.selectedShowtime,
      selectedSeats: booking.selectedSeats,
      selectedSnacks: booking.selectedFnbs, // alias
      priceBreakdown: booking.priceBreakdown,
      voucherCode: booking.voucherCode,
    },

    // Setters
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

// ── Hook ─────────────────────────────────────────────────────────────
export function useBooking() {
  const ctx = useContext(BookingContext);
  if (!ctx) throw new Error('useBooking must be used within a BookingProvider');
  return ctx;
}

export default BookingContext;
