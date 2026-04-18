package com.cinema.booking.service;

import com.cinema.booking.dto.BookingCalculationDTO;
import com.cinema.booking.dto.BookingDTO;
import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.dto.SeatStatusDTO;
import java.util.List;

public interface BookingService {
    List<SeatStatusDTO> getSeatStatuses(Integer showtimeId);
    boolean lockSeat(Integer showtimeId, Integer seatId, Integer userId);
    void unlockSeat(Integer showtimeId, Integer seatId);
    PriceBreakdownDTO calculatePrice(BookingCalculationDTO request);
    BookingDTO getBookingDetail(Integer bookingId);
    List<BookingDTO> searchBookings(String query);
    
    // State Pattern Operations
    void cancelBooking(Integer bookingId);
    void refundBooking(Integer bookingId);
    void printTickets(Integer bookingId);
}
