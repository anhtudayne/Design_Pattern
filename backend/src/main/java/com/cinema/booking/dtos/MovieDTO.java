package com.cinema.booking.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MovieDTO {
    private Integer movie_ID;
    private String title;
    private String description;
    private int duration_minutes;
    private LocalDate release_date;
    private String language;
    private String status;
    private List<MovieCastDTO> movieCastList;

    @Data
    public static class MovieCastDTO {
        private Integer id;
        private Integer cast_member_id;
        private String role_name;
        private String role_type;
    }
}
