package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromotionStatsLeaf implements StatsComponent {

    private final PromotionRepository promotionRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalPromotions", promotionRepository.count());
    }
}
