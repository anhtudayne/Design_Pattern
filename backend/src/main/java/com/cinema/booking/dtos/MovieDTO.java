package com.cinema.booking.dtos;

import com.cinema.booking.entities.Movie.AgeRating;
import com.cinema.booking.entities.Movie.MovieStatus;
import com.cinema.booking.entities.MovieCast.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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

    /**
     * Danh sách nhân sự (diễn viên/đạo diễn) gắn với phim.
     * - Khi tạo/cập nhật: client gửi castMemberId + roleType + roleName.
     * - Khi lấy chi tiết: server trả thêm castMemberName/bio.
     */
    private List<MovieCastDTO> casts;

    @Data
    public static class MovieCastDTO {
        private Integer id;
        private Integer castMemberId;
        private String castMemberName;
        private String castMemberBio;
        private String roleName;
        private RoleType roleType;
    }
}
