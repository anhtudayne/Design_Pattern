package com.cinema.booking.services;

import com.cinema.booking.dtos.MomoCallbackRequest;
import com.cinema.booking.dtos.MomoPaymentResponse;

public interface MomoService {

    MomoPaymentResponse createPayment(String orderId, long amountVnd, String orderInfo, String extraData) throws Exception;

    boolean verifySignature(MomoCallbackRequest callback);
}
