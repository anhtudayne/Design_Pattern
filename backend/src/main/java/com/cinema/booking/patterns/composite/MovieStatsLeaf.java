package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MovieStatsLeaf implements StatsComponent {

    private final MovieRepository movieRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalMovies", movieRepository.count());
    }
}
