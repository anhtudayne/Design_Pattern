package com.cinema.booking.service;

import com.cinema.booking.dto.request.MomoCallbackRequest;
import com.cinema.booking.dto.MomoPaymentResponse;

public interface MomoService {

    MomoPaymentResponse createPayment(String orderId, long amountVnd, String orderInfo, String extraData) throws Exception;

    boolean verifySignature(MomoCallbackRequest callback);
}
