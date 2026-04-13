package com.cinema.booking.patterns.pricing.strategy;

import com.cinema.booking.entities.Room;
import com.cinema.booking.entities.Showtime;
import com.cinema.booking.patterns.pricing.context.PricingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test cho Strategy layer — không cần Spring context.
 * Dùng ReflectionTestUtils để set @Value field (simulate Spring inject).
 */
class PricingStrategyTest {

    private static final BigDecimal BASE_PRICE = new BigDecimal("100000");

    // =========================================================================
    // EarlyBirdPricingStrategy
    // =========================================================================

    @Nested
    @DisplayName("EarlyBirdPricingStrategy")
    class EarlyBirdTests {

        private EarlyBirdPricingStrategy strategy;

        @BeforeEach
        void setUp() {
            strategy = new EarlyBirdPricingStrategy();
            ReflectionTestUtils.setField(strategy, "earlyBirdDiscountPct", new BigDecimal("10"));
        }

        @Test
        @DisplayName("isApplicable: đặt 10 ngày trước → true")
        void isApplicable_tenDaysBefore_returnsTrue() {
            PricingContext ctx = buildCtx(
                    LocalDateTime.of(2026, 6, 15, 20, 0),
                    LocalDateTime.of(2026, 6, 15, 20, 0).minusDays(10)
            );
            assertThat(strategy.isApplicable(ctx)).isTrue();
        }

        @Test
        @DisplayName("isApplicable: đặt 1 ngày trước → false")
        void isApplicable_oneDayBefore_returnsFalse() {
            PricingContext ctx = buildCtx(
                    LocalDateTime.of(2026, 6, 15, 20, 0),
                    LocalDateTime.of(2026, 6, 15, 20, 0).minusDays(1)
            );
            assertThat(strategy.isApplicable(ctx)).isFalse();
        }

        @Test
        @DisplayName("adjustBasePrice: giảm 10% → 90000")
        void adjustBasePrice_10pct_discount() {
            PricingContext ctx = buildCtx(
                    LocalDateTime.of(2026, 6, 15, 20, 0),
                    LocalDateTime.of(2026, 6, 15, 20, 0).minusDays(10)
            );
            BigDecimal result = strategy.adjustBasePrice(BASE_PRICE, ctx);
            assertThat(result).isEqualByComparingTo(new BigDecimal("90000.0"));
        }

        @Test
        @DisplayName("priority = 10")
        void priority_is10() {
            assertThat(strategy.priority()).isEqualTo(10);
        }
    }

    // =========================================================================
    // HolidayPricingStrategy
    // =========================================================================

    @Nested
    @DisplayName("HolidayPricingStrategy")
    class HolidayTests {

        private HolidayPricingStrategy strategy;

        @BeforeEach
        void setUp() {
            strategy = new HolidayPricingStrategy();
            ReflectionTestUtils.setField(strategy, "holidaySurchargePct", new BigDecimal("20"));
        }

        @Test
        @DisplayName("isApplicable: 30/4 → true")
        void isApplicable_april30_returnsTrue() {
            PricingContext ctx = buildCtx(LocalDateTime.of(2026, 4, 30, 10, 0), LocalDateTime.now());
            assertThat(strategy.isApplicable(ctx)).isTrue();
        }

        @Test
        @DisplayName("isApplicable: 15/6 → false")
        void isApplicable_june15_returnsFalse() {
            PricingContext ctx = buildCtx(LocalDateTime.of(2026, 6, 15, 10, 0), LocalDateTime.now());
            assertThat(strategy.isApplicable(ctx)).isFalse();
        }

        @Test
        @DisplayName("adjustBasePrice: phụ thu 20% → 120000")
        void adjustBasePrice_20pct_surcharge() {
            PricingContext ctx = buildCtx(LocalDateTime.of(2026, 4, 30, 10, 0), LocalDateTime.now());
            BigDecimal result = strategy.adjustBasePrice(BASE_PRICE, ctx);
            assertThat(result).isEqualByComparingTo(new BigDecimal("120000.0"));
        }

        @Test
        @DisplayName("priority = 20")
        void priority_is20() {
            assertThat(strategy.priority()).isEqualTo(20);
        }
    }

    // =========================================================================
    // WeekendPricingStrategy
    // =========================================================================

    @Nested
    @DisplayName("WeekendPricingStrategy")
    class WeekendTests {

        private WeekendPricingStrategy strategy;

        @BeforeEach
        void setUp() {
            strategy = new WeekendPricingStrategy();
            ReflectionTestUtils.setField(strategy, "weekendSurchargePct", new BigDecimal("15"));
        }

        @Test
        @DisplayName("isApplicable: Thứ 7 → true")
        void isApplicable_saturday_returnsTrue() {
            // 2026-04-11 là Thứ 7
            PricingContext ctx = buildCtx(LocalDateTime.of(2026, 4, 11, 19, 0), LocalDateTime.now());
            assertThat(strategy.isApplicable(ctx)).isTrue();
        }

        @Test
        @DisplayName("isApplicable: Thứ 3 → false")
        void isApplicable_tuesday_returnsFalse() {
            // 2026-04-14 là Thứ 3
            PricingContext ctx = buildCtx(LocalDateTime.of(2026, 4, 14, 19, 0), LocalDateTime.now());
            assertThat(strategy.isApplicable(ctx)).isFalse();
        }

        @Test
        @DisplayName("adjustBasePrice: phụ thu 15% → 115000")
        void adjustBasePrice_15pct_surcharge() {
            PricingContext ctx = buildCtx(LocalDateTime.of(2026, 4, 11, 19, 0), LocalDateTime.now());
            BigDecimal result = strategy.adjustBasePrice(BASE_PRICE, ctx);
            assertThat(result).isEqualByComparingTo(new BigDecimal("115000.0"));
        }

