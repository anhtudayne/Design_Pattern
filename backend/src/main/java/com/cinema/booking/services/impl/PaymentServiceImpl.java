package com.cinema.booking.services.impl;

import com.cinema.booking.entities.Payment;
import com.cinema.booking.repositories.PaymentRepository;
import com.cinema.booking.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public List<Payment> getUserPaymentHistory(Integer userId) {
        return paymentRepository.findUserPaymentHistory(userId);
    }

    @Override
    public Payment getPaymentDetails(Integer paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tồn tại với ID: " + paymentId));
    }
}
