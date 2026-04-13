package com.cinema.booking.services.factory;

import com.cinema.booking.entities.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class StandardBookingFactory implements BookingFactory {

    @Override
    public Booking createPendingBooking(Customer customer, Promotion promotion) {
        return createBooking(customer, promotion, Booking.BookingStatus.PENDING);
    }

    @Override
    public Booking createBooking(Customer customer, Promotion promotion, Booking.BookingStatus status) {
        return Booking.builder()
                .customer(customer)
                .promotion(promotion)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public Ticket createTicket(Booking booking, Seat seat, Showtime showtime, BigDecimal price) {
        return Ticket.builder()
                .booking(booking)
                .seat(seat)
                .showtime(showtime)
                .price(price)
                .build();
    }

    @Override
    public FnBLine createFnbLine(Booking booking, FnbItem item, Integer quantity, BigDecimal unitPrice) {
        return FnBLine.builder()
                .booking(booking)
                .item(item)
                .quantity(quantity)
                .unitPrice(unitPrice)
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
