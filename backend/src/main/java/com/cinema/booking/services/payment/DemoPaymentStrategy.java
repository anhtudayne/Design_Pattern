package com.cinema.booking.services.payment;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.CheckoutResult;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Payment;
import com.cinema.booking.services.template_method.checkout.DemoCheckoutProcess;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DemoPaymentStrategy implements PaymentStrategy {

    private final DemoCheckoutProcess demoCheckoutProcess;

    public DemoPaymentStrategy(DemoCheckoutProcess demoCheckoutProcess) {
        this.demoCheckoutProcess = demoCheckoutProcess;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.DEMO;
    }

    @Override
    public CheckoutResult checkout(CheckoutRequest request) throws Exception {
        return demoCheckoutProcess.checkout(request);
    }

    public Map<String, Object> buildDemoResult(Booking booking, Payment payment, PriceBreakdownDTO price) {
        return demoCheckoutProcess.buildDemoResult(booking, payment, price);
    }
}
