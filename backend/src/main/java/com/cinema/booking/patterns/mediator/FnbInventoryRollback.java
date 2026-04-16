package com.cinema.booking.patterns.mediator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FnbInventoryRollback implements PaymentColleague {

    // Deprecated service logic removed

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        // Already reserved during checkout request.
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        // No-op
    }
}
