package com.cinema.booking.patterns.pricing.strategy;

import com.cinema.booking.patterns.pricing.context.PricingContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fallback strategy — trả về basePrice không thay đổi.
 * priority=999 đảm bảo luôn là lựa chọn cuối cùng khi không có strategy nào phù hợp.
 */
@Component
public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal adjustBasePrice(BigDecimal basePrice, PricingContext ctx) {
        return basePrice;
    }

    @Override
    public boolean isApplicable(PricingContext ctx) {
        return true; // fallback luôn đúng
    }

    @Override
    public int priority() {
        return 999; // thấp nhất → fallback cuối
    }

    @Override
    public String name() {
        return "STANDARD";
    }
}
