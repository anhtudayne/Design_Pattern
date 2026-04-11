package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MovieStatsLeaf implements StatsComponent {

    @Autowired
    private MovieRepository movieRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalMovies", movieRepository.count());
    }
}
