package com.cinema.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Facade DTO — Gom thông tin chi tiết phim + danh sách suất chiếu vào 1 response.
 * Dùng cho trang chi tiết phim, thay vì Frontend gọi 2 API riêng (movie detail + showtimes).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDetailDTO {
    private MovieDTO movie;
    private List<ShowtimeDTO> showtimes;
    private List<CinemaDTO> availableCinemas;
}
