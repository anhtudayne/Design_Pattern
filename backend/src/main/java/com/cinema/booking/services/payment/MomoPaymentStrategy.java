package com.cinema.booking.services.payment;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.CheckoutResult;
import com.cinema.booking.services.template_method.checkout.MomoCheckoutProcess;
import org.springframework.stereotype.Component;

@Component
public class MomoPaymentStrategy implements PaymentStrategy {

    private final MomoCheckoutProcess momoCheckoutProcess;

    public MomoPaymentStrategy(MomoCheckoutProcess momoCheckoutProcess) {
        this.momoCheckoutProcess = momoCheckoutProcess;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.MOMO;
    }

    @Override
    public CheckoutResult checkout(CheckoutRequest request) throws Exception {
        return momoCheckoutProcess.checkout(request);
    }
}
