package com.cinema.booking.pattern.strategy.payment;

import com.cinema.booking.dto.request.CheckoutRequest;
import com.cinema.booking.dto.response.CheckoutResult;
import com.cinema.booking.pattern.template.checkout.VnpayCheckoutProcess;
import org.springframework.stereotype.Component;

@Component
public class VnpayPaymentStrategy implements PaymentStrategy {

    private final VnpayCheckoutProcess vnpayCheckoutProcess;

    public VnpayPaymentStrategy(VnpayCheckoutProcess vnpayCheckoutProcess) {
        this.vnpayCheckoutProcess = vnpayCheckoutProcess;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public CheckoutResult checkout(CheckoutRequest request) throws Exception {
        return vnpayCheckoutProcess.checkout(request);
    }
}
