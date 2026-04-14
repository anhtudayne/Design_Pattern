package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.entities.Customer;
import com.cinema.booking.entities.MembershipTier;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decorator áp dụng chiết khấu thành viên dựa trên {@link MembershipTier#getDiscountPercent()}.
 *
 * <p>Chỉ áp dụng khi:
 * <ul>
 *   <li>{@code context.getCustomer()} != null</li>
 *   <li>Customer có {@link MembershipTier} với discountPercent > 0</li>
 * </ul>
 *
 * <p>Discount được tính trên phần còn lại sau các discount đã áp trước đó
 * (ví dụ promotion nếu có).
 */
public class MemberDiscountDecorator extends BaseDiscountDecorator {

    public MemberDiscountDecorator(DiscountComponent wrapped) {
        super(wrapped);
    }

    @Override
    public DiscountResult applyDiscount(BigDecimal subtotal, PricingContext context) {
        DiscountResult previousResult = super.applyDiscount(subtotal, context);
        BigDecimal currentDiscount = previousResult.getTotalDiscount();
        BigDecimal promotionDiscount = previousResult.getPromotionDiscount();
        BigDecimal membershipDiscount = previousResult.getMembershipDiscount();

        Customer customer = context.getCustomer();
        if (customer != null) {
            MembershipTier tier = customer.getTier();
            if (tier != null && tier.getDiscountPercent() != null
                    && tier.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remaining = subtotal.subtract(currentDiscount);
                if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                    remaining = BigDecimal.ZERO;
                }
                BigDecimal memberDiscount = remaining
                        .multiply(tier.getDiscountPercent())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                currentDiscount = currentDiscount.add(memberDiscount);
                membershipDiscount = membershipDiscount.add(memberDiscount);
            }
        }

        return new DiscountResult(currentDiscount, promotionDiscount, membershipDiscount);
    }
}
