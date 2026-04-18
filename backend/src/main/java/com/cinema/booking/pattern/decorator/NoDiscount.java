package com.cinema.booking.pattern.decorator;
import com.cinema.booking.pattern.decorator.DiscountResult;
import com.cinema.booking.pattern.decorator.NoDiscount;
import com.cinema.booking.pattern.decorator.DiscountComponent;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import java.math.BigDecimal;

/**
 * Concrete Component — Base case cho decorator chain.
 * Trả về discount = 0 (không giảm giá gì cả).
 * Đây là nút gốc (leaf) mà mọi decorator chain đều bắt đầu từ đó.
 */
public class NoDiscount implements DiscountComponent {

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        return DiscountResult.none();
    }
}
