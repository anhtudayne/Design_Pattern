package com.cinema.booking.services.strategy_decorator_tu.pricing;

import com.cinema.booking.entities.*;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Context object chứa tất cả dữ liệu cần thiết cho việc tính giá.
 * Được truyền vào các Strategy và Decorator để tính toán.
 */
@Getter
@Builder
public class PricingContext {
    private Showtime showtime;
    private List<Seat> seats;
    private List<FnbItemQuantity> fnbItems;
    private Promotion promotion;

    /**
     * DTO nội bộ gom FnbItem + số lượng đặt.
     */
    @Getter
    @Builder
    public static class FnbItemQuantity {
        private FnbItem fnbItem;
        private Integer quantity;
    }
}
