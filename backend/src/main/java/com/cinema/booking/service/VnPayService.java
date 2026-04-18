package com.cinema.booking.service;

import com.cinema.booking.dto.response.VnpayPaymentResponse;

public interface VnPayService {

    VnpayPaymentResponse createPayment(String orderId, long amountVnd, String orderInfo, String extraData) throws Exception;
}
