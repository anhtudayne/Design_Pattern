package com.cinema.booking.pattern.proxy;

import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.pattern.strategy.pricing.PricingContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Primary
@Component("cachingPricingEngineProxy")
public class CachingPricingEngineProxy implements IPricingEngine {

    private static final String KEY_PREFIX = "pricing:";

    private final IPricingEngine delegate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long ttlSeconds;

    public CachingPricingEngineProxy(
            @Qualifier("pricingEngine") IPricingEngine delegate,
            RedisTemplate<String, Object> redisTemplate,
            @Value("${cinema.app.redisTtlSeconds:600}") long ttlSeconds) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public PriceBreakdownDTO calculateTotalPrice(PricingContext context) {
        String cacheKey = buildCacheKey(context);

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PriceBreakdownDTO dto) {
            return dto;
        }

        PriceBreakdownDTO result = delegate.calculateTotalPrice(context);
        redisTemplate.opsForValue().set(cacheKey, result, ttlSeconds, TimeUnit.SECONDS);
        return result;
    }

    private String buildCacheKey(PricingContext context) {
        Integer showtimeId = context.getShowtime() != null ? context.getShowtime().getShowtimeId() : 0;

        String seatPart = "seats:none";
        if (context.getSeats() != null && !context.getSeats().isEmpty()) {
            List<Integer> sortedIds = context.getSeats().stream()
                    .map(s -> s.getSeatId())
                    .sorted()
                    .collect(Collectors.toList());
            seatPart = "seats:" + sortedIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }

        String promoPart = (context.getPromotion() != null && context.getPromotion().getCode() != null)
                ? "promo:" + context.getPromotion().getCode()
                : "promo:none";

        String fnbPart = "fnb:none";
        if (context.getFnbItems() != null && !context.getFnbItems().isEmpty()) {
            List<PricingContext.FnbItemQuantity> sorted = context.getFnbItems().stream()
                    .sorted(Comparator.comparing(i -> i.getFnbItem().getFnbItemId()))
                    .collect(Collectors.toList());
            fnbPart = "fnb:" + sorted.stream()
                    .map(i -> i.getFnbItem().getFnbItemId() + ":" + i.getQuantity())
                    .collect(Collectors.joining(","));
        }

        return KEY_PREFIX + showtimeId + ":" + seatPart + ":" + fnbPart + ":" + promoPart;
    }
}
