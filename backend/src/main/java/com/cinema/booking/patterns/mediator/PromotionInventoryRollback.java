package com.cinema.booking.patterns.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PromotionInventoryRollback implements PaymentColleague {

    // Dependency removed due to new inventory schema

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        // Quantity already reserved when checkout request was created.
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        // No-op. Quantity already managed differently.
    }
}
