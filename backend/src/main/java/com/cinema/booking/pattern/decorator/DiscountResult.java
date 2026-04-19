package com.cinema.booking.pattern.decorator;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** Kết quả gộp từ chuỗi decorator; dùng để lắp {@code PriceBreakdownDTO}. */
@Getter
@Builder
public class DiscountResult {

    private final BigDecimal totalDiscount;
    private final BigDecimal promotionDiscount;
    private final BigDecimal membershipDiscount;
    private final String description;

    public static DiscountResult none() {
        return DiscountResult.builder()
                .totalDiscount(BigDecimal.ZERO)
                .promotionDiscount(BigDecimal.ZERO)
                .membershipDiscount(BigDecimal.ZERO)
                .description("Không có giảm giá")
                .build();
    }
}
