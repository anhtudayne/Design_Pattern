package com.cinema.booking.dtos;

import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Payment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResult {
    private Booking booking;
    private Payment payment;
    private PriceBreakdownDTO price;
    private Object paymentResult; // payUrl (String) hoặc entity Payment tuỳ strategy
}
