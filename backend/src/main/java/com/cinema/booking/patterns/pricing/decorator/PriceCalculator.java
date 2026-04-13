package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.patterns.pricing.context.PricingContext;

/**
 * Component interface của Decorator pattern.
 *
 * <ul>
 *   <li>Concrete Component: {@link BasePriceCalculator} — tạo {@link PricingAccumulator} ban đầu</li>
 *   <li>Abstract Decorator: {@link AbstractPriceCalculatorDecorator} — wrap inner và thêm khoản</li>
 * </ul>
 */
public interface PriceCalculator {

    /**
     * Tính giá và trả về accumulator với các trường đã được điền đủ.
     * Concrete Component tạo Accumulator ban đầu;
     * mỗi Decorator đọc inner result rồi thêm khoản của mình.
     */
    PricingAccumulator calculate(PricingContext ctx);
}
