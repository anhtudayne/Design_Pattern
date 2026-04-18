package com.cinema.booking.service;

import com.cinema.booking.dto.PaymentHistoryDTO;
import com.cinema.booking.entity.Payment;
import java.util.List;

public interface PaymentService {
    List<PaymentHistoryDTO> getUserPaymentHistory(Integer userId);
    Payment getPaymentDetails(Integer paymentId);
}
