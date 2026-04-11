package com.cinema.booking.patterns.chainofresponsibility;

public interface CheckoutValidationHandler {
    void setNext(CheckoutValidationHandler next);
    void handle(CheckoutValidationContext context);
}
