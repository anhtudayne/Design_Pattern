package com.cinema.booking.pattern.decorator;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** Kết quả gộp từ chuỗi decorator; dùng để lắp {@code PriceBreakdownDTO}. */
@Getter
@Builder
public class DiscountResult {

    @Builder.Default
    private final BigDecimal totalDiscount = BigDecimal.ZERO;

    @Builder.Default
    private final BigDecimal promotionDiscount = BigDecimal.ZERO;

    @Builder.Default
    private final BigDecimal membershipDiscount = BigDecimal.ZERO;

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
