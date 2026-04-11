package com.cinema.booking.patterns.composite;

import com.cinema.booking.entities.Payment;
import com.cinema.booking.repositories.PaymentRepository;
import com.cinema.booking.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class RevenueStatsLeaf implements StatsComponent {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public void collect(Map<String, Object> target) {
        // Filter SUCCESS payments — keep same logic as the original controller
        List<Payment> successfulPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS)
                .toList();

        BigDecimal revenue = successfulPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        target.put("totalRevenue", revenue);
        target.put("totalTickets", ticketRepository.count());
    }
}
