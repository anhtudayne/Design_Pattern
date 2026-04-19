package com.cinema.booking.pattern.composite;

import com.cinema.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Leaf: đếm tổng người dùng. */
@Component
@RequiredArgsConstructor
public class UserStatsLeaf implements StatsComponent {

    private final UserRepository userRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalUsers", userRepository.count());
    }
}
