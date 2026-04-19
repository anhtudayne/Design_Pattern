package com.cinema.booking.pattern.state.booking;

public class RefundedState implements BookingState {

    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã được hoàn tiền, không thể xác nhận lại.");
    }

    @Override
    public void cancel(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã hoàn tiền, không thể chuyển thành hủy (chưa thanh toán).");
    }

    @Override
    public void printTickets(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã hoàn tiền, vé không còn giá trị sử dụng.");
    }

    @Override
    public void refund(BookingContext context) {
        throw new IllegalStateException("Đơn hàng này đã được hoàn tiền từ trước.");
    }

    @Override
    public String getStateName() {
        return "REFUNDED";
    }
}
