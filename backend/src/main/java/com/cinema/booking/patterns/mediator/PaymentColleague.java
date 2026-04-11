package com.cinema.booking.patterns.mediator;

public interface PaymentColleague {
    void onPaymentSuccess(MomoCallbackContext context);
    void onPaymentFailure(MomoCallbackContext context);
}
