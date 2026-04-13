package com.cinema.booking.patterns.pricing.validation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Handler — phát hiện giá bất thường (giá/ghế quá thấp, có thể do lỗi tính toán).
 * Guard: nếu seatCount = 0 thì bỏ qua.
 *
 * TODO nâng cấp sau: check velocity (quá nhiều đơn cùng lúc), bất thường so với lịch sử.
 */
@Component
public class FraudDetectionHandler extends AbstractPriceValidationHandler {

    static final BigDecimal MIN_PRICE_PER_SEAT = new BigDecimal("10000"); // 10,000 VND

    @Override
    protected void doValidate(PriceValidationContext ctx) {
        if (ctx.getSeatCount() <= 0) return;

        BigDecimal pricePerSeat = ctx.getFinalTotal()
                .divide(BigDecimal.valueOf(ctx.getSeatCount()), 0, RoundingMode.FLOOR);

        if (pricePerSeat.compareTo(MIN_PRICE_PER_SEAT) < 0) {
            throw new IllegalStateException(
                    "Giá vé bất thường: " + pricePerSeat + " VND/ghế. " +
                    "Giá tối thiểu mỗi ghế là " + MIN_PRICE_PER_SEAT + " VND.");
        }
    }
}
