package com.cinema.booking.pattern.state.booking;

public class PendingState implements BookingState {

    @Override
    public void confirm(BookingContext context) {
        context.setState(new ConfirmedState());
    }

    @Override
    public void cancel(BookingContext context) {
        context.setState(new CancelledState());
    }

    @Override
    public void printTickets(BookingContext context) {
        throw new IllegalStateException("Không thể in vé do đơn hàng chưa được thanh toán!");
    }

    @Override
    public void refund(BookingContext context) {
        throw new IllegalStateException("Không thể hoàn tiền do đơn hàng chưa thanh toán!");
    }

    @Override
    public String getStateName() {
        return "PENDING";
    }
}
