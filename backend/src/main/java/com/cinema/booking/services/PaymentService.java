package com.cinema.booking.services;

import com.cinema.booking.dtos.PaymentHistoryDTO;
import com.cinema.booking.entities.Payment;
import java.util.List;

public interface PaymentService {
    List<PaymentHistoryDTO> getUserPaymentHistory(Integer userId);
    Payment getPaymentDetails(Integer paymentId);
}
