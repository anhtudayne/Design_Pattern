package com.cinema.booking.repositories;

import com.cinema.booking.entities.MovieGenre;
import com.cinema.booking.entities.MovieGenreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, MovieGenreId> {

    /** Lấy tất cả liên kết theo movieId */
    List<MovieGenre> findByMovieMovieId(Integer movieId);

    /** Xóa toàn bộ liên kết của một phim (dùng trong Bulk Update) */
    void deleteByMovieMovieId(Integer movieId);

    /** Xóa một liên kết cụ thể movie ↔ genre */
    void deleteByMovieMovieIdAndGenreGenreId(Integer movieId, Integer genreId);
}
