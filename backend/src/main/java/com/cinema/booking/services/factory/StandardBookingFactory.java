package com.cinema.booking.services.factory;

import com.cinema.booking.entities.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class StandardBookingFactory implements BookingFactory {

    @Override
    public Booking createPendingBooking(User user, Promotion promotion) {
        return createBooking(user, promotion, Booking.BookingStatus.PENDING);
    }

    @Override
    public Booking createBooking(User user, Promotion promotion, Booking.BookingStatus status) {
        return Booking.builder()
                .user(user)
                .promotion(promotion)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public Ticket createTicket(Booking booking, Seat seat, Showtime showtime, Movie movie, BigDecimal unitPrice) {
        return Ticket.builder()
                .booking(booking)
                .seat(seat)
                .showtime(showtime)
                .movie(movie)
                .unitPrice(unitPrice)
                .build();
    }

    @Override
    public FnBLine createFnbLine(Booking booking, FnbItem fnbItem, Integer quantity) {
        return FnBLine.builder()
                .booking(booking)
                .fnbItem(fnbItem)
                .quantity(quantity)
                .build();
    }

    @Override
    public Payment createPayment(Booking booking, String method, BigDecimal amount, Payment.PaymentStatus status) {
        return Payment.builder()
                .booking(booking)
                .paymentMethod(method)
                .amount(amount)
                .status(status)
                .paidAt(status == Payment.PaymentStatus.SUCCESS ? LocalDateTime.now() : null)
                .build();
    }
}
