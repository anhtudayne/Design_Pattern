package com.cinema.booking.services.payment;

import com.cinema.booking.dtos.CheckoutRequest;
import com.cinema.booking.dtos.CheckoutResult;
import com.cinema.booking.dtos.PriceBreakdownDTO;
import com.cinema.booking.entities.Booking;
import com.cinema.booking.entities.Payment;
import com.cinema.booking.services.template_method.checkout.StaffCashCheckoutProcess;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Strategy Pattern: Chiến lược thanh toán tiền mặt tại quầy bán vé Staff.
 * Delegate toàn bộ logic xuống {@link StaffCashCheckoutProcess}.
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
