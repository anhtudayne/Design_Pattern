package com.cinema.booking.services.strategy_decorator.pricing.specification;

import com.cinema.booking.entities.Room;
import com.cinema.booking.entities.Showtime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test cho PricingConditions — kiểm tra 4 predicate với 8 test case.
 * Không cần Spring context (pure unit test).
 */
class PricingConditionsTest {

    // -------------------------------------------------------------------------
    // isWeekend()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("isWeekend: Thứ 7 → true")
    void isWeekend_saturday_returnsTrue() {
        // 2026-04-11 là Thứ 7
        PricingSpecificationContext ctx = buildContext(LocalDateTime.of(2026, 4, 11, 19, 0), 0, 0, LocalDateTime.now());
        assertThat(PricingConditions.isWeekend().test(ctx)).isTrue();
    }

    @Test
    @DisplayName("isWeekend: Thứ 2 → false")
    void isWeekend_monday_returnsFalse() {
        // 2026-04-13 là Thứ 2
        PricingSpecificationContext ctx = buildContext(LocalDateTime.of(2026, 4, 13, 19, 0), 0, 0, LocalDateTime.now());
        assertThat(PricingConditions.isWeekend().test(ctx)).isFalse();
    }

    // -------------------------------------------------------------------------
    // isHoliday()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("isHoliday: 30/4 (Giải phóng miền Nam) → true")
    void isHoliday_april30_returnsTrue() {
        PricingSpecificationContext ctx = buildContext(LocalDateTime.of(2026, 4, 30, 10, 0), 0, 0, LocalDateTime.now());
        assertThat(PricingConditions.isHoliday().test(ctx)).isTrue();
    }

    @Test
    @DisplayName("isHoliday: 15/6 (ngày bình thường) → false")
    void isHoliday_june15_returnsFalse() {
        PricingSpecificationContext ctx = buildContext(LocalDateTime.of(2026, 6, 15, 10, 0), 0, 0, LocalDateTime.now());
        assertThat(PricingConditions.isHoliday().test(ctx)).isFalse();
    }

    // -------------------------------------------------------------------------
    // isEarlyBird()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("isEarlyBird: bookingTime = showtime - 4 ngày → true")
    void isEarlyBird_fourDaysBefore_returnsTrue() {
        LocalDateTime showtime = LocalDateTime.of(2026, 5, 10, 20, 0);
        LocalDateTime bookingTime = showtime.minusDays(4);
        PricingSpecificationContext ctx = buildContext(showtime, 0, 0, bookingTime);
        assertThat(PricingConditions.isEarlyBird().test(ctx)).isTrue();
    }

    @Test
    @DisplayName("isEarlyBird: bookingTime = showtime - 1 ngày → false")
    void isEarlyBird_oneDayBefore_returnsFalse() {
        LocalDateTime showtime = LocalDateTime.of(2026, 5, 10, 20, 0);
        LocalDateTime bookingTime = showtime.minusDays(1);
        PricingSpecificationContext ctx = buildContext(showtime, 0, 0, bookingTime);
        assertThat(PricingConditions.isEarlyBird().test(ctx)).isFalse();
    }

    // -------------------------------------------------------------------------
    // isHighOccupancy()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("isHighOccupancy(80): booked=85, total=100 → true (85% > 80%)")
    void isHighOccupancy_85of100_withThreshold80_returnsTrue() {
        PricingSpecificationContext ctx = buildContext(LocalDateTime.now(), 85, 100, LocalDateTime.now());
        assertThat(PricingConditions.isHighOccupancy(80).test(ctx)).isTrue();
    }

    @Test
    @DisplayName("isHighOccupancy(80): booked=70, total=100 → false (70% ≤ 80%)")
    void isHighOccupancy_70of100_withThreshold80_returnsFalse() {
        PricingSpecificationContext ctx = buildContext(LocalDateTime.now(), 70, 100, LocalDateTime.now());
        assertThat(PricingConditions.isHighOccupancy(80).test(ctx)).isFalse();
    }

    @Test
    @DisplayName("isHighOccupancy: totalSeatsCount=0 → false (guard chia cho 0)")
    void isHighOccupancy_zeroTotalSeats_returnsFalse() {
        PricingSpecificationContext ctx = buildContext(LocalDateTime.now(), 0, 0, LocalDateTime.now());
        assertThat(PricingConditions.isHighOccupancy(80).test(ctx)).isFalse();
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Xây dựng PricingSpecificationContext tối giản cho mục đích test predicates.
     * Các field không liên quan (seats, customer, promotion, fnbTotal) được set null/zero.
     */
    private PricingSpecificationContext buildContext(LocalDateTime showtimeStart,
                                        int bookedSeatsCount,
                                        int totalSeatsCount,
                                        LocalDateTime bookingTime) {
        Room room = Room.builder().roomId(1).build();
        Showtime showtime = Showtime.builder()
                .showtimeId(1)
                .room(room)
                .startTime(showtimeStart)
                .endTime(showtimeStart.plusHours(2))
                .basePrice(BigDecimal.valueOf(100_000))
                .build();

        return new PricingSpecificationContext(
                showtime,
                List.of(),   // seats không cần thiết cho predicates này
                null,        // customer
                null,        // promotion
                BigDecimal.ZERO, // fnbTotal
                bookedSeatsCount,
                totalSeatsCount,
                bookingTime
        );
    }
}
