package com.cinema.booking.pattern.composite;
import com.cinema.booking.pattern.composite.ShowtimeStatsLeaf;
import com.cinema.booking.pattern.composite.StatsComponent;

import com.cinema.booking.repository.ShowtimeRepository;
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
