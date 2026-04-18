package com.cinema.booking.pattern.composite;
import com.cinema.booking.pattern.composite.MovieStatsLeaf;
import com.cinema.booking.pattern.composite.StatsComponent;

import com.cinema.booking.repository.MovieRepository;
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
