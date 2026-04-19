package com.cinema.booking.pattern.decorator;

import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import java.math.BigDecimal;

/**
 * Một bước trong chuỗi giảm giá: nhận subtotal trước giảm và ngữ cảnh, trả {@link DiscountResult}.
 */
public interface DiscountComponent {

    /**
     * @param subtotal tổng vé + F&amp;B + phụ thu trước khi giảm giá
     */
    DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context);
}
