package com.cinema.booking.patterns.pricing.strategy;

import com.cinema.booking.patterns.pricing.context.PricingContext;
import com.cinema.booking.patterns.specification.PricingConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy áp dụng phụ thu cuối tuần.
 * priority=30: ưu tiên thấp hơn EarlyBird (10) và Holiday (20).
 */
@Component
public class WeekendPricingStrategy implements PricingStrategy {

    @Value("${cinema.pricing.weekend-surcharge-pct:15}")
    private BigDecimal weekendSurchargePct;

    @Override
    public BigDecimal adjustBasePrice(BigDecimal basePrice, PricingContext ctx) {
        return basePrice.multiply(
                BigDecimal.ONE.add(weekendSurchargePct.divide(new BigDecimal("100")))
        );
    }

    @Override
    public boolean isApplicable(PricingContext ctx) {
        return PricingConditions.isWeekend().test(ctx);
    }

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public String name() {
        return "WEEKEND";
    }
}
