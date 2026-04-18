package com.cinema.booking.dto.response;

import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.entity.Booking;
import com.cinema.booking.entity.Payment;
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
