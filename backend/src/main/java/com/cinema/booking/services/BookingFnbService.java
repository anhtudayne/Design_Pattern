package com.cinema.booking.services;

import com.cinema.booking.dtos.BookingFnbCreateDTO;
import com.cinema.booking.entities.FnBLine;
import java.util.List;

public interface BookingFnbService {
    List<FnBLine> getAllBookingFnbItems();
    List<FnBLine> getBookingFnbItemsByBookingId(Integer bookingId);
    List<FnBLine> createBookingFnbItems(BookingFnbCreateDTO createDTO);
    void deleteBookingFnbItemsByBookingId(Integer bookingId);
}
