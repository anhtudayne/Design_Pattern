package com.cinema.booking.services.strategy_decorator.pricing;

import com.cinema.booking.entities.User;
import com.cinema.booking.entities.Promotion;
import com.cinema.booking.entities.Seat;
import com.cinema.booking.entities.Showtime;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PricingContext {
    private Showtime showtime;
    private List<Seat> seats;
    /** FnB items đã được resolve giá từ DB trước khi vào engine. */
    private List<ResolvedFnbItem> resolvedFnbs;
    private Promotion promotion;
    /** Người dùng đặt vé — null nếu khách vãng lai. */
    private User user;
    /** Thời điểm đặt vé — dùng cho EarlyBird / time-based checks. */
    @Builder.Default
    private LocalDateTime bookingTime = LocalDateTime.now();
    /** Số vé đã bán cho suất chiếu — dùng tính occupancy. */
    private int bookedSeatsCount;
    /** Tổng số ghế trong phòng chiếu. */
    private int totalSeatsCount;

    /** Value object chứa FnB item đã resolved giá từ DB. */
    public record ResolvedFnbItem(Integer itemId, String name, BigDecimal price, int quantity) {}
}
