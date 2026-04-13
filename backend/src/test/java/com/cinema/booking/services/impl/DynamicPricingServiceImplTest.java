package com.cinema.booking.services.impl;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.*;
import com.cinema.booking.patterns.pricing.context.PricingContext;
import com.cinema.booking.patterns.pricing.context.PricingContextFactory;
import com.cinema.booking.patterns.pricing.decorator.BasePriceCalculator;
import com.cinema.booking.patterns.pricing.decorator.PriceCalculatorChainFactory;
import com.cinema.booking.patterns.pricing.strategy.*;
import com.cinema.booking.patterns.pricing.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test cho DynamicPricingServiceImpl — không cần Spring context.
 *
 * PricingContextFactory được stub qua anonymous subclass (không cần Mockito):
 * override build() trả về PricingContext đã chuẩn bị sẵn.
 * PriceCalculatorChainFactory + PriceValidationChain là real instances.
 */
class DynamicPricingServiceImplTest {

    /** PricingContext sẽ được set trước mỗi test case */
    private PricingContext stubbedCtx;

    private DynamicPricingServiceImpl service;

    private static final BigDecimal BASE_PRICE     = new BigDecimal("100000");
    private static final BigDecimal ZERO_SURCHARGE = BigDecimal.ZERO;

    @BeforeEach
    void setUp() {
        // Stub PricingContextFactory bằng anonymous subclass — không gọi DB
        PricingContextFactory contextFactory =
                new PricingContextFactory(null, null, null, null, null, null) {
                    @Override
                    public PricingContext build(BookingCalculationDTO request) {
                        return stubbedCtx;
                    }

                    @Override
                    public PricingContext build(BookingCalculationDTO request, Integer userId) {
                        return stubbedCtx;
                    }
                };

        // Real strategies với % được set qua ReflectionTestUtils
        EarlyBirdPricingStrategy earlyBird = new EarlyBirdPricingStrategy();
        ReflectionTestUtils.setField(earlyBird, "earlyBirdDiscountPct", new BigDecimal("10"));

        HolidayPricingStrategy holiday = new HolidayPricingStrategy();
        ReflectionTestUtils.setField(holiday, "holidaySurchargePct", new BigDecimal("20"));

        WeekendPricingStrategy weekend = new WeekendPricingStrategy();
        ReflectionTestUtils.setField(weekend, "weekendSurchargePct", new BigDecimal("15"));

        PricingStrategySelector selector = new PricingStrategySelector(
                List.of(earlyBird, holiday, weekend, new StandardPricingStrategy()));

        BasePriceCalculator base = new BasePriceCalculator(selector);
        PriceCalculatorChainFactory chainFactory = new PriceCalculatorChainFactory(base);

        // Real validation chain: MinPrice → MaxDiscount → FraudDetection
        MinPriceHandler minPrice = new MinPriceHandler();
        MaxDiscountHandler maxDiscount = new MaxDiscountHandler();
        FraudDetectionHandler fraudDetection = new FraudDetectionHandler();
        minPrice.setNext(maxDiscount);
        maxDiscount.setNext(fraudDetection);

        service = new DynamicPricingServiceImpl(contextFactory, chainFactory, minPrice);
    }

    // =========================================================================
    // 1. STANDARD strategy
    // =========================================================================

