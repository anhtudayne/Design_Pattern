package com.cinema.booking.services.strategy_decorator.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Base của decorator chain — không áp discount. */
@Component
public class NoDiscount implements DiscountComponent {
    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        return new DiscountResult(BigDecimal.ZERO);
    }
}
