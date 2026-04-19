package com.cinema.booking.pattern.state.seat;

import com.cinema.booking.dto.SeatStatusDTO;

public final class VacantSeatState implements SeatState {

    public static final VacantSeatState INSTANCE = new VacantSeatState();

    private VacantSeatState() {
    }

    @Override
    public SeatStatusDTO.SeatStatus toDisplayStatus() {
        return SeatStatusDTO.SeatStatus.VACANT;
    }

    @Override
    public boolean allowsLockAttempt() {
        return true;
    }
}
