package com.cinema.booking.pattern.composite;

import com.cinema.booking.repository.FnbItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Leaf: đếm món F&B trong menu. */
@Component
@RequiredArgsConstructor
public class FnbStatsLeaf implements StatsComponent {

    private final FnbItemRepository fnbItemRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalFnbItems", fnbItemRepository.count());
    }
}
