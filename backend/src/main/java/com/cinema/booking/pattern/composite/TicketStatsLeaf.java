package com.cinema.booking.pattern.composite;
import com.cinema.booking.pattern.composite.TicketStatsLeaf;
import com.cinema.booking.pattern.composite.StatsComponent;

import com.cinema.booking.repository.TicketRepository;
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
