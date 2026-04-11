package com.cinema.booking.services.strategy_decorator.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculate(PricingContext context);
}
