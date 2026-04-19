package com.cinema.booking.patterns.observer;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Observer Pattern — Event Object.
 *
 * Payload được phát đi mỗi khi trạng thái ghế thay đổi (lock / unlock / sold /
 * released).
 * Class bất biến (chỉ có getters, không setter), được serialize thành JSON và
 * push qua WebSocket.
 */
@Data
@AllArgsConstructor
public class SeatStatusEvent {
    private Integer showtimeId;
    private Integer seatId;
    private String seatCode;

    /**
     * Trạng thái mới của ghế:
     * <ul>
     * <li>{@code "PENDING"} — Đang được giữ bởi một Staff (lock Redis)</li>
     * <li>{@code "VACANT"} — Ghế vừa được giải phóng (unlock hoặc
     * cancel/refund)</li>
     * <li>{@code "SOLD"} — Đã thanh toán, không thể đặt nữa</li>
     * </ul>
     */
    private String status;

    /**
     * User ID của người trigger event (null nếu do hệ thống tự động, ví dụ: TTL
     * expire)
     */
    private Integer triggeredByUserId;
}
