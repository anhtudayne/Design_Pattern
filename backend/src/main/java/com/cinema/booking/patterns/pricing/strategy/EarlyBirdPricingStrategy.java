package com.cinema.booking.patterns.pricing.strategy;

import com.cinema.booking.patterns.pricing.context.PricingContext;
import com.cinema.booking.patterns.specification.PricingConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Strategy áp dụng giảm giá đặt vé sớm (Early Bird).
 * priority=10: ưu tiên cao nhất trong 4 strategy tiêu chuẩn.
 */
@Component
public class EarlyBirdPricingStrategy implements PricingStrategy {

    @Value("${cinema.pricing.early-bird-discount-pct:10}")
    private BigDecimal earlyBirdDiscountPct;

    @Override
    public BigDecimal adjustBasePrice(BigDecimal basePrice, PricingContext ctx) {
        return basePrice.multiply(
                BigDecimal.ONE.subtract(earlyBirdDiscountPct.divide(new BigDecimal("100")))
        );
    }

    @Override
    public boolean isApplicable(PricingContext ctx) {
        return PricingConditions.isEarlyBird().test(ctx);
    }

    @Override
    public int priority() {
        return 10; // ưu tiên cao nhất trong 4 strategy tiêu chuẩn
    }

    @Override
    public String name() {
        return "EARLY_BIRD";
    }
}
