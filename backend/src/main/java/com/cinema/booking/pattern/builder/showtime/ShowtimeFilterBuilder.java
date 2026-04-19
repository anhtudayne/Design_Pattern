package com.cinema.booking.pattern.builder.showtime;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Builder Pattern — Fluent API để xây dựng ShowtimeFilter từng bước.
 * Cho phép client xây dựng query filter phức tạp mà không cần constructor nhiều tham số.
 * 
 * Ví dụ sử dụng:
 *   ShowtimeFilter filter = new ShowtimeFilterBuilder()
 *       .byCinema(1)
 *       .byDate(LocalDate.now())
 *       .byScreenType("IMAX")
 *       .build();
 */
public class ShowtimeFilterBuilder {
    private Integer cinemaId;
    private Integer movieId;
    private Integer locationId;
    private LocalDate date;
    private String screenType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    public ShowtimeFilterBuilder byCinema(Integer cinemaId) {
        this.cinemaId = cinemaId;
        return this;
    }

    public ShowtimeFilterBuilder byMovie(Integer movieId) {
        this.movieId = movieId;
        return this;
    }

    public ShowtimeFilterBuilder byLocation(Integer locationId) {
        this.locationId = locationId;
        return this;
    }

    public ShowtimeFilterBuilder byDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public ShowtimeFilterBuilder byScreenType(String screenType) {
        if (screenType != null && !screenType.isBlank()) {
            this.screenType = screenType;
        }
        return this;
    }

    public ShowtimeFilterBuilder byPriceRange(BigDecimal min, BigDecimal max) {
        this.minPrice = min;
        this.maxPrice = max;
        return this;
    }

    public ShowtimeFilter build() {
        return new ShowtimeFilter(cinemaId, movieId, locationId, date, screenType, minPrice, maxPrice);
    }
}
