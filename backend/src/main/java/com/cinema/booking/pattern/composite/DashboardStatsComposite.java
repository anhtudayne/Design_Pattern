package com.cinema.booking.pattern.composite;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DashboardStatsComposite implements StatsComponent {

    private final List<StatsComponent> children;

    public DashboardStatsComposite(List<StatsComponent> allComponents) {
        // Loại bỏ chính bean này để tránh đệ quy
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
