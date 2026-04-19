package com.cinema.booking.pattern.decorator;

import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import java.math.BigDecimal;

/**
 * Ủy quyền cho {@link DiscountComponent} bọc bên trong; lớp con cộng thêm giảm giá và gộp {@link DiscountResult}.
 */
public abstract class BaseDiscountDecorator implements DiscountComponent {

    protected final DiscountComponent wrapped;

    protected BaseDiscountDecorator(DiscountComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        return wrapped.applyDiscount(subtotal, context);
    }
}
