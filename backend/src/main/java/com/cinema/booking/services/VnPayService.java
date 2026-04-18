package com.cinema.booking.services;

import com.cinema.booking.dtos.VnpayPaymentResponse;

public interface VnPayService {

    VnpayPaymentResponse createPayment(String orderId, long amountVnd, String orderInfo, String extraData) throws Exception;
}
