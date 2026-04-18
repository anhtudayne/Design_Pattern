package com.cinema.booking.pattern.factory;
import com.cinema.booking.pattern.factory.BookingFactory;

import com.cinema.booking.entity.*;

import java.math.BigDecimal;

public interface BookingFactory {
    Booking createPendingBooking(User user, Promotion promotion);
    Booking createBooking(User user, Promotion promotion, Booking.BookingStatus status);
    Ticket createTicket(Booking booking, Seat seat, Showtime showtime, Movie movie, BigDecimal unitPrice);
    FnBLine createFnbLine(Booking booking, FnbItem fnbItem, Integer quantity);
    Payment createPayment(Booking booking, String method, BigDecimal amount, Payment.PaymentStatus status);
}
