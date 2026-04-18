package com.cinema.booking.pattern.state.booking;
import com.cinema.booking.pattern.state.booking.CancelledState;
import com.cinema.booking.pattern.state.booking.ConfirmedState;
import com.cinema.booking.pattern.state.booking.BookingContext;
import com.cinema.booking.pattern.state.booking.BookingState;

public class ConfirmedState implements BookingState {

    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã được xác nhận từ trước!");
    }

    @Override
    public void cancel(BookingContext context) {
        // Cho phép Hủy trực tiếp (tương đương Refund) để tối giản quy trình
        context.setState(new CancelledState());
    }

    @Override
    public void printTickets(BookingContext context) {
        // Assume logic for printing physical tickets (maybe status goes to printed/checked-in later but we keep it here as just an action)
        System.out.println("Printing tickets for booking " + context.getBooking().getBookingId());
    }

    @Override
    public void refund(BookingContext context) {
        context.setState(new CancelledState());
    }

    @Override
    public String getStateName() {
        return "CONFIRMED";
    }
}
