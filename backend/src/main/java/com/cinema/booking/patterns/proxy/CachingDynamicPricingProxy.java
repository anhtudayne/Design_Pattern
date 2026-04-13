package com.cinema.booking.patterns.proxy;

import com.cinema.booking.dtos.BookingCalculationDTO;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.security.UserDetailsImpl;
import com.cinema.booking.services.DynamicPricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Proxy pattern — wrap DynamicPricingServiceImpl với Redis cache (TTL 5 phút).
 *
 * <p>@Primary: Spring inject proxy này thay vì impl ở mọi nơi inject DynamicPricingService.
 * @Qualifier("dynamicPricingServiceImpl"): delegate đến impl thực sự — tránh circular.</p>
 *
 * <p>Cache key bao gồm userId để đảm bảo membership discount chính xác:
 * VIP user và anonymous có giá khác nhau → phải cache riêng.</p>
 *
 * <p>Không có invalidate: pricing result phụ thuộc thời gian thực (occupancy, bookingTime)
 * → TTL tự hết hạn là đủ.</p>
 */
@Primary
@Service
public class CachingDynamicPricingProxy implements DynamicPricingService {

    static final long   PRICING_CACHE_TTL_SECONDS = 300L;
    static final String CACHE_KEY_PREFIX           = "pricing:";

    @Qualifier("dynamicPricingServiceImpl")
    @Autowired
    private DynamicPricingService delegate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public PriceBreakdownDTO calculatePrice(BookingCalculationDTO request) {
        String key = buildCacheKey(request);

        Object cached = cacheGet(key);
        if (cached instanceof PriceBreakdownDTO dto) {
            return dto;
        }

        // Cache MISS: delegate sang DynamicPricingServiceImpl (tính giá + validation)
        PriceBreakdownDTO result = delegate.calculatePrice(request);

        // Chỉ cache kết quả hợp lệ (delegate đã throw nếu validation fail)
        cachePut(key, result);
        return result;
    }

    // ─── Cache ops (protected để test có thể override) ───────────────────────

    protected Object cacheGet(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    protected void cachePut(String key, PriceBreakdownDTO result) {
        redisTemplate.opsForValue().set(key, result, PRICING_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    // ─── Cache key ──────────────────────────────────────────────────────────

    /**
     * Format: {@code pricing:{showtimeId}:[{sorted seatIds}]:{userId}:{promoCode}}
     * Sorted seatIds: [1,2,3] và [3,1,2] cho cùng key.
     * userId: "anonymous" nếu chưa đăng nhập.
     */
    String buildCacheKey(BookingCalculationDTO request) {
        String seatPart = request.getSeatIds() == null
                ? "[]"
                : request.getSeatIds().stream()
                        .sorted()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",", "[", "]"));

        String promoPart = (request.getPromoCode() != null && !request.getPromoCode().isBlank())
                ? request.getPromoCode().trim()
                : "none";

        return String.format("%s%d:%s:%s:%s",
                CACHE_KEY_PREFIX,
                request.getShowtimeId(),
                seatPart,
                resolveUserId(),
                promoPart);
    }

    String resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return String.valueOf(ud.getId());
        }
        return "anonymous";
    }
}
