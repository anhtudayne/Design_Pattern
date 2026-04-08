package com.cinema.booking.repositories;

import com.cinema.booking.entities.Movie;
import com.cinema.booking.entities.Movie.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
    // Hàm phụ tìm danh sách phim đang rạp (Trạng thái NOW_SHOWING/COMING_SOON)
    List<Movie> findByStatus(MovieStatus status);
}
