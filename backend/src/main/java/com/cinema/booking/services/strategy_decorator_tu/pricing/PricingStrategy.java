package com.cinema.booking.services.strategy_decorator_tu.pricing;

import java.math.BigDecimal;

/**
 * Strategy interface cho việc tính giá.
 * Mỗi concrete strategy triển khai cách tính giá cho một loại sản phẩm cụ thể.
 */
public interface PricingStrategy {
    BigDecimal calculate(PricingContext context);
}
