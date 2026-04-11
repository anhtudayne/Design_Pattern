package com.cinema.booking.services.strategy_decorator.pricing;

import java.math.BigDecimal;

public interface DiscountComponent {
    DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context);
}
