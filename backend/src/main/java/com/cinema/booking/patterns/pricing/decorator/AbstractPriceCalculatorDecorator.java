package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.patterns.pricing.context.PricingContext;

/**
 * Abstract Decorator — implement PriceCalculator và wrap một PriceCalculator inner.
 * Template method: gọi inner trước → sau đó gọi decorate() để thêm khoản của mình.
 *
 * <p>Concrete Decorator chỉ cần implement {@link #decorate(PricingAccumulator, PricingContext)}
 * (3-5 dòng) — không cần lặp lại code gọi inner.</p>
 */
public abstract class AbstractPriceCalculatorDecorator implements PriceCalculator {

    protected final PriceCalculator inner;

    protected AbstractPriceCalculatorDecorator(PriceCalculator inner) {
        this.inner = inner;
    }

    @Override
    public final PricingAccumulator calculate(PricingContext ctx) {
        PricingAccumulator acc = inner.calculate(ctx); // gọi inner trước
        decorate(acc, ctx);                            // thêm khoản của mình
        return acc;
    }

    /**
     * Mỗi Concrete Decorator chỉ implement method này — set đúng 1 field trong acc.
     * KHÔNG được thay đổi {@code ticketTotal} — chỉ BasePriceCalculator set field đó.
     */
    protected abstract void decorate(PricingAccumulator acc, PricingContext ctx);
}
