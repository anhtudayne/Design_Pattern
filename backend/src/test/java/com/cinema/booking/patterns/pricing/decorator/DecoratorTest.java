package com.cinema.booking.patterns.pricing.decorator;

import com.cinema.booking.entities.*;
import com.cinema.booking.patterns.pricing.context.PricingContext;
import com.cinema.booking.patterns.pricing.strategy.StandardPricingStrategy;
import com.cinema.booking.patterns.pricing.strategy.PricingStrategySelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test cho Decorator layer — không cần Spring context.
 * Mỗi decorator được test độc lập với một inner giả lập (stub).
 */
class DecoratorTest {

    /** Inner stub trả về accumulator với ticketTotal = 200_000 */
    private PriceCalculator stubInner;
    private static final BigDecimal TICKET_TOTAL = new BigDecimal("200000");

    @BeforeEach
    void setUpStub() {
        stubInner = ctx -> {
            PricingAccumulator acc = new PricingAccumulator();
            acc.setTicketTotal(TICKET_TOTAL);
            acc.setAppliedStrategy("STANDARD");
            return acc;
        };
    }

    // =========================================================================
    // OccupancyDecorator
    // =========================================================================

    @Nested
    @DisplayName("OccupancyDecorator")
    class OccupancyTests {

        @Test
        @DisplayName("occupancy 90% → surcharge = ticketTotal * 10%")
        void highOccupancy_addsSurcharge() {
            PricingContext ctx = buildCtx(null, null, BigDecimal.ZERO, 90, 100);
            PricingAccumulator acc = new OccupancyDecorator(stubInner).calculate(ctx);

            BigDecimal expected = TICKET_TOTAL.multiply(new BigDecimal("10"))
                    .divide(new BigDecimal("100"));
            assertThat(acc.getOccupancySurcharge()).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("occupancy 70% → surcharge = 0")
        void lowOccupancy_noSurcharge() {
            PricingContext ctx = buildCtx(null, null, BigDecimal.ZERO, 70, 100);
            PricingAccumulator acc = new OccupancyDecorator(stubInner).calculate(ctx);

            assertThat(acc.getOccupancySurcharge()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // =========================================================================
    // FnbDecorator
    // =========================================================================

    @Nested
    @DisplayName("FnbDecorator")
    class FnbTests {

        @Test
        @DisplayName("fnbTotal = 150_000 → acc.fnbTotal = 150_000")
        void fnbDecorator_setsCorrectTotal() {
            BigDecimal fnb = new BigDecimal("150000");
            PricingContext ctx = buildCtx(null, null, fnb, 0, 100);
            PricingAccumulator acc = new FnbDecorator(stubInner).calculate(ctx);

            assertThat(acc.getFnbTotal()).isEqualByComparingTo(fnb);
        }
    }

    // =========================================================================
    // MemberDiscountDecorator
    // =========================================================================

    @Nested
    @DisplayName("MemberDiscountDecorator")
    class MemberDiscountTests {

        @Test
        @DisplayName("Customer VIP tier 10% → membershipDiscount = ticketTotal * 10%")
        void vipCustomer_getsDiscount() {
            Customer customer = buildCustomer(new BigDecimal("10"));
            PricingContext ctx = buildCtx(customer, null, BigDecimal.ZERO, 0, 100);
            PricingAccumulator acc = new MemberDiscountDecorator(stubInner).calculate(ctx);

            BigDecimal expected = TICKET_TOTAL.multiply(new BigDecimal("10"))
                    .divide(new BigDecimal("100"));
            assertThat(acc.getMembershipDiscount()).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("Anonymous (customer=null) → membershipDiscount = 0")
        void anonymousCustomer_noDiscount() {
            PricingContext ctx = buildCtx(null, null, BigDecimal.ZERO, 0, 100);
            PricingAccumulator acc = new MemberDiscountDecorator(stubInner).calculate(ctx);

            assertThat(acc.getMembershipDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // =========================================================================
    // VoucherDecorator
    // =========================================================================

    @Nested
    @DisplayName("VoucherDecorator")
    class VoucherTests {

        @Test
        @DisplayName("Promo FIXED -50_000 → voucherDiscount = 50_000")
        void fixedPromo_correctDiscount() {
            Promotion promo = buildPromo(Promotion.DiscountType.FIXED, new BigDecimal("50000"),
                    LocalDateTime.now().plusDays(10));
            PricingContext ctx = buildCtx(null, promo, BigDecimal.ZERO, 0, 100);
            PricingAccumulator acc = new VoucherDecorator(stubInner).calculate(ctx);

            assertThat(acc.getVoucherDiscount()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("Promo PERCENT 20% → voucherDiscount = subtotal * 20%")
        void percentPromo_correctDiscount() {
            Promotion promo = buildPromo(Promotion.DiscountType.PERCENT, new BigDecimal("20"),
                    LocalDateTime.now().plusDays(10));
            PricingContext ctx = buildCtx(null, promo, BigDecimal.ZERO, 0, 100);
            PricingAccumulator acc = new VoucherDecorator(stubInner).calculate(ctx);

            // subtotal = ticketTotal (no occupancy, no fnb) → 200_000 * 20% = 40_000
            assertThat(acc.getVoucherDiscount()).isEqualByComparingTo(new BigDecimal("40000.0"));
        }

        @Test
        @DisplayName("Promo hết hạn → voucherDiscount = 0")
        void expiredPromo_noDiscount() {
            Promotion promo = buildPromo(Promotion.DiscountType.FIXED, new BigDecimal("50000"),
                    LocalDateTime.now().minusDays(1)); // đã hết hạn
            PricingContext ctx = buildCtx(null, promo, BigDecimal.ZERO, 0, 100);
            PricingAccumulator acc = new VoucherDecorator(stubInner).calculate(ctx);

            assertThat(acc.getVoucherDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // =========================================================================
    // Full chain (PriceCalculatorChainFactory)
    // =========================================================================

    @Nested
    @DisplayName("Full chain — PriceCalculatorChainFactory")
    class FullChainTest {

        @Test
        @DisplayName("Đầy đủ: ticket + occupancy + fnb - membership - voucher")
        void fullChain_allFieldsCorrect() {
            // Dùng BasePriceCalculator thực với StandardStrategy (basePrice=100_000, 2 ghế Standard)
            PricingStrategySelector selector = new PricingStrategySelector(
                    List.of(new StandardPricingStrategy()));
            BasePriceCalculator realBase = new BasePriceCalculator(selector);

            // 2 ghế standard không phụ thu → ticketTotal = 2 * 100_000 = 200_000
            SeatType standard = SeatType.builder()
                    .priceSurcharge(BigDecimal.ZERO).build();
            Seat s1 = Seat.builder().seatType(standard).build();
            Seat s2 = Seat.builder().seatType(standard).build();

            // Customer VIP 10%
            Customer customer = buildCustomer(new BigDecimal("10"));

            // Promo FIXED -20_000
            Promotion promo = buildPromo(Promotion.DiscountType.FIXED, new BigDecimal("20000"),
                    LocalDateTime.now().plusDays(5));

            // Occupancy 90% → surcharge = 200_000 * 10% = 20_000
            // FnB = 50_000
            PricingContext ctx = new PricingContext(
                    buildShowtime(new BigDecimal("100000")),
                    List.of(s1, s2),
                    customer,
                    promo,
                    new BigDecimal("50000"), // fnbTotal
                    90, 100,               // booked/total → 90%
                    LocalDateTime.now()
            );

            PriceCalculator chain = new VoucherDecorator(
                    new MemberDiscountDecorator(
                            new FnbDecorator(
                                    new OccupancyDecorator(realBase))));

            PricingAccumulator acc = chain.calculate(ctx);

            // ticketTotal = 200_000
            assertThat(acc.getTicketTotal()).isEqualByComparingTo(new BigDecimal("200000"));
            // occupancySurcharge = 200_000 * 10% = 20_000
            assertThat(acc.getOccupancySurcharge()).isEqualByComparingTo(new BigDecimal("20000.0"));
            // fnbTotal = 50_000
            assertThat(acc.getFnbTotal()).isEqualByComparingTo(new BigDecimal("50000"));
            // membershipDiscount = 200_000 * 10% = 20_000
            assertThat(acc.getMembershipDiscount()).isEqualByComparingTo(new BigDecimal("20000.0"));
            // subtotal = 200_000 + 20_000 + 50_000 = 270_000
            // base voucher = subtotal - membership = 270_000 - 20_000 = 250_000
            // voucherDiscount = FIXED 20_000
            assertThat(acc.getVoucherDiscount()).isEqualByComparingTo(new BigDecimal("20000"));
            // finalTotal = subtotal - totalDiscount = 270_000 - (20_000 + 20_000) = 230_000
            assertThat(acc.finalTotal()).isEqualByComparingTo(new BigDecimal("230000.0"));
            assertThat(acc.getAppliedStrategy()).isEqualTo("STANDARD");
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private PricingContext buildCtx(Customer customer, Promotion promotion,
                                    BigDecimal fnbTotal,
                                    int booked, int total) {
        return new PricingContext(
                buildShowtime(new BigDecimal("100000")),
                List.of(),
                customer,
                promotion,
                fnbTotal,
                booked,
                total,
                LocalDateTime.now()
        );
    }

    private Showtime buildShowtime(BigDecimal basePrice) {
        Room room = Room.builder().roomId(1).build();
        return Showtime.builder()
                .showtimeId(1)
                .room(room)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .basePrice(basePrice)
                .build();
    }

    private Customer buildCustomer(BigDecimal discountPercent) {
        MembershipTier tier = MembershipTier.builder()
                .discountPercent(discountPercent)
                .build();
        Customer customer = new Customer();
        customer.setTier(tier);
        return customer;
    }

    private Promotion buildPromo(Promotion.DiscountType type, BigDecimal value,
                                  LocalDateTime validTo) {
        return Promotion.builder()
                .discountType(type)
                .discountValue(value)
                .validTo(validTo)
                .build();
    }
}
