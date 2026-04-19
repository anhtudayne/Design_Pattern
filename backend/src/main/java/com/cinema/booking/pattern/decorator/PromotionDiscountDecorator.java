package com.cinema.booking.pattern.decorator;

import com.cinema.booking.entity.Promotion;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Áp dụng {@link Promotion}: theo % trên subtotal (làm tròn xuống) hoặc số tiền cố định.
 * Tổng giảm không vượt {@code subtotal}.
 */
public class PromotionDiscountDecorator extends BaseDiscountDecorator {

    private final Promotion promotion;

    public PromotionDiscountDecorator(DiscountComponent wrapped, Promotion promotion) {
        super(wrapped);
        this.promotion = promotion;
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        DiscountResult inner = super.applyDiscount(subtotal, context);

        BigDecimal promoDiscount;
        String promoDescription;

        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENT) {
            promoDiscount = subtotal
                    .multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
            promoDescription = "Mã " + promotion.getCode() + ": -" + promotion.getDiscountValue() + "%";
        } else {
            promoDiscount = promotion.getDiscountValue();
            promoDescription = "Mã " + promotion.getCode() + ": -" + promotion.getDiscountValue() + "đ";
        }

        BigDecimal newPromoTotal = inner.getPromotionDiscount().add(promoDiscount);
        BigDecimal newTotal = inner.getTotalDiscount().add(promoDiscount);
        if (newTotal.compareTo(subtotal) > 0) {
            BigDecimal overflow = newTotal.subtract(subtotal);
            newPromoTotal = newPromoTotal.subtract(overflow);
            newTotal = subtotal;
        }

        String description = "Không có giảm giá".equals(inner.getDescription())
                ? promoDescription
                : inner.getDescription() + " | " + promoDescription;

        return DiscountResult.builder()
                .totalDiscount(newTotal)
                .promotionDiscount(newPromoTotal)
                .membershipDiscount(inner.getMembershipDiscount())
                .description(description)
                .build();
    }
}
