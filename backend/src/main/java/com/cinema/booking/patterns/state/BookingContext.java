package com.cinema.booking.patterns.state;

import com.cinema.booking.entities.Booking;
import lombok.Getter;

@Getter
public class BookingContext {
    private BookingState state;
    private final Booking booking;

    public BookingContext(Booking booking) {
        this.booking = booking;
        this.state = StateFactory.getState(booking.getStatus());
    }

    public void setState(BookingState state) {
        this.state = state;
        // Keep entity in sync
        this.booking.setStatus(Booking.BookingStatus.valueOf(state.getStateName()));
    }

    public void confirm() {
        state.confirm(this);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void printTickets() {
        state.printTickets(this);
    }

    public void refund() {
        state.refund(this);
    }

}
