package com.cinema.booking.controllers;

import com.cinema.booking.entities.Genre;
import com.cinema.booking.entities.Movie;
import com.cinema.booking.entities.MovieGenre;
import com.cinema.booking.repositories.GenreRepository;
import com.cinema.booking.repositories.MovieRepository;
import com.cinema.booking.repositories.MovieGenreRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/movie-genres")
@Tag(name = "13. Liên kết Phim & Thể loại", description = "Các API quản lý bảng nối EXPLICIT giữa Phim và Thể loại")
public class MovieGenreController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    @Operation(summary = "Lấy danh sách thể loại của một phim")
    @GetMapping("/{movieId}")
    public ResponseEntity<List<Genre>> getGenresByMovie(@PathVariable Integer movieId) {
        List<MovieGenre> links = movieGenreRepository.findByMovieMovieId(movieId);
        List<Genre> genres = links.stream()
                .map(MovieGenre::getGenre)
                .collect(Collectors.toList());
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "Gán các thể loại cho phim (Bulk Update)")
    @PostMapping("/{movieId}")
    @Transactional
    public ResponseEntity<?> assignGenresToMovie(@PathVariable Integer movieId, @RequestBody List<Integer> genreIds) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại"));

        // Xóa các liên kết cũ
        movieGenreRepository.deleteByMovieMovieId(movieId);

        // Tạo các liên kết mới
        List<MovieGenre> newLinks = genreIds.stream()
                .map(id -> genreRepository.findById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(genre -> MovieGenre.builder().movie(movie).genre(genre).build())
                .collect(Collectors.toList());

        movieGenreRepository.saveAll(newLinks);
        return ResponseEntity.ok("Đã cập nhật các thể loại cho phim (ID: " + movieId + ")");
    }

    @Operation(summary = "Thêm một thể loại vào phim")
    @PostMapping("/{movieId}/add/{genreId}")
    public ResponseEntity<?> addGenreToMovie(@PathVariable Integer movieId, @PathVariable Integer genreId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại"));
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Thể loại không tồn tại"));

        // Kiểm tra xem đã tồn tại chưa
        boolean exists = movieGenreRepository.findByMovieMovieId(movieId).stream()
                .anyMatch(link -> link.getGenre().getGenreId().equals(genreId));

        if (!exists) {
            MovieGenre link = MovieGenre.builder().movie(movie).genre(genre).build();
            movieGenreRepository.save(link);
        }
        return ResponseEntity.ok("Đã thêm thể loại vào phim");
    }

    @Operation(summary = "Xóa một thể loại khỏi phim")
    @DeleteMapping("/{movieId}/remove/{genreId}")
    @Transactional
    public ResponseEntity<?> removeGenreFromMovie(@PathVariable Integer movieId, @PathVariable Integer genreId) {
        movieGenreRepository.deleteByMovieMovieIdAndGenreGenreId(movieId, genreId);
        return ResponseEntity.ok("Đã gỡ bỏ thể loại khỏi phim");
    }
}
