package com.cinema.booking.services.strategy_decorator.pricing.decorator;

import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;

import java.math.BigDecimal;

public abstract class BaseDiscountDecorator implements DiscountComponent {
    protected final DiscountComponent wrapped;

    public BaseDiscountDecorator(DiscountComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        return wrapped.applyDiscount(subtotal, context);
    }
}
