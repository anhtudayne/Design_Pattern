package com.cinema.booking.services.adapter;

import com.cinema.booking.dtos.GenreDTO;
import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.entities.Movie.MovieStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete Adapter — Chuyển đổi dữ liệu từ TMDb (The Movie Database) sang MovieDTO.
 *
 * Xử lý các điểm khác biệt:
 *   - overview → description
 *   - runtime → durationMinutes
 *   - release_date (String "2025-06-15") → releaseDate (LocalDate)
 *   - poster_path ("/abc.jpg") → posterUrl ("https://image.tmdb.org/t/p/w500/abc.jpg")
 *   - original_language → language
 *   - genres (ExternalGenre) → genres (GenreDTO)
 *   - Mặc định status = COMING_SOON cho phim import
 */
@Component
public class TmdbMovieAdapter implements MovieDataAdapter {

    /** Base URL cho poster ảnh từ TMDb */
    private static final String POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @Override
    public MovieDTO adapt(ExternalMovieData source) {
        MovieDTO dto = new MovieDTO();

        // --- Mapping trực tiếp (cùng tên) ---
        dto.setTitle(source.getTitle());

        // --- Mapping khác tên field ---
        dto.setDescription(source.getOverview());            // overview → description
        dto.setDurationMinutes(source.getRuntime());         // runtime → durationMinutes

        // --- Mapping khác kiểu dữ liệu: String → LocalDate ---
        dto.setReleaseDate(parseReleaseDate(source.getRelease_date()));

        // --- Mapping cần ghép: path fragment → full URL ---
        dto.setPosterUrl(buildPosterUrl(source.getPoster_path()));

        // --- Mapping đổi tên: original_language → language ---
        dto.setLanguage(source.getOriginal_language());

        // --- Mapping genres: ExternalGenre → GenreDTO ---
        dto.setGenres(adaptGenres(source.getGenres()));

        // --- Giá trị mặc định cho phim import ---
        dto.setStatus(MovieStatus.COMING_SOON);

        return dto;
    }

    /**
     * Parse release_date String → LocalDate, trả null nếu format không hợp lệ.
     */
    private LocalDate parseReleaseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Ghép poster_path fragment với base URL của TMDb.
     * VD: "/abc123.jpg" → "https://image.tmdb.org/t/p/w500/abc123.jpg"
     */
    private String buildPosterUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) {
            return null;
        }
        return POSTER_BASE_URL + posterPath;
    }

    /**
     * Chuyển đổi danh sách ExternalGenre → GenreDTO.
     * Chỉ map tên genre (không map ID vì ID bên ngoài khác ID nội bộ).
     */
    private List<GenreDTO> adaptGenres(List<ExternalMovieData.ExternalGenre> externalGenres) {
        if (externalGenres == null || externalGenres.isEmpty()) {
            return Collections.emptyList();
        }
        return externalGenres.stream()
                .map(eg -> GenreDTO.builder()
                        .name(eg.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
