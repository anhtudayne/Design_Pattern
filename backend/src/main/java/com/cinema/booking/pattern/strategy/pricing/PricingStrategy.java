package com.cinema.booking.pattern.strategy.pricing;

import java.math.BigDecimal;

/**
 * Strategy interface cho việc tính giá.
 * Mỗi concrete strategy triển khai cách tính giá cho một loại sản phẩm cụ thể.
 */
public interface PricingStrategy {
    PricingLineType lineType();
    BigDecimal calculate(PricingContext context);
}
