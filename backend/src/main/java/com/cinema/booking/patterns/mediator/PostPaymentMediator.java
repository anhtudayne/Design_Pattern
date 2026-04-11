package com.cinema.booking.patterns.mediator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PostPaymentMediator {

    private final List<PaymentColleague> colleagues;

    @Autowired
    public PostPaymentMediator(
            BookingStatusUpdater bookingStatusUpdater,
            UserSpendingUpdater userSpendingUpdater,
            TicketIssuer ticketIssuer,
            PaymentStatusUpdater paymentStatusUpdater,
            TicketEmailNotifier ticketEmailNotifier) {
        // Order of execution matters
        this.colleagues = Arrays.asList(
                bookingStatusUpdater,
                userSpendingUpdater,
                ticketIssuer,
                paymentStatusUpdater,
                ticketEmailNotifier
        );
    }

    public void settleSuccess(MomoCallbackContext context) {
        for (PaymentColleague colleague : colleagues) {
            colleague.onPaymentSuccess(context);
        }
    }

    public void settleFailure(MomoCallbackContext context) {
        for (PaymentColleague colleague : colleagues) {
            colleague.onPaymentFailure(context);
        }
    }
}
