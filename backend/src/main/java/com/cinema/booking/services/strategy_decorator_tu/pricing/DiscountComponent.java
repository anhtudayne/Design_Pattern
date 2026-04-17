package com.cinema.booking.services.strategy_decorator_tu.pricing;

import java.math.BigDecimal;

/**
 * Component interface cho Decorator Pattern (giảm giá).
 * Mỗi Decorator sẽ implement interface này để "bọc" thêm logic giảm giá.
 */
public interface DiscountComponent {
    DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context);
}
