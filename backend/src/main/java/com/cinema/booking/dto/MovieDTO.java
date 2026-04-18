package com.cinema.booking.dto;

import com.cinema.booking.entity.Movie.MovieStatus;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MovieDTO {
    private Integer movieId;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String language;
    private String posterUrl;
    private MovieStatus status;
    private List<MovieCastDTO> movieCastList;
    private List<GenreDTO> genres;

    @Data
    public static class MovieCastDTO {
        private Integer id;
        private Integer castMemberId;
        private String roleName;
        private String roleType;
    }
}
