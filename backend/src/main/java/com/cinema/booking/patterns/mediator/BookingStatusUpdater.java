package com.cinema.booking.patterns.mediator;

import com.cinema.booking.entities.Booking;
import com.cinema.booking.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingStatusUpdater implements PaymentColleague {

    private final BookingRepository bookingRepository;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        context.getBooking().confirm();
        bookingRepository.save(context.getBooking());
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        context.getBooking().setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(context.getBooking());
    }
}
