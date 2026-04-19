package com.cinema.booking.patterns.state;

public class RefundedState implements BookingState {

    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã được hoàn tiền cho khách, không thể xác nhận lại!");
    }

    @Override
    public void cancel(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã được hoàn tiền, trạng thái hủy (Cancel) không còn hiệu lực!");
    }

    @Override
    public void printTickets(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã hoàn tiền, vé không còn giá trị sử dụng để in ra!");
    }

    @Override
    public void refund(BookingContext context) {
        throw new IllegalStateException("Hệ thống ghi nhận đơn hàng này đã được thực hiện giao dịch hoàn tiền!");
    }

    @Override
    public String getStateName() {
        return "REFUNDED";
    }
}
