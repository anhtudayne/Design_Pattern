package com.cinema.booking.pattern.strategy.payment;

import com.cinema.booking.dto.request.CheckoutRequest;
import com.cinema.booking.dto.response.CheckoutResult;

/**
 * Strategy: mỗi kênh thanh toán đóng gói luồng checkout (delegate tới {@link com.cinema.booking.pattern.template.checkout.AbstractCheckoutTemplate}).
 */
public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    CheckoutResult checkout(CheckoutRequest request) throws Exception;
}
