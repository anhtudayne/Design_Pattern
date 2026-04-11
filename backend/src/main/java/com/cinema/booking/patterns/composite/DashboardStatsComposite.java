package com.cinema.booking.patterns.composite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Composite — orchestrates all stat leaves.
 * Controller calls collect() once and receives a fully populated stats map.
 */
@Component
public class DashboardStatsComposite implements StatsComponent {

    private final List<StatsComponent> children;

    @Autowired
    public DashboardStatsComposite(
            MovieStatsLeaf movieStatsLeaf,
            UserStatsLeaf userStatsLeaf,
            ShowtimeStatsLeaf showtimeStatsLeaf,
            FnbStatsLeaf fnbStatsLeaf,
            VoucherStatsLeaf voucherStatsLeaf,
            RevenueStatsLeaf revenueStatsLeaf) {
        this.children = Arrays.asList(
                movieStatsLeaf,
                userStatsLeaf,
                showtimeStatsLeaf,
                fnbStatsLeaf,
                voucherStatsLeaf,
                revenueStatsLeaf
        );
    }

    @Override
    public void collect(Map<String, Object> target) {
        for (StatsComponent child : children) {
            child.collect(target);
        }
    }
}
