package com.cinema.booking.patterns.state;

public class ConfirmedState implements BookingState {

    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã được xác nhận từ trước!");
    }

    @Override
    public void cancel(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã được thanh toán, bạn phải dùng chức năng Hoàn Tiền (Refund) để kế toán kiểm soát dòng tiền!");
    }

    @Override
    public void printTickets(BookingContext context) {
        System.out.println("Printing tickets for booking " + context.getBooking().getBookingId());
        context.setState(new PrintedState());
    }

    @Override
    public void refund(BookingContext context) {
        context.setState(new RefundedState());
    }


    @Override
    public String getStateName() {
        return "CONFIRMED";
    }
}
