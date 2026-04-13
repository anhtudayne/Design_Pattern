package com.cinema.booking.patterns.pricing.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handler — đảm bảo giá cuối không âm.
 * Phải chạy đầu tiên trong chain: nếu finalTotal âm thì
 * MaxDiscountHandler sẽ tính sai tỷ lệ chia.
 */
@Component
public class MinPriceHandler extends AbstractPriceValidationHandler {

    @Override
    protected void doValidate(PriceValidationContext ctx) {
        if (ctx.getFinalTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Giá cuối cùng không thể âm: " + ctx.getFinalTotal());
        }
    }
}
