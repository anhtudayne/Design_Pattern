package com.cinema.booking.services.seatlock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter: ánh xạ {@link SeatLockProvider} sang Redis SETNX / delete / multiGet.
 */
@Component
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
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(key, holderUserId, ttlSeconds, TimeUnit.SECONDS));
    }

    @Override
    public void release(Integer showtimeId, Integer seatId) {
        redisTemplate.delete(lockKey(showtimeId, seatId));
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
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        List<Boolean> held = new ArrayList<>(seatIds.size());
        for (int i = 0; i < seatIds.size(); i++) {
            held.add(values != null && i < values.size() && values.get(i) != null);
        }
        return held;
    }
}
