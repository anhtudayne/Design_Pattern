package com.cinema.booking.pattern.decorator;
import com.cinema.booking.pattern.decorator.DiscountResult;
import com.cinema.booking.pattern.decorator.DiscountComponent;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import java.math.BigDecimal;

/**
 * Component interface cho Decorator Pattern (giảm giá).
 * Mỗi Decorator sẽ implement interface này để "bọc" thêm logic giảm giá.
 */
public interface DiscountComponent {
    DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context);
}
