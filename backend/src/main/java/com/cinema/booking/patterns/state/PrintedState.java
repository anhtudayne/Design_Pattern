package com.cinema.booking.patterns.state;

public class PrintedState implements BookingState {

    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Vé thực tế đã được in ra cho đơn hàng này!");
    }

    @Override
    public void cancel(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã in vé cứng, không cho phép hủy hệ thống!");
    }

    @Override
    public void printTickets(BookingContext context) {
        throw new IllegalStateException("Vé cho đơn hàng này đã được in ra giấy từ trước.");
    }

    @Override
    public void refund(BookingContext context) {
        throw new IllegalStateException("Theo quy định của rạp, vé cứng đã được in ra và giao cho khách thì KHÔNG được phép hoàn lại tiền!");
    }

    @Override
    public String getStateName() {
        return "PRINTED";
    }
}
