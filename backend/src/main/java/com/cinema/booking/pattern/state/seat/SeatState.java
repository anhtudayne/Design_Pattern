package com.cinema.booking.pattern.state.seat;
import com.cinema.booking.pattern.state.seat.SeatState;
import com.cinema.booking.adapter.seatlock.SeatLockProvider;

import com.cinema.booking.dto.SeatStatusDTO;

/**
 * State: hành vi / quy tắc hiển thị và thao tác khóa theo trạng thái nghiệp vụ của ghế (trống / đang giữ / đã bán).
 */
public interface SeatState {

    SeatStatusDTO.SeatStatus toDisplayStatus();

    /**
     * Ghế đã bán thì không được thử giữ tạm; còn lại vẫn có thể gọi {@link com.cinema.booking.services.seatlock.SeatLockProvider#tryAcquire}
     * (SETNX sẽ thất bại nếu người khác đang giữ).
     */
    boolean allowsLockAttempt();
}
