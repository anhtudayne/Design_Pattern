package com.cinema.booking.patterns.pricing.context;

import com.cinema.booking.entities.Customer;
import com.cinema.booking.entities.Promotion;
import com.cinema.booking.entities.Seat;
import com.cinema.booking.entities.Showtime;
import jakarta.annotation.Nullable;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable value object chứa toàn bộ dữ liệu cần thiết để tính giá vé.
 * Được build một lần bởi PricingContextFactory — các Pattern layer (Strategy, Decorator, CoR)
 * không cần gọi DB, chỉ đọc từ context này.
 */
@Value
public class PricingContext {

    /** Suất chiếu cần tính giá — chứa basePrice và thông tin phòng chiếu. */
    Showtime showtime;

    /** Danh sách ghế được chọn — mỗi ghế có SeatType.priceSurcharge. */
    List<Seat> seats;

    /** Khách hàng đặt vé — null nếu anonymous hoặc chưa đăng nhập. */
    @Nullable Customer customer;

    /** Mã khuyến mãi áp dụng — null nếu không có promoCode hoặc đã hết hạn. */
    @Nullable Promotion promotion;

    /** Tổng tiền F&B đã tính trước: sum(fnbItem.price * qty). */
    BigDecimal fnbTotal;

    /** Số vé đã bán cho suất chiếu này — dùng để tính occupancy. */
    int bookedSeatsCount;

    /** Tổng số ghế trong phòng chiếu — dùng để tính occupancy. */
    int totalSeatsCount;

    /** Thời điểm đặt vé = LocalDateTime.now() tại thời điểm gọi service. */
    LocalDateTime bookingTime;
}
