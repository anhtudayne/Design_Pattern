package com.cinema.booking.adapter.seatlock;
import com.cinema.booking.adapter.seatlock.SeatLockProvider;

import java.util.List;

/**
 * Adapter target: nghiệp vụ ghế chỉ phụ thuộc interface này, không phụ thuộc Redis cụ thể.
 */
public interface SeatLockProvider {

    boolean tryAcquire(Integer showtimeId, Integer seatId, Integer holderUserId, long ttlSeconds);

    void release(Integer showtimeId, Integer seatId);

    /**
     * @return danh sách {@code true} nếu ghế tương ứng trong {@code seatIds} đang có lock, cùng thứ tự input
     */
    List<Boolean> batchLockHeld(Integer showtimeId, List<Integer> seatIds);
}
