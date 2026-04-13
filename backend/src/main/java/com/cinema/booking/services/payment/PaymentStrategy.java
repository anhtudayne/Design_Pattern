package com.cinema.booking.services.payment;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.CheckoutResult;

/**
 * Strategy: mỗi kênh thanh toán đóng gói luồng checkout (delegate tới {@link com.cinema.booking.services.template_method.checkout.AbstractCheckoutTemplate}).
 */
public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    CheckoutResult checkout(CheckoutRequest request) throws Exception;
}
