package com.cinema.booking.pattern.decorator;

import com.cinema.booking.entity.Customer;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Giảm giá theo hạng thành viên, bọc ngoài chuỗi giảm giá bên trong.
 * Phần giảm = {@code membershipDiscountPercent}% của {@code subtotal} (vé + F&amp;B + phụ thu), làm tròn xuống;
 * tổng giảm không vượt {@code subtotal}.
 */
public class MemberDiscountDecorator extends BaseDiscountDecorator {

    public MemberDiscountDecorator(DiscountComponent wrapped) {
        super(wrapped);
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        DiscountResult inner = super.applyDiscount(subtotal, context);

        if (context.getCustomer() == null) {
            return inner;
        }
        Customer customer = context.getCustomer();
        BigDecimal discountPct = customer.getMembershipDiscountPercent();
        if (discountPct == null || discountPct.compareTo(BigDecimal.ZERO) <= 0) {
            return inner;
        }

        BigDecimal memberDiscount = subtotal
                .multiply(discountPct)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);

        BigDecimal newMemberTotal = inner.getMembershipDiscount().add(memberDiscount);
        BigDecimal newTotal = inner.getTotalDiscount().add(memberDiscount);

        if (newTotal.compareTo(subtotal) > 0) {
            BigDecimal overflow = newTotal.subtract(subtotal);
            newMemberTotal = newMemberTotal.subtract(overflow);
            newTotal = subtotal;
        }

        String memberDesc = "Hạng thành viên: -" + discountPct.stripTrailingZeros().toPlainString() + "%";
        String description = "Không có giảm giá".equals(inner.getDescription())
                ? memberDesc
                : inner.getDescription() + " | " + memberDesc;

        return DiscountResult.builder()
                .totalDiscount(newTotal)
                .promotionDiscount(inner.getPromotionDiscount())
                .membershipDiscount(newMemberTotal)
                .description(description)
                .build();
    }
}
