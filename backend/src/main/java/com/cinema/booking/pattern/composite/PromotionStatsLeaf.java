package com.cinema.booking.pattern.composite;

import com.cinema.booking.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Leaf: đếm tổng khuyến mãi. */
@Component
@RequiredArgsConstructor
public class PromotionStatsLeaf implements StatsComponent {

    private final PromotionRepository promotionRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalPromotions", promotionRepository.count());
    }
}
