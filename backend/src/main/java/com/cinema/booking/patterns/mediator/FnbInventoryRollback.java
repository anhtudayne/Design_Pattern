package com.cinema.booking.patterns.mediator;

import com.cinema.booking.services.FnbItemInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FnbInventoryRollback implements PaymentColleague {

    private final FnbItemInventoryService fnbItemInventoryService;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        // Already reserved during checkout request.
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        fnbItemInventoryService.releaseItemsForBooking(context.getBooking().getBookingId());
    }
}
