package com.cinema.booking.domain.seat;

import com.cinema.booking.dtos.SeatStatusDTO;

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
