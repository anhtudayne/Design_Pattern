package com.cinema.booking.service;

import com.cinema.booking.dto.request.BookingFnbCreateDTO;
import com.cinema.booking.entity.FnBLine;
import java.util.List;

public interface BookingFnbService {
    List<FnBLine> getAllBookingFnbItems();
    List<FnBLine> getBookingFnbItemsByBookingId(Integer bookingId);
    List<FnBLine> createBookingFnbItems(BookingFnbCreateDTO createDTO);
    void deleteBookingFnbItemsByBookingId(Integer bookingId);
}
