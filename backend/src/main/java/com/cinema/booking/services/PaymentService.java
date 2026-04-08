package com.cinema.booking.services;

import com.cinema.booking.entities.Payment;
import java.util.List;

public interface PaymentService {
    List<Payment> getUserPaymentHistory(Integer userId);
    Payment getPaymentDetails(Integer paymentId);
}
