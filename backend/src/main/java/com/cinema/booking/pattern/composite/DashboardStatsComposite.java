package com.cinema.booking.pattern.composite;
import com.cinema.booking.pattern.composite.DashboardStatsComposite;
import com.cinema.booking.pattern.composite.StatsComponent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Composite — orchestrates all stat leaves.
 * Controller calls collect() once and receives a fully populated stats map.
 * 
 * Open/Closed Principle: New stat leaves are automatically picked up via
 * dependency injection without modifying this class.
 */
@Component
public class DashboardStatsComposite implements StatsComponent {

    private final List<StatsComponent> children;

    @Autowired
    public DashboardStatsComposite(List<StatsComponent> allComponents) {
        // Filter out self to avoid infinite recursion
        this.children = allComponents.stream()
                .filter(c -> !(c instanceof DashboardStatsComposite))
                .collect(Collectors.toList());
    }

    @Override
    public void collect(Map<String, Object> target) {
        for (StatsComponent child : children) {
            child.collect(target);
        }
    }
}
