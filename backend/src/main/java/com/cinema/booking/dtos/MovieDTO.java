package com.cinema.booking.dtos;

import com.cinema.booking.entities.Movie.AgeRating;
import com.cinema.booking.entities.Movie.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieDTO {
    private Integer movieId;
    
    @NotBlank(message = "Tên phim không được phép trống")
    private String title;
    
    private String description;
    
    @NotNull(message = "Thời lượng phim là yêu cầu bắt buộc")
    private Integer durationMinutes;
    
    private LocalDate releaseDate;
    private String language;
    private AgeRating ageRating;
    private String posterUrl;
    private String trailerUrl;
    private MovieStatus status;
}
