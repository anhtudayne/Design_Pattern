package com.cinema.booking.pattern.composite;
import com.cinema.booking.pattern.composite.StatsComponent;

import java.util.Map;

/**
 * Composite pattern — Component interface.
 * Mỗi leaf hoặc composite ghi số liệu thống kê vào target map.
 */
public interface StatsComponent {
    void collect(Map<String, Object> target);
}
