package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserStatsLeaf implements StatsComponent {

    private final UserRepository userRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalUsers", userRepository.count());
    }
}
