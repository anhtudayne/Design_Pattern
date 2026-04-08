package com.cinema.booking.services;

import com.cinema.booking.dtos.MomoPaymentResponse;

public interface MomoService {
    MomoPaymentResponse createPayment(String orderId, long amount, String orderInfo, String extraData) throws Exception;
    boolean verifySignature(Object callbackRequest) throws Exception;
}
