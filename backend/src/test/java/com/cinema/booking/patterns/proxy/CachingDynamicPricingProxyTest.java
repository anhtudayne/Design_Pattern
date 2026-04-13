package com.cinema.booking.patterns.proxy;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.services.DynamicPricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test cho CachingDynamicPricingProxy — không cần Spring / Redis / Mockito.
 *
 * Test subclass override cacheGet/cachePut → HashMap in-memory thay Redis.
 * Delegate stub dùng AtomicInteger counter để kiểm tra số lần gọi.
 */
class CachingDynamicPricingProxyTest {

    private Map<String, Object> store;
    private AtomicInteger delegateCallCount;

    private static final PriceBreakdownDTO FIXED_RESULT = PriceBreakdownDTO.builder()
            .ticketTotal(new BigDecimal("200000"))
            .finalTotal(new BigDecimal("200000"))
            .appliedStrategy("STANDARD")
            .build();

    /** Proxy với in-memory cache + counting delegate */
    private CachingDynamicPricingProxy proxy;

    @BeforeEach
    void setUp() {
        store = new HashMap<>();
        delegateCallCount = new AtomicInteger(0);

        // Proxy subclass: override cache ops → HashMap, không cần RedisTemplate
        proxy = new CachingDynamicPricingProxy() {
            @Override
            protected Object cacheGet(String key) {
                return store.get(key);
            }

            @Override
            protected void cachePut(String key, PriceBreakdownDTO result) {
                store.put(key, result);
            }
        };

        // Inject delegate stub: đếm lần gọi
        DynamicPricingService stubDelegate = request -> {
            delegateCallCount.incrementAndGet();
            return FIXED_RESULT;
        };
        ReflectionTestUtils.setField(proxy, "delegate", stubDelegate);
    }

    // =========================================================================
    // Cache key
    // =========================================================================

    @Nested
    @DisplayName("Cache key")
    class CacheKeyTests {

        @Test
        @DisplayName("Format đúng: pricing:{showtimeId}:[sorted seatIds]:{userId}:{promo}")
        void cacheKey_correctFormat() {
            BookingCalculationDTO req = req(1, List.of(3, 1, 2), "PROMO10");
            assertThat(proxy.buildCacheKey(req))
                    .isEqualTo("pricing:1:[1,2,3]:anonymous:PROMO10");
        }

        @Test
        @DisplayName("Không có promo → promoPart = 'none'")
        void cacheKey_noPromo_usesNone() {
            BookingCalculationDTO req = req(5, List.of(10), null);
            assertThat(proxy.buildCacheKey(req))
                    .isEqualTo("pricing:5:[10]:anonymous:none");
        }

        @Test
        @DisplayName("seatIds thứ tự khác nhau → cùng key (sort đảm bảo idempotent)")
        void cacheKey_differentSeatOrder_sameKey() {
            assertThat(proxy.buildCacheKey(req(1, List.of(1, 2, 3), null)))
                    .isEqualTo(proxy.buildCacheKey(req(1, List.of(3, 1, 2), null)));
        }

        @Test
        @DisplayName("Showtime khác nhau → key khác nhau")
        void cacheKey_differentShowtime_differentKey() {
            assertThat(proxy.buildCacheKey(req(1, List.of(1), null)))
                    .isNotEqualTo(proxy.buildCacheKey(req(2, List.of(1), null)));
        }

        @Test
        @DisplayName("resolveUserId: không có SecurityContext → 'anonymous'")
        void resolveUserId_noContext_returnsAnonymous() {
            assertThat(proxy.resolveUserId()).isEqualTo("anonymous");
        }
    }

    // =========================================================================
    // Cache hit / miss
    // =========================================================================

    @Nested
    @DisplayName("Cache hit / miss")
    class CacheHitMissTests {

        @Test
        @DisplayName("Cache MISS: delegate gọi 1 lần, kết quả được lưu vào store")
        void cacheMiss_callsDelegateAndCaches() {
            BookingCalculationDTO request = req(1, List.of(1, 2), null);

            PriceBreakdownDTO result = proxy.calculatePrice(request);

            assertThat(delegateCallCount.get()).isEqualTo(1);
            assertThat(result).isSameAs(FIXED_RESULT);
            assertThat(store).containsKey(proxy.buildCacheKey(request));
        }

        @Test
        @DisplayName("Cache HIT: lần 2 không gọi delegate, trả kết quả từ store")
        void cacheHit_returnsFromStore_noDelegateCall() {
            BookingCalculationDTO request = req(1, List.of(1, 2), null);

            proxy.calculatePrice(request);       // MISS → delegate gọi
            proxy.calculatePrice(request);       // HIT  → không gọi delegate

            assertThat(delegateCallCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Request khác showtime → cache miss riêng → delegate gọi 2 lần")
        void differentRequests_missedSeparately_delegateCalledTwice() {
            proxy.calculatePrice(req(1, List.of(1), null));
            proxy.calculatePrice(req(2, List.of(1), null));

            assertThat(delegateCallCount.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("Delegate throw → exception propagate, KHÔNG cache")
        void delegateThrows_exceptionPropagated_nothingCached() {
            // Tạo proxy với delegate ném exception
            CachingDynamicPricingProxy throwingProxy = new CachingDynamicPricingProxy() {
                @Override protected Object cacheGet(String key) { return store.get(key); }
                @Override protected void cachePut(String key, PriceBreakdownDTO result) { store.put(key, result); }
            };
            ReflectionTestUtils.setField(throwingProxy, "delegate",
                    (DynamicPricingService) request -> {
                        throw new IllegalStateException("Giá cuối không hợp lệ");
                    });

            BookingCalculationDTO request = req(99, List.of(1), null);

            assertThatThrownBy(() -> throwingProxy.calculatePrice(request))
                    .isInstanceOf(IllegalStateException.class);

            // Không có gì được cache sau khi exception
            assertThat(store).doesNotContainKey(throwingProxy.buildCacheKey(request));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private BookingCalculationDTO req(int showtimeId, List<Integer> seatIds, String promoCode) {
        BookingCalculationDTO req = new BookingCalculationDTO();
        req.setShowtimeId(showtimeId);
        req.setSeatIds(seatIds);
        req.setPromoCode(promoCode);
        return req;
    }
}
