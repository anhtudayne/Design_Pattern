package com.cinema.booking.pattern.state.booking;
import com.cinema.booking.pattern.state.booking.BookingContext;
import com.cinema.booking.pattern.state.booking.BookingState;

public interface BookingState {
    void confirm(BookingContext context);
    void cancel(BookingContext context);
    void printTickets(BookingContext context);
    void refund(BookingContext context);
    
    // Default method to return string representation
    String getStateName();
}
