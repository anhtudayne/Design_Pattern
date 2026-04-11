package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.entities.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromotionDiscountDecorator extends BaseDiscountDecorator {

    public PromotionDiscountDecorator(DiscountComponent wrapped) {
        super(wrapped);
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        DiscountResult previousResult = super.applyDiscount(subtotal, context);
        BigDecimal currentDiscount = previousResult.getTotalDiscount();

        Promotion promotion = context.getPromotion();
        if (promotion != null && promotion.getValidTo() != null && LocalDateTime.now().isBefore(promotion.getValidTo())) {
            BigDecimal additionalDiscount = BigDecimal.ZERO;
            if (promotion.getDiscountType() == Promotion.DiscountType.PERCENT) {
                additionalDiscount = subtotal.multiply(promotion.getDiscountValue().divide(new BigDecimal("100")));
            } else if (promotion.getDiscountType() == Promotion.DiscountType.FIXED) {
                additionalDiscount = promotion.getDiscountValue();
            }
            currentDiscount = currentDiscount.add(additionalDiscount);
        }

        return new DiscountResult(currentDiscount);
    }
}
