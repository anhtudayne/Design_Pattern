package com.cinema.booking.adapter.seatlock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter: ánh xạ {@link SeatLockProvider} sang Redis SETNX / delete / multiGet.
 */
@Component
@Slf4j
public class RedisSeatLockAdapter implements SeatLockProvider {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisSeatLockAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static String lockKey(Integer showtimeId, Integer seatId) {
        return "showtime:" + showtimeId + ":seat:" + seatId + ":lock";
    }

    @Override
    public boolean tryAcquire(Integer showtimeId, Integer seatId, Integer holderUserId, long ttlSeconds) {
        String key = lockKey(showtimeId, seatId);
        try {
            return Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(key, holderUserId, ttlSeconds, TimeUnit.SECONDS));
        } catch (Exception ex) {
            log.warn("Redis unavailable when trying to lock seat {} for showtime {}: {}",
                    seatId, showtimeId, ex.getMessage());
            return false;
        }
    }

    @Override
    public void release(Integer showtimeId, Integer seatId) {
        try {
            redisTemplate.delete(lockKey(showtimeId, seatId));
        } catch (Exception ex) {
            log.warn("Redis unavailable when releasing seat {} for showtime {}: {}",
                    seatId, showtimeId, ex.getMessage());
        }
    }

    @Override
    public List<Boolean> batchLockHeld(Integer showtimeId, List<Integer> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>(seatIds.size());
        for (Integer seatId : seatIds) {
            keys.add(lockKey(showtimeId, seatId));
        }
        List<Object> values;
        try {
            values = redisTemplate.opsForValue().multiGet(keys);
        } catch (Exception ex) {
            log.warn("Redis unavailable when reading seat locks for showtime {}: {}",
                    showtimeId, ex.getMessage());
            values = Collections.emptyList();
        }
        List<Boolean> held = new ArrayList<>(seatIds.size());
        for (int i = 0; i < seatIds.size(); i++) {
            held.add(values != null && i < values.size() && values.get(i) != null);
        }
        return held;
    }
}
