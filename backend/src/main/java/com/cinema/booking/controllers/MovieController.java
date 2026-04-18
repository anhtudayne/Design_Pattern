package com.cinema.booking.controllers;

import com.cinema.booking.dtos.MovieDTO;
import com.cinema.booking.dtos.MovieDTO.MovieCastDTO;
import com.cinema.booking.entities.Movie.MovieStatus;
import com.cinema.booking.services.MovieService;
import com.cinema.booking.services.adapter.ExternalMovieData;
import com.cinema.booking.services.adapter.MovieDataAdapter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    // ──────────────────────────────────────────────────────────────────
    //  Adapter Pattern: MovieDataAdapter chuyển đổi dữ liệu bên ngoài
    // ──────────────────────────────────────────────────────────────────
    @Autowired
    private MovieDataAdapter movieDataAdapter;

    // GET /api/movies?status=NOW_SHOWING
    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies(
            @RequestParam(required = false) MovieStatus status) {
        if (status != null) {
            return ResponseEntity.ok(movieService.getMoviesByStatus(status));
        }
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Integer id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @PostMapping
    public ResponseEntity<MovieDTO> createMovie(@Valid @RequestBody MovieDTO movieDTO) {
        return ResponseEntity.ok(movieService.createMovie(movieDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieDTO> updateMovie(@PathVariable Integer id, @Valid @RequestBody MovieDTO movieDTO) {
        return ResponseEntity.ok(movieService.updateMovie(id, movieDTO));
    }

    /**
     * Thay thế danh sách Cast của một phim.
     * Payload là danh sách MovieDTO.MovieCastDTO (castMemberId + roleType + roleName).
     */
    @PutMapping("/{id}/casts")
    public ResponseEntity<MovieDTO> replaceMovieCasts(@PathVariable Integer id, @RequestBody java.util.List<MovieCastDTO> casts) {
        MovieDTO dto = movieService.getMovieById(id);
        dto.setMovieCastList(casts);
        return ResponseEntity.ok(movieService.updateMovie(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok().build();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Adapter Pattern — Import phim từ nguồn bên ngoài (VD: TMDb API)
    //  Admin gửi dữ liệu dạng ExternalMovieData → Adapter chuyển đổi
    //  sang MovieDTO → gọi movieService.createMovie() để lưu vào DB
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Import phim từ dữ liệu bên ngoài (Adapter Pattern).
     * Nhận ExternalMovieData (format TMDb) → adapt sang MovieDTO → tạo phim mới.
     */
    @PostMapping("/import-external")
    public ResponseEntity<MovieDTO> importExternalMovie(@RequestBody ExternalMovieData externalData) {
        // Bước 1: Adapter chuyển đổi format bên ngoài → format nội bộ
        MovieDTO adaptedMovie = movieDataAdapter.adapt(externalData);

        // Bước 2: Gọi service tạo phim bình thường (như nhập thủ công)
        MovieDTO createdMovie = movieService.createMovie(adaptedMovie);

        return ResponseEntity.ok(createdMovie);
    }

    /**
     * Import nhiều phim cùng lúc từ nguồn bên ngoài (Adapter Pattern - batch).
     */
    @PostMapping("/import-external/batch")
    public ResponseEntity<List<MovieDTO>> importExternalMovies(@RequestBody List<ExternalMovieData> externalDataList) {
        List<MovieDTO> createdMovies = externalDataList.stream()
                .map(movieDataAdapter::adapt)
                .map(movieService::createMovie)
                .toList();

        return ResponseEntity.ok(createdMovies);
    }
}
