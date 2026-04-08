package com.cinema.booking.services;

import com.cinema.booking.dtos.BookingFnbCreateDTO;
import com.cinema.booking.entities.BookingFnbItem;
import java.util.List;

public interface BookingFnbService {
    List<BookingFnbItem> getAllBookingFnbItems();
    List<BookingFnbItem> getBookingFnbItemsByBookingId(Integer bookingId);
    List<BookingFnbItem> createBookingFnbItems(BookingFnbCreateDTO createDTO);
    void deleteBookingFnbItemsByBookingId(Integer bookingId);
}
