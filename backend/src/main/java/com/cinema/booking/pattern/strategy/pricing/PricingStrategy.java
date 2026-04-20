package com.cinema.booking.pattern.strategy.pricing;
import java.math.BigDecimal;

public interface PricingStrategy {
    PricingLineType lineType();
    BigDecimal calculate(PricingContext context);
}
