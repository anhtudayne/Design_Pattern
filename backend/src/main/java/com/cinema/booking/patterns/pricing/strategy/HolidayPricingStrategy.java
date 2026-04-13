package com.cinema.booking.patterns.pricing.strategy;

import com.cinema.booking.patterns.pricing.context.PricingContext;
import com.cinema.booking.patterns.specification.PricingConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy áp dụng phụ thu ngày lễ.
 * priority=20: ưu tiên cao hơn Weekend (30), thấp hơn EarlyBird (10).
 */
@Component
public class HolidayPricingStrategy implements PricingStrategy {

    @Value("${cinema.pricing.holiday-surcharge-pct:20}")
    private BigDecimal holidaySurchargePct;

    @Override
    public BigDecimal adjustBasePrice(BigDecimal basePrice, PricingContext ctx) {
        return basePrice.multiply(
                BigDecimal.ONE.add(holidaySurchargePct.divide(new BigDecimal("100")))
        );
    }

    @Override
    public boolean isApplicable(PricingContext ctx) {
        return PricingConditions.isHoliday().test(ctx);
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public String name() {
        return "HOLIDAY";
    }
}
