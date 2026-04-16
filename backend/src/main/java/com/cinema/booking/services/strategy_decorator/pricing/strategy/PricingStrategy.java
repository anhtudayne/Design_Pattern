package com.cinema.booking.services.strategy_decorator.pricing.strategy;

import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;

import java.math.BigDecimal;

public interface PricingStrategy {

    PricingLineType lineType();

    BigDecimal calculate(PricingContext context);
}
