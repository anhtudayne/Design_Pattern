package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Leaf component responsible for collecting ticket statistics.
 * Single Responsibility: Only handles total ticket count.
 */
@Component
@RequiredArgsConstructor
public class TicketStatsLeaf implements StatsComponent {

    private final TicketRepository ticketRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalTickets", ticketRepository.count());
    }
}
