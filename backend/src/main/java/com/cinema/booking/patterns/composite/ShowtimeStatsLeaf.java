package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ShowtimeStatsLeaf implements StatsComponent {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalShowtimes", showtimeRepository.count());
    }
}
