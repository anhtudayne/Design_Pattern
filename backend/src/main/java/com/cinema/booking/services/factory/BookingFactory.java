package com.cinema.booking.services.factory;

import com.cinema.booking.entities.*;

import java.math.BigDecimal;

public interface BookingFactory {
    Booking createPendingBooking(Customer customer, Promotion promotion);
    Booking createBooking(Customer customer, Promotion promotion, Booking.BookingStatus status);
    Ticket createTicket(Booking booking, Seat seat, Showtime showtime, BigDecimal price);
    FnBLine createFnbLine(Booking booking, FnbItem item, Integer quantity, BigDecimal unitPrice);
    Payment createPayment(Booking booking, String method, BigDecimal amount, Payment.PaymentStatus status);
}
