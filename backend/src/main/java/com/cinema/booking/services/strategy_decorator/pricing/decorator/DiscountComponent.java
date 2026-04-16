package com.cinema.booking.services.strategy_decorator.pricing.decorator;

import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;

import java.math.BigDecimal;

public interface DiscountComponent {
    DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context);
}
