package com.cinema.booking.patterns.observer;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Observer Pattern — Event Object.
 *
 * Payload được phát đi mỗi khi trạng thái ghế thay đổi (lock / unlock / sold / released).
 * Class bất biến (chỉ có getters, không setter), được serialize thành JSON và push qua WebSocket.
 */
@Data
@AllArgsConstructor
public class SeatStatusEvent {

    /** ID suất chiếu — dùng để đăng ký topic WebSocket: /topic/seats/{showtimeId} */
    private Integer showtimeId;

    /** ID ghế cụ thể bị thay đổi trạng thái */
    private Integer seatId;

    /** Mã ghế hiển thị (ví dụ: "C3") để Frontend cập nhật UI ngay không cần query thêm */
    private String seatCode;

    /**
     * Trạng thái mới của ghế:
     * <ul>
     *   <li>{@code "PENDING"}   — Đang được giữ bởi một Staff (lock Redis)</li>
     *   <li>{@code "AVAILABLE"} — Ghế vừa được giải phóng (unlock hoặc cancel/refund)</li>
     *   <li>{@code "SOLD"}      — Đã thanh toán, không thể đặt nữa</li>
     * </ul>
     */
    private String status;

    /** User ID của người trigger event (null nếu do hệ thống tự động, ví dụ: TTL expire) */
    private Integer triggeredByUserId;
}
