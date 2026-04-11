package com.cinema.booking.patterns.chainofresponsibility;

public abstract class AbstractCheckoutValidationHandler implements CheckoutValidationHandler {
    private CheckoutValidationHandler next;

    @Override
    public void setNext(CheckoutValidationHandler next) {
        this.next = next;
    }

    @Override
    public void handle(CheckoutValidationContext context) {
        doHandle(context);
        if (next != null) {
            next.handle(context);
        }
    }

    protected abstract void doHandle(CheckoutValidationContext context);
}
