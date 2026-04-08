package com.cinema.booking.dtos;

import com.cinema.booking.entities.Movie.AgeRating;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShowtimeDTO {
    private Integer showtimeId;

    @NotNull(message = "ID phim là bắt buộc")
    private Integer movieId;

    @NotNull(message = "ID phòng chiếu là bắt buộc")
    private Integer roomId;

    @NotNull(message = "Thời gian bắt đầu là bắt buộc")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @NotNull(message = "Giá vé cơ bản là bắt buộc")
    private BigDecimal basePrice;

    private BigDecimal surcharge;

    // ── Enriched fields (read-only, populated by service) ──
    private String movieTitle;
    private String moviePosterUrl;
    private AgeRating movieAgeRating;
    private Integer movieDurationMinutes;
    private String roomName;
    private String screenType;
    private Integer cinemaId;
    private String cinemaName;
}
