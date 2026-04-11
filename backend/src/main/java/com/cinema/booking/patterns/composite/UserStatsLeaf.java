package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserStatsLeaf implements StatsComponent {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalUsers", userRepository.count());
    }
}
