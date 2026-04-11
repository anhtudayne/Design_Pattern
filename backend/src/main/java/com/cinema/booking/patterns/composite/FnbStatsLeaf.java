package com.cinema.booking.patterns.composite;

import com.cinema.booking.repositories.FnbItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FnbStatsLeaf implements StatsComponent {

    @Autowired
    private FnbItemRepository fnbItemRepository;

    @Override
    public void collect(Map<String, Object> target) {
        target.put("totalFnbItems", fnbItemRepository.count());
    }
}
