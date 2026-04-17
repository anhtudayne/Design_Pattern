package com.cinema.booking.services.strategy_decorator.pricing.decorator;

import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Base của decorator chain — không áp discount. */
@Component
public class NoDiscount implements DiscountComponent {
    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        return new DiscountResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
