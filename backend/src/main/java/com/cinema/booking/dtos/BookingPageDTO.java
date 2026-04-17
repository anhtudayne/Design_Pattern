package com.cinema.booking.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Facade DTO — Gom tất cả dữ liệu cần cho trang đặt vé Customer vào 1 response duy nhất.
 * Thay vì Frontend phải gọi 5 API riêng lẻ, chỉ cần gọi 1 API trả về BookingPageDTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPageDTO {
    private List<MovieDTO> movies;
    private List<CinemaDTO> cinemas;
    private List<LocationDTO> locations;
    private List<ShowtimeDTO> showtimes;
    private List<FnbItemDTO> fnbItems;
}
