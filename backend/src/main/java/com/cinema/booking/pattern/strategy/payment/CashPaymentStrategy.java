package com.cinema.booking.pattern.strategy.payment;

import com.cinema.booking.dto.request.CheckoutRequest;
import com.cinema.booking.dto.response.CheckoutResult;
import com.cinema.booking.dto.PriceBreakdownDTO;
import com.cinema.booking.entity.Booking;
import com.cinema.booking.entity.Payment;
import com.cinema.booking.pattern.template.checkout.StaffCashCheckoutProcess;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Strategy Pattern: thanh toán tiền mặt (quầy Staff hoặc khách web qua {@code /api/payment/checkout/cash}).
 * Delegate xuống {@link StaffCashCheckoutProcess} (Template Method).
 */
@Component
public class CashPaymentStrategy implements PaymentStrategy {

    private final StaffCashCheckoutProcess cashCheckoutProcess;

    public CashPaymentStrategy(StaffCashCheckoutProcess cashCheckoutProcess) {
        this.cashCheckoutProcess = cashCheckoutProcess;
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public CheckoutResult checkout(CheckoutRequest request) throws Exception {
        return cashCheckoutProcess.checkout(request);
    }

    public Map<String, Object> buildResult(Booking booking, Payment payment, PriceBreakdownDTO price) {
        return cashCheckoutProcess.buildResult(booking, payment, price);
    }
}
