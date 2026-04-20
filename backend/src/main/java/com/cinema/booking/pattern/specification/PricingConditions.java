package com.cinema.booking.pattern.specification;

import java.time.DayOfWeek;
import java.time.MonthDay;
import java.util.Set;
import java.util.function.Predicate;


public final class PricingConditions {

    private PricingConditions() { /* static utils only */ }

    private static final int EARLY_BIRD_DAYS = 3;

    private static final Set<MonthDay> VIETNAMESE_HOLIDAYS = Set.of(
            MonthDay.of(1, 1),   // Tết Dương lịch
            MonthDay.of(4, 30),  // Giải phóng miền Nam
            MonthDay.of(5, 1),   // Quốc tế Lao động
            MonthDay.of(9, 2)    // Quốc khánh
    );


    public static Predicate<PricingSpecificationContext> isWeekend() {
        return ctx -> {
            DayOfWeek day = ctx.getShowtime().getStartTime().getDayOfWeek();
            return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        };
    }

    public static Predicate<PricingSpecificationContext> isHoliday() {
        return ctx -> {
            MonthDay md = MonthDay.from(ctx.getShowtime().getStartTime());
            return VIETNAMESE_HOLIDAYS.contains(md);
        };
    }


    public static Predicate<PricingSpecificationContext> isEarlyBird() {
        return ctx -> {
            long daysBefore = java.time.temporal.ChronoUnit.DAYS.between(
                    ctx.getBookingTime(), ctx.getShowtime().getStartTime());
            return daysBefore >= EARLY_BIRD_DAYS;
        };
    }


    public static Predicate<PricingSpecificationContext> isHighOccupancy(int thresholdPct) {
        return ctx -> {
            if (ctx.getTotalSeatsCount() == 0) return false;
            int occupancyPct = ctx.getBookedSeatsCount() * 100 / ctx.getTotalSeatsCount();
            return occupancyPct > thresholdPct;
        };
    }
}
