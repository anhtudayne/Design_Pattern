package com.cinema.booking.patterns.state;

public class CancelledState implements BookingState {

    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã hủy, không thể xác nhận.");
    }

    @Override
    public void cancel(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã ở trạng thái hủy.");
    }

    @Override
    public void printTickets(BookingContext context) {
        throw new IllegalStateException("Không thể in vé cho đơn hàng đã hủy.");
    }

    @Override
    public void refund(BookingContext context) {
        throw new IllegalStateException("Đơn hàng này đã bị hủy do chưa thanh toán, không có chi phí nào để hoàn.");
    }


    @Override
    public String getStateName() {
        return "CANCELLED";
    }
}
