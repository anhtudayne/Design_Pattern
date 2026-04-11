package com.cinema.booking.patterns.mediator;

import com.cinema.booking.entities.Payment;
import com.cinema.booking.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentStatusUpdater implements PaymentColleague {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        try {
            List<Payment> payments = paymentRepository.findByBookingAndPaymentMethodAndStatus(context.getBooking(), "MOMO", Payment.PaymentStatus.PENDING);
            
            Payment payment;
            if (!payments.isEmpty()) {
                payment = payments.get(payments.size() - 1);
            } else {
                payment = Payment.builder()
                        .booking(context.getBooking())
                        .paymentMethod("MOMO")
                        .amount(new BigDecimal(context.getCallback().getAmount()))
                        .build();
            }

            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println(">>> [StarCine] ERROR khi cập nhật Payment: " + e.getMessage());
        }
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        try {
            List<Payment> payments = paymentRepository.findByBookingAndPaymentMethodAndStatus(context.getBooking(), "MOMO", Payment.PaymentStatus.PENDING);
            
            if (!payments.isEmpty()) {
                Payment payment = payments.get(payments.size() - 1);
                payment.setStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
            }
        } catch (Exception e) {
            System.err.println(">>> [StarCine] ERROR khi cập nhật Payment FAILED: " + e.getMessage());
        }
    }
}
