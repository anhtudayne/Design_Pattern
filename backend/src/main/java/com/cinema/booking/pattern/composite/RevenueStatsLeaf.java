package com.cinema.booking.pattern.composite;
import com.cinema.booking.pattern.composite.RevenueStatsLeaf;
import com.cinema.booking.pattern.composite.StatsComponent;

import com.cinema.booking.entity.Payment;
import com.cinema.booking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Leaf component responsible for collecting revenue statistics.
 * Single Responsibility: Only handles total revenue calculation from successful payments.
 */
@Component
@RequiredArgsConstructor
public class RevenueStatsLeaf implements StatsComponent {

    private final PaymentRepository paymentRepository;

    @Override
    public void collect(Map<String, Object> target) {
        List<Payment> successfulPayments = paymentRepository.findByStatus(Payment.PaymentStatus.SUCCESS);

        BigDecimal revenue = successfulPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        target.put("totalRevenue", revenue);
    }
}
