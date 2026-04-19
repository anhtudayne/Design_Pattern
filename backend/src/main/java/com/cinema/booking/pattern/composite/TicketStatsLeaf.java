package com.cinema.booking.pattern.composite;

import com.cinema.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Leaf: đếm tổng số vé. */
@Component
@RequiredArgsConstructor
public class TicketStatsLeaf implements StatsComponent {

    private final TicketRepository ticketRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalTickets", ticketRepository.count());
    }
}
