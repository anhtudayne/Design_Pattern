package com.cinema.booking.patterns.chainofresponsibility;

import org.springframework.stereotype.Component;

@Component
public class MaxSeatsHandler extends AbstractCheckoutValidationHandler {

    private static final int MAX_SEATS_PER_BOOKING = 8;

    @Override
    protected void doHandle(CheckoutValidationContext context) {
        if (context.getSeatIds() == null || context.getSeatIds().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất 1 ghế.");
        }
        if (context.getSeatIds().size() > MAX_SEATS_PER_BOOKING) {
            throw new RuntimeException("Không được đặt quá " + MAX_SEATS_PER_BOOKING + " ghế trong một lần.");
        }
    }
}
