package com.cinema.booking.domain.seat;

import com.cinema.booking.dtos.SeatStatusDTO;

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
