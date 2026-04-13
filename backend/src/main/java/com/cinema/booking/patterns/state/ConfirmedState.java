package com.cinema.booking.patterns.state;

public class ConfirmedState implements BookingState {

    @Override
    public void confirm(BookingContext context) {
        throw new IllegalStateException("Đơn hàng đã được xác nhận từ trước!");
    }

    @Override
    public void cancel(BookingContext context) {
        throw new IllegalStateException("Không thể Hủy vé theo quy trình thông thường đối với vé đã thanh toán. Vui lòng Refund hoặc kiểm tra policy.");
    }

    @Override
    public void printTickets(BookingContext context) {
        // Assume logic for printing physical tickets (maybe status goes to printed/checked-in later but we keep it here as just an action)
        System.out.println("Printing tickets for booking " + context.getBooking().getBookingId());
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
