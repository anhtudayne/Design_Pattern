package com.cinema.booking.patterns.pricing.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test cho PriceValidationChain — 10 case, không cần Spring context.
 */
class PriceValidationChainTest {

    // =========================================================================
    // MinPriceHandler
    // =========================================================================

    @Nested
    @DisplayName("MinPriceHandler")
    class MinPriceTests {

        private final MinPriceHandler handler = new MinPriceHandler();

        @Test
        @DisplayName("finalTotal âm → throw IllegalStateException")
        void negativeFinalTotal_throws() {
            PriceValidationContext ctx = ctx(bd("200000"), bd("0"), bd("-100"), 2);
            assertThatThrownBy(() -> handler.validate(ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("không thể âm");
        }

        @Test
        @DisplayName("finalTotal = 0 → pass (boundary)")
        void zeroFinalTotal_passes() {
            PriceValidationContext ctx = ctx(bd("200000"), bd("200000"), bd("0"), 2);
            assertThatCode(() -> handler.validate(ctx)).doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // MaxDiscountHandler
    // =========================================================================

    @Nested
    @DisplayName("MaxDiscountHandler")
    class MaxDiscountTests {

        private final MaxDiscountHandler handler = new MaxDiscountHandler();

        @Test
        @DisplayName("discount/subtotal = 0.51 (51%) → throw")
        void discountOver50pct_throws() {
            // subtotal=100_000, discount=51_000 → ratio=0.51
            PriceValidationContext ctx = ctx(bd("100000"), bd("51000"), bd("49000"), 1);
            assertThatThrownBy(() -> handler.validate(ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("50%");
        }

        @Test
        @DisplayName("discount/subtotal = 0.50 (50%) → pass (boundary exact)")
        void discountExactly50pct_passes() {
            // subtotal=100_000, discount=50_000 → ratio=0.50
            PriceValidationContext ctx = ctx(bd("100000"), bd("50000"), bd("50000"), 1);
            assertThatCode(() -> handler.validate(ctx)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("subtotal = 0 → pass (guard chia 0)")
        void zeroSubtotal_passes() {
            PriceValidationContext ctx = ctx(bd("0"), bd("0"), bd("0"), 0);
            assertThatCode(() -> handler.validate(ctx)).doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // FraudDetectionHandler
    // =========================================================================

    @Nested
    @DisplayName("FraudDetectionHandler")
    class FraudDetectionTests {

        private final FraudDetectionHandler handler = new FraudDetectionHandler();

        @Test
        @DisplayName("pricePerSeat = 9_999 VND → throw")
        void tooLowPricePerSeat_throws() {
            // finalTotal=9_999, seatCount=1
            PriceValidationContext ctx = ctx(bd("100000"), bd("90001"), bd("9999"), 1);
            assertThatThrownBy(() -> handler.validate(ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("bất thường");
        }

        @Test
        @DisplayName("pricePerSeat = 10_000 VND → pass (boundary)")
        void exactMinPricePerSeat_passes() {
            // finalTotal=20_000, seatCount=2 → 10_000/seat
            PriceValidationContext ctx = ctx(bd("100000"), bd("80000"), bd("20000"), 2);
            assertThatCode(() -> handler.validate(ctx)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("seatCount = 0 → pass (guard chia 0)")
        void zeroSeatCount_passes() {
            PriceValidationContext ctx = ctx(bd("0"), bd("0"), bd("0"), 0);
            assertThatCode(() -> handler.validate(ctx)).doesNotThrowAnyException();
        }
    }

    // =========================================================================
    // Full chain (MinPrice → MaxDiscount → FraudDetection)
    // =========================================================================

    @Nested
    @DisplayName("Full chain")
    class FullChainTests {

        private PriceValidationHandler buildChain() {
            MinPriceHandler minPrice = new MinPriceHandler();
            MaxDiscountHandler maxDiscount = new MaxDiscountHandler();
            FraudDetectionHandler fraudDetection = new FraudDetectionHandler();
            minPrice.setNext(maxDiscount);
            maxDiscount.setNext(fraudDetection);
            return minPrice;
        }

        @Test
        @DisplayName("Happy path: tất cả rule OK → pass hết chain")
        void happyPath_passesAllHandlers() {
            // subtotal=200_000, discount=40_000 (20%), finalTotal=160_000, 2 ghế → 80_000/ghế
            PriceValidationContext ctx = ctx(bd("200000"), bd("40000"), bd("160000"), 2);
            assertThatCode(() -> buildChain().validate(ctx)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Discount 60% → throw tại MaxDiscountHandler")
        void discountOver50_throwsAtMaxDiscount() {
            // subtotal=200_000, discount=120_000 (60%), finalTotal=80_000
            PriceValidationContext ctx = ctx(bd("200000"), bd("120000"), bd("80000"), 2);
            assertThatThrownBy(() -> buildChain().validate(ctx))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("50%");
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private PriceValidationContext ctx(BigDecimal subtotal, BigDecimal totalDiscount,
                                       BigDecimal finalTotal, int seatCount) {
        return new PriceValidationContext(subtotal, totalDiscount, finalTotal, seatCount);
    }

    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }
}
