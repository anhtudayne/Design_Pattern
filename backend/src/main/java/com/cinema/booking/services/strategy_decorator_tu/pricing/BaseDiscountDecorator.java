package com.cinema.booking.services.strategy_decorator_tu.pricing;

import java.math.BigDecimal;

/**
 * Abstract Decorator — Base class cho tất cả các Concrete Decorators.
 * Giữ reference đến DiscountComponent bên trong (wrapped).
 * Subclass gọi super.applyDiscount() để lấy kết quả của lớp trước,
 * rồi cộng thêm discount riêng của mình.
 */
public abstract class BaseDiscountDecorator implements DiscountComponent {

    protected final DiscountComponent wrapped;

    protected BaseDiscountDecorator(DiscountComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        // Delegate cho lớp bên trong trước
        return wrapped.applyDiscount(subtotal, context);
    }
}
