package com.cinema.booking.patterns.mediator;

import com.cinema.booking.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TicketEmailNotifier implements PaymentColleague {

    @Autowired
    private EmailService emailService;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        try {
            System.out.println(">>> [StarCine] Đang gửi Email vé trực tiếp...");
            emailService.sendTicketEmail(context.getBooking().getBookingId());
        } catch (Exception e) {
            System.err.println(">>> [StarCine] ERROR khi gửi Email: " + e.getMessage());
        }
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        // Do nothing
    }
}
