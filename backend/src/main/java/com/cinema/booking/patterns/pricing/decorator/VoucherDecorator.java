package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.entities.Promotion;
import com.cinema.booking.patterns.pricing.context.PricingContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Decorator — áp dụng giảm giá voucher/promotion.
 * Tính trên phần còn lại sau membershipDiscount.
 * Clamp: voucherDiscount không thể lớn hơn phần base còn lại (tránh âm).
 */
public class VoucherDecorator extends AbstractPriceCalculatorDecorator {

    public VoucherDecorator(PriceCalculator inner) {
        super(inner);
    }

    @Override
    protected void decorate(PricingAccumulator acc, PricingContext ctx) {
        Promotion promo = ctx.getPromotion();
        if (promo == null || promo.getValidTo() == null) return;
        if (LocalDateTime.now().isAfter(promo.getValidTo())) return;

        // Base để tính voucher = subtotal - membershipDiscount
        BigDecimal base = acc.subtotal().subtract(acc.getMembershipDiscount());

        BigDecimal discount = switch (promo.getDiscountType()) {
            case PERCENT -> base.multiply(promo.getDiscountValue())
                               .divide(new BigDecimal("100"));
            case FIXED   -> promo.getDiscountValue();
        };

        // Clamp: voucher không được vượt quá phần còn lại sau membership
        if (discount.compareTo(base) > 0) discount = base;
        acc.setVoucherDiscount(discount);
    }
}
