package com.cinema.booking.patterns.pricing.strategy;

import com.cinema.booking.patterns.pricing.context.PricingContext;

import java.math.BigDecimal;

/**
 * Strategy interface — mỗi implementation mô tả một công thức điều chỉnh basePrice.
 *
 * <p>OCP: thêm strategy mới = thêm class @Component mới implement interface này.
 * {@link PricingStrategySelector} không cần biết strategy nào tồn tại — chỉ iterate List.</p>
 */
public interface PricingStrategy {

    /**
     * Điều chỉnh giá cơ sở của showtime cho một ghế.
     *
     * @param basePrice giá cơ sở từ {@code Showtime.basePrice}
     * @param ctx       pricing context đầy đủ (không cần gọi DB thêm)
     * @return giá đã điều chỉnh
     */
    BigDecimal adjustBasePrice(BigDecimal basePrice, PricingContext ctx);

    /**
     * Strategy tự khai báo điều kiện áp dụng.
     * Selector không cần biết cụ thể là điều kiện gì — OCP.
     */
    boolean isApplicable(PricingContext ctx);

    /**
     * Số ưu tiên: số càng nhỏ, ưu tiên càng cao.
     * Selector sort theo priority, chọn strategy đầu tiên có isApplicable = true.
     */
    int priority();

    /** Tên hiển thị — ghi vào PriceBreakdownDTO.appliedStrategy. */
    String name();
}
