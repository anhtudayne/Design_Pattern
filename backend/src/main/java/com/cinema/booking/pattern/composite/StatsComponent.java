package com.cinema.booking.pattern.composite;

import java.util.Map;

/**
 * Thành phần composite (dashboard): leaf hoặc composite đều ghi số liệu vào map đích.
 */
public interface StatsComponent {
    void collect(Map<String, Object> target);
}
