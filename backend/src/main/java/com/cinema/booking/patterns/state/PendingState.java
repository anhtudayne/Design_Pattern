package com.cinema.booking.patterns.state;

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
        throw new IllegalStateException("Đơn hàng chưa được thanh toán nên không thể thực hiện in vé!");
    }

    @Override
    public void refund(BookingContext context) {
        throw new IllegalStateException("Đơn hàng chưa thanh toán, không có khoản tiền nào để hoàn lại!");
    }


    @Override
    public String getStateName() {
        return "PENDING";
    }
}
