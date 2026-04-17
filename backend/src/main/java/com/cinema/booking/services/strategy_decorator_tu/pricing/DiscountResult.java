package com.cinema.booking.services.strategy_decorator_tu.pricing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Kết quả trả về từ Discount chain.
 * Chứa tổng giảm giá và mô tả chi tiết.
 */
@Getter
@Builder
@AllArgsConstructor
public class DiscountResult {
    private BigDecimal totalDiscount;
    private String description;

    public static DiscountResult none() {
        return new DiscountResult(BigDecimal.ZERO, "Không có giảm giá");
    }
}
