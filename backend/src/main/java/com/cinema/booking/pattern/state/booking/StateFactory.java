package com.cinema.booking.pattern.state.booking;
import com.cinema.booking.pattern.state.booking.CancelledState;
import com.cinema.booking.pattern.state.booking.ConfirmedState;
import com.cinema.booking.pattern.state.booking.PendingState;
import com.cinema.booking.pattern.state.booking.BookingState;

import com.cinema.booking.entity.Booking;

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
