package com.cinema.booking.services.strategy_decorator.pricing;

import java.math.BigDecimal;

/**
 * Placeholder/Obsolete Decorator.
 * Membership discount logic was removed because MembershipTier entity was deleted to match strict diagram.
 */
public class MemberDiscountDecorator extends BaseDiscountDecorator {

    public MemberDiscountDecorator(DiscountComponent wrapped) {
        super(wrapped);
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        return super.applyDiscount(subtotal, context);
    }
}
