package com.cinema.booking.pattern.strategy.pricing;
import com.cinema.booking.pattern.strategy.pricing.PricingStrategy;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import java.math.BigDecimal;

/**
 * Strategy interface cho việc tính giá.
 * Mỗi concrete strategy triển khai cách tính giá cho một loại sản phẩm cụ thể.
 */
public interface PricingStrategy {
    BigDecimal calculate(PricingContext context);
}
