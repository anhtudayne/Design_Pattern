package com.cinema.booking.patterns.composite;

import com.cinema.booking.entities.Payment;
import com.cinema.booking.repositories.PaymentRepository;
import com.cinema.booking.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RevenueStatsLeaf implements StatsComponent {

    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    @Override
    public void collect(Map<String, Object> target) {
        List<Payment> successfulPayments = paymentRepository.findByStatus(Payment.PaymentStatus.SUCCESS);

        BigDecimal revenue = successfulPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        target.put("totalRevenue", revenue);
        target.put("totalTickets", ticketRepository.count());
    }
}
