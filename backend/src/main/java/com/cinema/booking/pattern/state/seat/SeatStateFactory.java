package com.cinema.booking.pattern.state.seat;
import com.cinema.booking.pattern.state.seat.SeatStateFactory;
import com.cinema.booking.pattern.state.seat.SoldSeatState;
import com.cinema.booking.pattern.state.seat.PendingSeatState;
import com.cinema.booking.pattern.state.seat.VacantSeatState;
import com.cinema.booking.pattern.state.seat.SeatState;

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
