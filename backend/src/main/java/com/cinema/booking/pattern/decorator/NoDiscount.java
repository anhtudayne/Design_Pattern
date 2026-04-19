package com.cinema.booking.pattern.decorator;

import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import java.math.BigDecimal;

/** Bước gốc của chuỗi: không giảm, trả {@link DiscountResult#none()}. */
public class NoDiscount implements DiscountComponent {

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        return DiscountResult.none();
    }
}
