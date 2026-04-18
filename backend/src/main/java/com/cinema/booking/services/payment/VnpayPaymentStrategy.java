package com.cinema.booking.services.payment;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.CheckoutResult;
import com.cinema.booking.services.template_method.checkout.VnpayCheckoutProcess;
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
