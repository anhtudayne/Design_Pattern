package com.cinema.booking.repository;

import com.cinema.booking.entity.Movie;
import com.cinema.booking.entity.Movie.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    // Hàm phụ tìm danh sách phim đang rạp (Trạng thái NOW_SHOWING/COMING_SOON)
    List<Movie> findByStatus(MovieStatus status);
}
