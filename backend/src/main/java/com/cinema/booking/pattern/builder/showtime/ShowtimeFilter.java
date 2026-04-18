package com.cinema.booking.pattern.builder.showtime;
import com.cinema.booking.pattern.builder.showtime.ShowtimeFilterBuilder;
import com.cinema.booking.pattern.builder.showtime.ShowtimeFilter;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Product — Immutable filter object chứa tất cả tiêu chí lọc suất chiếu.
 * Được xây dựng bởi ShowtimeFilterBuilder (Builder Pattern).
 */
@Getter
public class ShowtimeFilter {
    private final Integer cinemaId;
    private final Integer movieId;
    private final Integer locationId;
    private final LocalDate date;
    private final String screenType;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;

    // Package-private constructor — chỉ Builder được tạo
    ShowtimeFilter(Integer cinemaId, Integer movieId, Integer locationId,
                   LocalDate date, String screenType,
                   BigDecimal minPrice, BigDecimal maxPrice) {
        this.cinemaId = cinemaId;
        this.movieId = movieId;
        this.locationId = locationId;
        this.date = date;
        this.screenType = screenType;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    /** Kiểm tra xem filter có tiêu chí nào hay không */
    public boolean hasAnyCriteria() {
        return cinemaId != null || movieId != null || locationId != null
                || date != null || screenType != null
                || minPrice != null || maxPrice != null;
    }
}
