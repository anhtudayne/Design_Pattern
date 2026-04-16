package com.cinema.booking.patterns.state;

import com.cinema.booking.entities.Booking;

public class StateFactory {
    public static BookingState getState(Booking.BookingStatus status) {
        if (status == null) return new PendingState();
        switch (status) {
            case PENDING: return new PendingState();
            case CONFIRMED: return new ConfirmedState();
            case CANCELLED: return new CancelledState();
            default: return new PendingState();
        }
    }
}
