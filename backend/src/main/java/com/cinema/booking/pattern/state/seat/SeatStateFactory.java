package com.cinema.booking.pattern.state.seat;

/**
 * Tạo đúng {@link SeatState} từ snapshot dữ liệu (đã bán trong DB vs có lock Redis).
 */
public final class SeatStateFactory {

    private SeatStateFactory() {
    }

    public static SeatState fromSnapshot(boolean soldInDatabase, boolean redisLockPresent) {
        if (soldInDatabase) {
            return SoldSeatState.INSTANCE;
        }
        if (redisLockPresent) {
            return PendingSeatState.INSTANCE;
        }
        return VacantSeatState.INSTANCE;
    }
}
