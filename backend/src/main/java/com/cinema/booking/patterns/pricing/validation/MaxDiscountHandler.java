package com.cinema.booking.patterns.pricing.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Handler — tổng giảm giá không được vượt quá 50% subtotal.
 * Guard: nếu subtotal = 0 thì bỏ qua (tránh chia cho 0).
 */
@Component
public class MaxDiscountHandler extends AbstractPriceValidationHandler {

    static final BigDecimal MAX_DISCOUNT_RATIO = new BigDecimal("0.50");

    @Override
    protected void doValidate(PriceValidationContext ctx) {
        if (ctx.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal ratio = ctx.getTotalDiscount()
                .divide(ctx.getSubtotal(), 4, RoundingMode.HALF_UP);

        if (ratio.compareTo(MAX_DISCOUNT_RATIO) > 0) {
            BigDecimal pct = ratio.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP);
            throw new IllegalStateException(
                    "Tổng giảm giá vượt quá 50% giá trị đơn hàng. " +
                    "Giảm thực tế: " + pct + "%");
        }
    }
}
