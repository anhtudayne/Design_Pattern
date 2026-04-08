package com.cinema.booking.services;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.dtos.SeatStatusDTO;
import java.util.List;

public interface BookingService {
    List<SeatStatusDTO> getSeatStatuses(Integer showtimeId);
    boolean lockSeat(Integer showtimeId, Integer seatId, Integer userId);
    void unlockSeat(Integer showtimeId, Integer seatId);
    PriceBreakdownDTO calculatePrice(BookingCalculationDTO request);
}