        @Test
        @DisplayName("priority = 30")
        void priority_is30() {
            assertThat(strategy.priority()).isEqualTo(30);
        }
    }

    // =========================================================================
    // StandardPricingStrategy
    // =========================================================================

    @Nested
    @DisplayName("StandardPricingStrategy")
    class StandardTests {

        private final StandardPricingStrategy strategy = new StandardPricingStrategy();

        @Test
        @DisplayName("isApplicable: luôn true (fallback)")
        void isApplicable_alwaysTrue() {
            PricingContext ctx = buildCtx(LocalDateTime.now(), LocalDateTime.now());
            assertThat(strategy.isApplicable(ctx)).isTrue();
        }

        @Test
        @DisplayName("adjustBasePrice: trả về basePrice nguyên bản")
        void adjustBasePrice_returnsUnchanged() {
            PricingContext ctx = buildCtx(LocalDateTime.now(), LocalDateTime.now());
            assertThat(strategy.adjustBasePrice(BASE_PRICE, ctx))
                    .isEqualByComparingTo(BASE_PRICE);
        }

        @Test
        @DisplayName("priority = 999 (thấp nhất)")
        void priority_is999() {
            assertThat(strategy.priority()).isEqualTo(999);
        }
    }

    // =========================================================================
    // PricingStrategySelector
    // =========================================================================

    @Nested
    @DisplayName("PricingStrategySelector")
    class SelectorTests {

        private PricingStrategySelector selector;

        @BeforeEach
        void setUp() {
            EarlyBirdPricingStrategy earlyBird = new EarlyBirdPricingStrategy();
            ReflectionTestUtils.setField(earlyBird, "earlyBirdDiscountPct", new BigDecimal("10"));

            HolidayPricingStrategy holiday = new HolidayPricingStrategy();
            ReflectionTestUtils.setField(holiday, "holidaySurchargePct", new BigDecimal("20"));

            WeekendPricingStrategy weekend = new WeekendPricingStrategy();
            ReflectionTestUtils.setField(weekend, "weekendSurchargePct", new BigDecimal("15"));

            selector = new PricingStrategySelector(List.of(
                    earlyBird,
                    holiday,
                    weekend,
                    new StandardPricingStrategy()
            ));
        }

        @Test
        @DisplayName("30/4 + đặt 4 ngày trước → EarlyBird (priority 10 < 20)")
        void select_holidayAndEarlyBird_picksEarlyBird() {
            LocalDateTime showtime = LocalDateTime.of(2026, 4, 30, 10, 0);
            PricingContext ctx = buildCtx(showtime, showtime.minusDays(4));
            assertThat(selector.select(ctx).name()).isEqualTo("EARLY_BIRD");
        }

        @Test
        @DisplayName("30/4 + đặt 1 ngày trước → Holiday (priority 20)")
        void select_holidayOnly_picksHoliday() {
            LocalDateTime showtime = LocalDateTime.of(2026, 4, 30, 10, 0);
            PricingContext ctx = buildCtx(showtime, showtime.minusDays(1));
            assertThat(selector.select(ctx).name()).isEqualTo("HOLIDAY");
        }

        @Test
        @DisplayName("Thứ 7 + đặt 1 ngày trước → Weekend (priority 30)")
        void select_weekendOnly_picksWeekend() {
            // 2026-04-11 Thứ 7, không phải ngày lễ
            LocalDateTime showtime = LocalDateTime.of(2026, 4, 11, 19, 0);
            PricingContext ctx = buildCtx(showtime, showtime.minusDays(1));
            assertThat(selector.select(ctx).name()).isEqualTo("WEEKEND");
        }

        @Test
        @DisplayName("Thứ 3 + đặt 1 ngày trước → Standard (fallback priority 999)")
        void select_noneApplicable_picksStandard() {
            // 2026-04-14 Thứ 3, ngày bình thường
            LocalDateTime showtime = LocalDateTime.of(2026, 4, 14, 19, 0);
            PricingContext ctx = buildCtx(showtime, showtime.minusDays(1));
            assertThat(selector.select(ctx).name()).isEqualTo("STANDARD");
        }

        @Test
        @DisplayName("List chỉ có [Standard] → Standard")
        void select_onlyStandard_picksStandard() {
            PricingStrategySelector singleSelector = new PricingStrategySelector(
                    List.of(new StandardPricingStrategy()));
            PricingContext ctx = buildCtx(LocalDateTime.now(), LocalDateTime.now());
            assertThat(singleSelector.select(ctx).name()).isEqualTo("STANDARD");
        }

        @Test
        @DisplayName("List rỗng → throw IllegalStateException")
        void select_emptyList_throwsException() {
            PricingStrategySelector emptySelector = new PricingStrategySelector(List.of());
            PricingContext ctx = buildCtx(LocalDateTime.now(), LocalDateTime.now());
            assertThatThrownBy(() -> emptySelector.select(ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("StandardPricingStrategy");
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private PricingContext buildCtx(LocalDateTime showtimeStart, LocalDateTime bookingTime) {
        Room room = Room.builder().roomId(1).build();
        Showtime showtime = Showtime.builder()
                .showtimeId(1)
                .room(room)
                .startTime(showtimeStart)
                .endTime(showtimeStart.plusHours(2))
                .basePrice(BASE_PRICE)
                .build();

        return new PricingContext(
                showtime,
                List.of(),
                null,
                null,
                BigDecimal.ZERO,
                0,
                100,
                bookingTime
        );
    }
}
