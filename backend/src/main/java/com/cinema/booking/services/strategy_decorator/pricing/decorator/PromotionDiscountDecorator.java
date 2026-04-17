package com.cinema.booking.services.strategy_decorator.pricing.decorator;

import com.cinema.booking.entities.Promotion;
import com.cinema.booking.services.strategy_decorator.pricing.core.PricingContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decorator áp dụng chiết khấu promotion (PERCENT hoặc FIXED).
 *
 * <p>Validation (validTo, quantity) đã được thực hiện tại tầng service
 * trước khi build PricingContext. Decorator chỉ thuần tính discount.
 */
public class PromotionDiscountDecorator extends BaseDiscountDecorator {

    public PromotionDiscountDecorator(DiscountComponent wrapped) {
        super(wrapped);
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        DiscountResult previousResult = super.applyDiscount(subtotal, context);
        BigDecimal currentDiscount = previousResult.getTotalDiscount();
        BigDecimal promotionDiscount = previousResult.getPromotionDiscount();
        BigDecimal membershipDiscount = previousResult.getMembershipDiscount();

        Promotion promotion = context.getPromotion();
        if (promotion != null) {
            BigDecimal additionalDiscount;
            if (promotion.getDiscountType() == Promotion.DiscountType.PERCENT) {
                additionalDiscount = subtotal
                        .multiply(promotion.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                additionalDiscount = promotion.getDiscountValue();
            }
            BigDecimal remaining = subtotal.subtract(currentDiscount);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                remaining = BigDecimal.ZERO;
            }
            if (additionalDiscount.compareTo(remaining) > 0) {
                additionalDiscount = remaining;
            }
            currentDiscount = currentDiscount.add(additionalDiscount);
            promotionDiscount = promotionDiscount.add(additionalDiscount);
        }

        return new DiscountResult(currentDiscount, promotionDiscount, membershipDiscount);
    }
}
