package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.patterns.pricing.context.PricingContext;

import java.math.BigDecimal;

/**
 * Decorator — áp dụng giảm giá hạng thành viên (membership discount).
 * Giảm trên ticketTotal (không giảm F&B). Bỏ qua nếu khách anonymous hoặc chưa có tier.
 */
public class MemberDiscountDecorator extends AbstractPriceCalculatorDecorator {

    public MemberDiscountDecorator(PriceCalculator inner) {
        super(inner);
    }

    @Override
    protected void decorate(PricingAccumulator acc, PricingContext ctx) {
        if (ctx.getCustomer() == null || ctx.getCustomer().getTier() == null) return;

        BigDecimal discountPct = ctx.getCustomer().getTier().getDiscountPercent();
        if (discountPct == null || discountPct.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal discount = acc.getTicketTotal()
                .multiply(discountPct)
                .divide(new BigDecimal("100"));
        acc.setMembershipDiscount(discount);
    }
}
