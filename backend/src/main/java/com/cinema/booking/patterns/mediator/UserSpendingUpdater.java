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
            safeIncreaseCustomerSpending(customer.getUserId(), paidAmount);
            System.out.println(">>> [StarCine] Đã cộng total_spending cho User #"
                    + customer.getUserId() + " thêm: " + paidAmount);
        }
    }

    @Override
    public void onPaymentFailure(MomoCallbackContext context) {
        // No spending update on failure
    }

    private void safeIncreaseCustomerSpending(Integer userId, BigDecimal amount) {
        RuntimeException last = null;
        for (int i = 0; i < MAX_DEADLOCK_RETRIES; i++) {
            try {
                customerRepository.increaseTotalSpending(userId, amount);
                return;
            } catch (RuntimeException ex) {
                last = ex;
                String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                if (!msg.contains("deadlock")) {
                    throw ex;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_BASE_MS * (i + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        throw last != null ? last : new RuntimeException("Không thể cập nhật tổng chi tiêu khách hàng");
    }
}
