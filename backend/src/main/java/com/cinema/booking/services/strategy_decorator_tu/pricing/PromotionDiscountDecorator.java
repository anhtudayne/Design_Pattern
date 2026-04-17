package com.cinema.booking.services.strategy_decorator_tu.pricing;

import com.cinema.booking.entities.Promotion;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete Decorator — Áp dụng mã khuyến mãi từ bảng promotions.
 * Hỗ trợ 2 loại giảm giá:
 *   - PERCENT: giảm theo % (VD: 10% tổng hóa đơn)
 *   - FIXED: giảm số tiền cố định (VD: giảm 50.000đ)
 * Discount không vượt quá subtotal.
 */
public class PromotionDiscountDecorator extends BaseDiscountDecorator {

    private final Promotion promotion;

    public PromotionDiscountDecorator(DiscountComponent wrapped, Promotion promotion) {
        super(wrapped);
        this.promotion = promotion;
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        // Lấy kết quả từ chain trước đó
        DiscountResult previousResult = super.applyDiscount(subtotal, context);
        BigDecimal previousDiscount = previousResult.getTotalDiscount();

        // Tính discount của promotion này
        BigDecimal promotionDiscount;
        String description;

        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENT) {
            promotionDiscount = subtotal.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
            description = previousResult.getDescription() + " | Khuyến mãi " 
                    + promotion.getCode() + ": -" + promotion.getDiscountValue() + "%";
        } else {
            // FIXED
            promotionDiscount = promotion.getDiscountValue();
            description = previousResult.getDescription() + " | Khuyến mãi " 
                    + promotion.getCode() + ": -" + promotion.getDiscountValue() + "đ";
        }

        // Tổng discount = trước + hiện tại, nhưng không được vượt subtotal
        BigDecimal totalDiscount = previousDiscount.add(promotionDiscount);
        if (totalDiscount.compareTo(subtotal) > 0) {
            totalDiscount = subtotal;
        }

        return DiscountResult.builder()
                .totalDiscount(totalDiscount)
                .description(description)
                .build();
    }
}
