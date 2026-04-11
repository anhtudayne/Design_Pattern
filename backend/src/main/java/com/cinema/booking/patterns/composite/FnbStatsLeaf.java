package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.FnbItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FnbStatsLeaf implements StatsComponent {

    private final FnbItemRepository fnbItemRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalFnbItems", fnbItemRepository.count());
    }
}
