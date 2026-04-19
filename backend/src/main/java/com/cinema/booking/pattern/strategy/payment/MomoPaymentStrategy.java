package com.cinema.booking.pattern.strategy.payment;

import com.cinema.booking.dto.request.CheckoutRequest;
import com.cinema.booking.dto.response.CheckoutResult;
import com.cinema.booking.pattern.template.checkout.MomoCheckoutProcess;
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
