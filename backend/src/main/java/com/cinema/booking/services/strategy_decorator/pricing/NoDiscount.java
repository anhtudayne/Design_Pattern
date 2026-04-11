package com.cinema.booking.services.strategy_decorator.pricing;

import java.math.BigDecimal;

public class NoDiscount implements DiscountComponent {
    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        return new DiscountResult(BigDecimal.ZERO);
    }
}
