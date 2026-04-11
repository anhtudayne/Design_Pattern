package com.cinema.booking.patterns.specification;

import com.cinema.booking.entities.Showtime;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Specification pattern — static factory methods cho từng điều kiện lọc Showtime.
 * Kết hợp các predicate qua Specification.where(...).and(...).
 * Final class, private constructor — không cho phép instantiate.
 */
public final class ShowtimeSpecifications {

    private ShowtimeSpecifications() { /* static utils only */ }

    /**
     * Lọc theo cinemaId — join Showtime → room → cinema.
     */
    public static Specification<Showtime> hasCinemaId(Integer cinemaId) {
        return (root, query, cb) -> {
            if (cinemaId == null) return cb.conjunction();
            return cb.equal(root.get("room").get("cinema").get("cinemaId"), cinemaId);
        };
    }

    /**
     * Lọc theo movieId — join Showtime → movie.
     */
    public static Specification<Showtime> hasMovieId(Integer movieId) {
        return (root, query, cb) -> {
            if (movieId == null) return cb.conjunction();
            return cb.equal(root.get("movie").get("movieId"), movieId);
        };
    }

    /**
     * Lọc theo ngày chiếu — startTime trong khoảng [date 00:00, date+1 00:00).
     */
    public static Specification<Showtime> onDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return cb.conjunction();
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end   = date.plusDays(1).atStartOfDay();
            return cb.and(
                cb.greaterThanOrEqualTo(root.get("startTime"), start),
                cb.lessThan(root.get("startTime"), end)
            );
        };
    }
}
