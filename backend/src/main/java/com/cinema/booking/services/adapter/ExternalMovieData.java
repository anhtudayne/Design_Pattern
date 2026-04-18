package com.cinema.booking.services.adapter;

import lombok.Data;

import java.util.List;

/**
 * Adaptee — Cấu trúc dữ liệu từ API bên ngoài (TMDb - The Movie Database).
 * Các tên field và kiểu dữ liệu KHÁC hoàn toàn so với MovieDTO của StarCine.
 *
 * Ví dụ mapping:
 *   TMDb "overview"           → StarCine "description"
 *   TMDb "runtime"            → StarCine "durationMinutes"
 *   TMDb "release_date" (String) → StarCine "releaseDate" (LocalDate)
 *   TMDb "poster_path" (path)    → StarCine "posterUrl" (full URL)
 *   TMDb "original_language"     → StarCine "language"
 */
@Data
public class ExternalMovieData {
    private String title;
    private String overview;           // ≠ description
    private Integer runtime;           // ≠ durationMinutes
    private String release_date;       // String "2025-06-15" ≠ LocalDate
    private String poster_path;        // "/abc123.jpg" ≠ full URL
    private String original_language;  // "en" ≠ language
    private List<ExternalGenre> genres;

    @Data
    public static class ExternalGenre {
        private Integer id;
        private String name;
    }
}
