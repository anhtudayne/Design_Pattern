package com.cinema.booking.patterns.mediator;

import com.cinema.booking.entities.Customer;
import com.cinema.booking.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserSpendingUpdater implements PaymentColleague {

    private static final int MAX_DEADLOCK_RETRIES = 3;
    private static final long RETRY_BASE_MS = 120L;

    private final CustomerRepository customerRepository;

    @Override
    public void onPaymentSuccess(MomoCallbackContext context) {
        Customer customer = context.getBooking().getCustomer();
        if (customer != null) {
            BigDecimal paidAmount = new BigDecimal(context.getCallback().getAmount());
            System.out.println(">>> [StarCine] Đã ghi nhận User #"
                    + customer.getUserId() + " có phát sinh giao dịch.");
        }
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        // No spending update on failure
    }

}
