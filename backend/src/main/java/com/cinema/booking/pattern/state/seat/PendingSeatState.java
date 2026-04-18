package com.cinema.booking.pattern.state.seat;
import com.cinema.booking.pattern.state.seat.PendingSeatState;
import com.cinema.booking.pattern.state.seat.SeatState;

import com.cinema.booking.dto.SeatStatusDTO;

public final class PendingSeatState implements SeatState {

    public static final PendingSeatState INSTANCE = new PendingSeatState();

    private PendingSeatState() {
    }

    @Override
    public SeatStatusDTO.SeatStatus toDisplayStatus() {
        return SeatStatusDTO.SeatStatus.PENDING;
    }

    @Override
    public boolean allowsLockAttempt() {
        return true;
    }
}
