package com.cinema.booking.patterns.mediator;

import com.cinema.booking.repositories.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookingStatusUpdater implements PaymentColleague {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        context.getBooking().confirm();
        bookingRepository.save(context.getBooking());
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        context.getBooking().setStatus(com.cinema.booking.entities.Booking.BookingStatus.CANCELLED);
        bookingRepository.save(context.getBooking());
    }
}
