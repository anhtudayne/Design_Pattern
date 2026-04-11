package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShowtimeStatsLeaf implements StatsComponent {

    private final ShowtimeRepository showtimeRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalShowtimes", showtimeRepository.count());
    }
}
