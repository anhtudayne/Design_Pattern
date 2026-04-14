package com.cinema.booking.patterns.mediator;

import com.cinema.booking.services.PromotionInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionInventoryRollback implements PaymentColleague {

    private final PromotionInventoryService promotionInventoryService;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        // Quantity already reserved when checkout request was created.
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        promotionInventoryService.releasePromotionForBooking(context.getBooking().getBookingId());
    }
}
