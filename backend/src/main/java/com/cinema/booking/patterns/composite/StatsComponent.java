package com.cinema.booking.patterns.composite;

import java.util.Map;

/**
 * Composite pattern — Component interface.
 * Mỗi leaf hoặc composite ghi số liệu thống kê vào target map.
 */
public interface StatsComponent {
    void collect(Map<String, Object> target);
}
