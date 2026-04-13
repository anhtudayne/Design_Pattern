package com.cinema.booking.patterns.state;

public interface BookingState {
    void confirm(BookingContext context);
    void cancel(BookingContext context);
    void printTickets(BookingContext context);
    void refund(BookingContext context);
    
    // Default method to return string representation
    String getStateName();
}
