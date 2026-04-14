package com.cinema.booking.services.strategy_decorator.pricing;

import java.math.BigDecimal;

public interface PricingStrategy {

    PricingLineType lineType();

    BigDecimal calculate(PricingContext context);
}
