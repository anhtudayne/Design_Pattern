package com.cinema.booking.pattern.state.seat;
import com.cinema.booking.pattern.state.seat.SoldSeatState;
import com.cinema.booking.pattern.state.seat.SeatState;

import com.cinema.booking.dto.SeatStatusDTO;

public final class SoldSeatState implements SeatState {

    public static final SoldSeatState INSTANCE = new SoldSeatState();

    private SoldSeatState() {
    }

    @Override
    public SeatStatusDTO.SeatStatus toDisplayStatus() {
        return SeatStatusDTO.SeatStatus.SOLD;
    }

    @Override
    public boolean allowsLockAttempt() {
        return false;
    }
}