    @Test
    @DisplayName("Thứ 2, anonymous, không promo → STANDARD, discount=0")
    void standard_anonymousNoPromo() {
        // 2026-04-13 Thứ 2, booking cùng ngày → không EarlyBird
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 13, 20, 0),
                LocalDateTime.of(2026, 4, 13, 10, 0),
                null, null, BigDecimal.ZERO, 0, 100);

        PriceBreakdownDTO result = service.calculatePrice(new BookingCalculationDTO());

        assertThat(result.getAppliedStrategy()).isEqualTo("STANDARD");
        // ticketTotal = 2 * 100,000 = 200,000
        assertThat(result.getTicketTotal()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(result.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getFinalTotal()).isEqualByComparingTo(new BigDecimal("200000"));
    }

    // =========================================================================
    // 2. WEEKEND strategy (+15%)
    // =========================================================================

    @Test
    @DisplayName("Thứ 7, booking cùng ngày → WEEKEND, ticketTotal tăng 15%")
    void weekend_sameDay_appliesSurcharge() {
        // 2026-04-11 Thứ 7, booking cùng ngày → 0 ngày trước → không EarlyBird
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 11, 20, 0),
                LocalDateTime.of(2026, 4, 11, 10, 0),
                null, null, BigDecimal.ZERO, 0, 100);

        PriceBreakdownDTO result = service.calculatePrice(new BookingCalculationDTO());

        assertThat(result.getAppliedStrategy()).isEqualTo("WEEKEND");
        // ticketTotal = 2 * 100,000 * 1.15 = 230,000
        assertThat(result.getTicketTotal()).isEqualByComparingTo(new BigDecimal("230000.0"));
    }

    // =========================================================================
    // 3. HOLIDAY strategy (+20%)
    // =========================================================================

    @Test
    @DisplayName("30/4, booking cùng ngày → HOLIDAY, ticketTotal tăng 20%")
    void holiday_30april_appliesSurcharge() {
        // 2026-04-30 (ngày lễ), booking cùng ngày → không EarlyBird
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 30, 20, 0),
                LocalDateTime.of(2026, 4, 30, 10, 0),
                null, null, BigDecimal.ZERO, 0, 100);

        PriceBreakdownDTO result = service.calculatePrice(new BookingCalculationDTO());

        assertThat(result.getAppliedStrategy()).isEqualTo("HOLIDAY");
        // ticketTotal = 2 * 100,000 * 1.20 = 240,000
        assertThat(result.getTicketTotal()).isEqualByComparingTo(new BigDecimal("240000.0"));
    }

    // =========================================================================
    // 4. EARLY_BIRD strategy (-10%), ngày thường
    // =========================================================================

    @Test
    @DisplayName("Thứ 3, đặt trước 4 ngày → EARLY_BIRD, ticketTotal giảm 10%")
    void earlyBird_tuesdayFourDaysBefore() {
        // 2026-04-14 Thứ 3, booking 2026-04-10 (4 ngày trước)
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 14, 20, 0),
                LocalDateTime.of(2026, 4, 10, 10, 0),
                null, null, BigDecimal.ZERO, 0, 100);

        PriceBreakdownDTO result = service.calculatePrice(new BookingCalculationDTO());

        assertThat(result.getAppliedStrategy()).isEqualTo("EARLY_BIRD");
        // ticketTotal = 2 * 100,000 * 0.90 = 180,000
        assertThat(result.getTicketTotal()).isEqualByComparingTo(new BigDecimal("180000.0"));
    }

    // =========================================================================
    // 5. EARLY_BIRD ưu tiên hơn WEEKEND (priority 10 < 30)
    // =========================================================================

    @Test
    @DisplayName("Thứ 7, đặt trước 4 ngày → EARLY_BIRD thắng WEEKEND (priority 10 < 30)")
    void earlyBird_winsOverWeekend() {
        // 2026-04-18 Thứ 7, booking 2026-04-14 (4 ngày trước)
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 18, 20, 0),
                LocalDateTime.of(2026, 4, 14, 10, 0),
                null, null, BigDecimal.ZERO, 0, 100);

        PriceBreakdownDTO result = service.calculatePrice(new BookingCalculationDTO());

        assertThat(result.getAppliedStrategy()).isEqualTo("EARLY_BIRD");
        assertThat(result.getTicketTotal()).isEqualByComparingTo(new BigDecimal("180000.0"));
    }

    // =========================================================================
    // 6. Occupancy 90% → occupancySurcharge = ticketTotal * 10%
    // =========================================================================

    @Test
    @DisplayName("Occupancy 90% → occupancySurcharge = ticketTotal * 10%")
    void highOccupancy_addsSurcharge() {
        // Thứ 2, cùng ngày → STANDARD; booked=90/100
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 13, 20, 0),
                LocalDateTime.of(2026, 4, 13, 10, 0),
                null, null, BigDecimal.ZERO, 90, 100);

        PriceBreakdownDTO result = service.calculatePrice(new BookingCalculationDTO());

        // ticketTotal=200,000; surcharge=200,000*10%=20,000; finalTotal=220,000
        assertThat(result.getOccupancySurcharge()).isEqualByComparingTo(new BigDecimal("20000.0"));
        assertThat(result.getFinalTotal()).isEqualByComparingTo(new BigDecimal("220000.0"));
    }

    // =========================================================================
    // 7. VIP member 10% → membershipDiscount = ticketTotal * 10%
    // =========================================================================

    @Test
    @DisplayName("VIP tier 10% → membershipDiscount = ticketTotal * 10%")
    void vipMember_getsDiscount() {
        Customer customer = buildCustomer(new BigDecimal("10"));
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 13, 20, 0),
                LocalDateTime.of(2026, 4, 13, 10, 0),
                customer, null, BigDecimal.ZERO, 0, 100);

        PriceBreakdownDTO result = service.calculatePrice(new BookingCalculationDTO());

        // membershipDiscount = 200,000 * 10% = 20,000; finalTotal = 180,000
        assertThat(result.getMembershipDiscount()).isEqualByComparingTo(new BigDecimal("20000.0"));
        assertThat(result.getFinalTotal()).isEqualByComparingTo(new BigDecimal("180000.0"));
    }

    // =========================================================================
    // 8. Voucher FIXED 50k → discountAmount = 50k
    // =========================================================================

    @Test
    @DisplayName("Voucher FIXED 50,000 → discountAmount = 50,000")
    void fixedVoucher_applied() {
        Promotion promo = Promotion.builder()
                .discountType(Promotion.DiscountType.FIXED)
                .discountValue(new BigDecimal("50000"))
                .validTo(LocalDateTime.now().plusDays(30))
                .build();
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 13, 20, 0),
                LocalDateTime.of(2026, 4, 13, 10, 0),
                null, promo, BigDecimal.ZERO, 0, 100);

        PriceBreakdownDTO result = service.calculatePrice(new BookingCalculationDTO());

        assertThat(result.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(result.getFinalTotal()).isEqualByComparingTo(new BigDecimal("150000"));
    }

    // =========================================================================
    // 9. Discount > 50% subtotal → throw IllegalStateException (MaxDiscountHandler)
    // =========================================================================

    @Test
    @DisplayName("Voucher PERCENT 80% → tổng giảm > 50% subtotal → throw")
    void excessiveDiscount_throwsValidationError() {
        // voucherDiscount = subtotal * 80% = 200,000 * 80% = 160,000
        // ratio = 160,000/200,000 = 0.80 > 0.50 → MaxDiscountHandler throw
        Promotion promo = Promotion.builder()
                .discountType(Promotion.DiscountType.PERCENT)
                .discountValue(new BigDecimal("80"))
                .validTo(LocalDateTime.now().plusDays(30))
                .build();
        stubbedCtx = buildCtx(
                LocalDateTime.of(2026, 4, 13, 20, 0),
                LocalDateTime.of(2026, 4, 13, 10, 0),
                null, promo, BigDecimal.ZERO, 0, 100);

        assertThatThrownBy(() -> service.calculatePrice(new BookingCalculationDTO()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("50%");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private PricingContext buildCtx(LocalDateTime showtimeStart,
                                     LocalDateTime bookingTime,
                                     Customer customer,
                                     Promotion promotion,
                                     BigDecimal fnbTotal,
                                     int bookedSeats,
                                     int totalSeats) {
        Room room = Room.builder().roomId(1).build();
        Showtime showtime = Showtime.builder()
                .showtimeId(1).room(room)
                .startTime(showtimeStart)
                .endTime(showtimeStart.plusHours(2))
                .basePrice(BASE_PRICE)
                .build();

        SeatType std = SeatType.builder().priceSurcharge(ZERO_SURCHARGE).build();
        List<Seat> seats = List.of(
                Seat.builder().seatId(1).seatType(std).build(),
                Seat.builder().seatId(2).seatType(std).build()
        );

        return new PricingContext(showtime, seats, customer, promotion,
                fnbTotal, bookedSeats, totalSeats, bookingTime);
    }

    private Customer buildCustomer(BigDecimal discountPercent) {
        MembershipTier tier = MembershipTier.builder()
                .discountPercent(discountPercent).build();
        Customer customer = new Customer();
        customer.setTier(tier);
        return customer;
    }
}
