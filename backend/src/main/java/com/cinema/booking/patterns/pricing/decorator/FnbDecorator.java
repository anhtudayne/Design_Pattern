package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.patterns.pricing.context.PricingContext;

/**
 * Decorator — điền fnbTotal vào accumulator.
 * Giá trị này đã được PricingContextFactory tính sẵn (sum fnbItem.price * qty).
 */
public class FnbDecorator extends AbstractPriceCalculatorDecorator {

    public FnbDecorator(PriceCalculator inner) {
        super(inner);
    }

    @Override
    protected void decorate(PricingAccumulator acc, PricingContext ctx) {
        acc.setFnbTotal(ctx.getFnbTotal());
    }
}
