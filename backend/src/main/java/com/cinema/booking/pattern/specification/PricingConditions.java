package com.cinema.booking.pattern.specification;
import com.cinema.booking.pattern.specification.PricingSpecificationContext;
import com.cinema.booking.pattern.specification.PricingConditions;

import java.time.DayOfWeek;
import java.time.MonthDay;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Static factory class khai báo các Predicate điều kiện giá vé.
 * Kết hợp predicate qua Predicate.and() / .or() / .negate().
 *
 * <pre>
 * Ví dụ:
 *   Predicate&lt;PricingSpecificationContext&gt; surge = isWeekend().or(isHoliday());
 *   boolean apply = surge.test(ctx);
 * </pre>
 */
public final class PricingConditions {

    private PricingConditions() { /* static utils only */ }

    // -------------------------------------------------------------------------
    // Hằng số nghiệp vụ
    // -------------------------------------------------------------------------

    /** Số ngày tối thiểu đặt trước để được coi là Early Bird. */
    private static final int EARLY_BIRD_DAYS = 3;

    /**
     * Danh sách ngày lễ Việt Nam theo dương lịch.
     * TODO: bổ sung ngày Tết Nguyên Đán (âm lịch) — cần tính chuyển đổi âm-dương
     *       theo từng năm cụ thể (ví dụ dùng thư viện UnicodeICU hoặc hardcode từng năm).
     */
    private static final Set<MonthDay> VIETNAMESE_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),   // Tết Dương lịch
            MonthDay.of(4, 30),  // Giải phóng miền Nam
            MonthDay.of(5, 1),   // Quốc tế Lao động
            MonthDay.of(9, 2)    // Quốc khánh
    );

    // -------------------------------------------------------------------------
    // Predicates
    // -------------------------------------------------------------------------

    /**
     * Suất chiếu vào cuối tuần (Thứ 7 hoặc Chủ nhật).
     */
    public static Predicate<PricingSpecificationContext> isWeekend() {
        return ctx -> {
            DayOfWeek day = ctx.getShowtime().getStartTime().getDayOfWeek();
            return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        };
    }

    /**
     * Suất chiếu rơi vào ngày lễ (danh sách {@link #VIETNAMESE_HOLIDAYS}).
     */
    public static Predicate<PricingSpecificationContext> isHoliday() {
        return ctx -> {
            MonthDay md = MonthDay.from(ctx.getShowtime().getStartTime());
            return VIETNAMESE_HOLIDAYS.contains(md);
        };
    }

    /**
     * Đặt trước ít nhất {@link #EARLY_BIRD_DAYS} ngày so với giờ chiếu.
     */
    public static Predicate<PricingSpecificationContext> isEarlyBird() {
        return ctx -> {
            long daysBefore = java.time.temporal.ChronoUnit.DAYS.between(
                    ctx.getBookingTime(), ctx.getShowtime().getStartTime());
            return daysBefore >= EARLY_BIRD_DAYS;
        };
    }

    /**
     * Tỷ lệ lấp đầy vượt ngưỡng {@code thresholdPct}%.
     * Guard: nếu totalSeatsCount = 0 thì trả về false (tránh chia cho 0).
     */
    public static Predicate<PricingSpecificationContext> isHighOccupancy(int thresholdPct) {
        return ctx -> {
            if (ctx.getTotalSeatsCount() == 0) return false;
            int occupancyPct = ctx.getBookedSeatsCount() * 100 / ctx.getTotalSeatsCount();
            return occupancyPct > thresholdPct;
        };
    }
}
