package com.cinema.booking.pattern.chain;
import com.cinema.booking.pattern.chain.AbstractPricingValidationHandler;
import com.cinema.booking.pattern.chain.PricingValidationContext;
import com.cinema.booking.pattern.chain.PricingValidationHandler;

/**
 * Abstract base — delegate đến next handler sau khi doValidate() hoàn thành.
 */
public abstract class AbstractPricingValidationHandler implements PricingValidationHandler {

    private PricingValidationHandler next;

    @Override
    public void setNext(PricingValidationHandler next) {
        this.next = next;
    }

    @Override
    public void validate(PricingValidationContext context) {
        doValidate(context);
        if (next != null) {
            next.validate(context);
        }
    }

    protected abstract void doValidate(PricingValidationContext context);
}
